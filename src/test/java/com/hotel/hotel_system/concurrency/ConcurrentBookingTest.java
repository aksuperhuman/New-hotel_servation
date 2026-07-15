package com.hotel.hotel_system.concurrency;

import com.hotel.hotel_system.dto.ReservationRequest;
import com.hotel.hotel_system.exception.RoomUnavailableException;
import com.hotel.hotel_system.model.Hotel;
import com.hotel.hotel_system.model.Reservation;
import com.hotel.hotel_system.model.Room;
import com.hotel.hotel_system.model.RoomStatus;
import com.hotel.hotel_system.model.RoomType;
import com.hotel.hotel_system.repository.ReservationRepository;
import com.hotel.hotel_system.repository.RoomRepository;
import com.hotel.hotel_system.service.AvailabilityService;
import com.hotel.hotel_system.service.LockService;
import com.hotel.hotel_system.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Simulates two customers trying to book the exact same room for the
 * exact same date range at the same instant. Exactly one must win
 * (HELD reservation created); the other must receive the
 * RoomUnavailableException that the controller layer maps to HTTP 409.
 *
 * LockService's Redisson call is faked with a real AtomicBoolean-backed
 * "distributed lock" so the race condition is genuinely exercised
 * across threads, without requiring a live Redis server in the test
 * environment.
 */
@ExtendWith(MockitoExtension.class)
class ConcurrentBookingTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private AvailabilityService availabilityService;
    @Mock
    private RLock rLock;

    private Room room;
    private ReservationRequest request;

    @BeforeEach
    void setUp() {
        Hotel hotel = new Hotel();
        hotel.setId(1L);

        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setBasePrice(BigDecimal.valueOf(150));

        room = new Room();
        room.setId(55L);
        room.setHotel(hotel);
        room.setRoomType(roomType);
        room.setStatus(RoomStatus.AVAILABLE);

        request = new ReservationRequest();
        request.setRoomId(55L);
        request.setGuestName("Racer");
        request.setCheckInDate(LocalDate.of(2026, 9, 1));
        request.setCheckOutDate(LocalDate.of(2026, 9, 3));

        lenient().when(roomRepository.findById(55L)).thenReturn(Optional.of(room));
        lenient().when(availabilityService.isRoomAvailable(eq(55L), any(), any())).thenReturn(true);
        lenient().when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void onlyOneOfTwoConcurrentBookingAttempts_forSameRoomAndDates_succeeds() throws InterruptedException {
        // A single AtomicBoolean plays the role of the distributed Redis
        // lock key "reservation-lock:room:55:2026-09-01:2026-09-03" -
        // only the first thread to flip it false->true acquires it.
        AtomicBoolean locked = new AtomicBoolean(false);

        LockService lockService = mock(LockService.class);
        when(lockService.buildLockKey(55L, request.getCheckInDate(), request.getCheckOutDate()))
                .thenReturn("reservation-lock:room:55:2026-09-01:2026-09-03");
        when(lockService.tryAcquire(anyString())).thenAnswer(inv ->
                locked.compareAndSet(false, true) ? rLock : null);

        ReservationService reservationService =
                new ReservationService(reservationRepository, roomRepository, availabilityService, lockService);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        Callable<Void> attemptBooking = () -> {
            startGate.await();
            try {
                reservationService.createReservation(request, null);
                successCount.incrementAndGet();
            } catch (RoomUnavailableException e) {
                conflictCount.incrementAndGet();
            }
            return null;
        };

        Future<Void> f1 = pool.submit(attemptBooking);
        Future<Void> f2 = pool.submit(attemptBooking);

        startGate.countDown(); // release both threads at the same time
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        try {
            f1.get();
            f2.get();
        } catch (ExecutionException e) {
            fail("Unexpected exception during concurrent booking: " + e.getCause());
        }

        assertEquals(1, successCount.get(), "exactly one booking attempt should succeed");
        assertEquals(1, conflictCount.get(), "exactly one booking attempt should be rejected with 409");
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }
}
