package com.hotel.hotel_system.service;

import com.hotel.hotel_system.dto.PaymentRequest;
import com.hotel.hotel_system.dto.PaymentResponse;
import com.hotel.hotel_system.exception.PaymentFailedException;
import com.hotel.hotel_system.model.Reservation;
import com.hotel.hotel_system.model.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ReservationService reservationService;

    private PaymentService paymentService;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(reservationService);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatus(ReservationStatus.HELD);
        reservation.setTotalPrice(BigDecimal.valueOf(300));

        lenient().when(reservationService.getByIdOrThrow(1L)).thenReturn(reservation);
    }

    @Test
    void processPayment_confirmsReservation_whenCardValidAndAmountMatches() {
        PaymentRequest request = new PaymentRequest();
        request.setReservationId(1L);
        request.setAmount(BigDecimal.valueOf(300));
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Alice");

        PaymentResponse response = paymentService.processPayment(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getTransactionId());
        verify(reservationService).moveToPaymentProcessing(1L);
        verify(reservationService).confirm(1L);
        verify(reservationService, never()).fail(anyLong());
    }

    @Test
    void processPayment_fails_whenCardNumberInvalid() {
        PaymentRequest request = new PaymentRequest();
        request.setReservationId(1L);
        request.setAmount(BigDecimal.valueOf(300));
        request.setCardNumber("123"); // too short -> declined
        request.setCardHolderName("Alice");

        assertThrows(PaymentFailedException.class, () -> paymentService.processPayment(request));

        verify(reservationService).moveToPaymentProcessing(1L);
        verify(reservationService).fail(1L);
        verify(reservationService, never()).confirm(anyLong());
    }

    @Test
    void processPayment_rejectsAmountMismatch_beforeTouchingStateMachine() {
        PaymentRequest request = new PaymentRequest();
        request.setReservationId(1L);
        request.setAmount(BigDecimal.valueOf(999)); // does not match reservation total of 300
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("Alice");

        assertThrows(PaymentFailedException.class, () -> paymentService.processPayment(request));

        verify(reservationService, never()).moveToPaymentProcessing(anyLong());
    }
}
