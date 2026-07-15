package com.hotel.hotel_system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @Test
    void buildLockKey_isDerivedFromRoomIdAndDateRange() {
        LockService lockService = new LockService(redissonClient);

        String key = lockService.buildLockKey(42L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5));

        assertTrue(key.contains("42"));
        assertTrue(key.contains("2026-08-01"));
        assertTrue(key.contains("2026-08-05"));
    }

    @Test
    void tryAcquire_returnsLock_whenRedissonGrantsIt() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(0, LockService.LOCK_TTL_MINUTES, TimeUnit.MINUTES)).thenReturn(true);

        LockService lockService = new LockService(redissonClient);
        RLock acquired = lockService.tryAcquire("some-key");

        assertNotNull(acquired);
        verify(rLock).tryLock(0, LockService.LOCK_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Test
    void tryAcquire_returnsNull_whenAnotherCustomerAlreadyHoldsTheLock() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(0, LockService.LOCK_TTL_MINUTES, TimeUnit.MINUTES)).thenReturn(false);

        LockService lockService = new LockService(redissonClient);
        RLock acquired = lockService.tryAcquire("some-key");

        assertNull(acquired);
    }

    @Test
    void release_forceUnlocksWhenLocked() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.isLocked()).thenReturn(true);

        LockService lockService = new LockService(redissonClient);
        lockService.release("some-key");

        verify(rLock).forceUnlock();
    }

    @Test
    void release_isNoOp_whenNotLocked() {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.isLocked()).thenReturn(false);

        LockService lockService = new LockService(redissonClient);
        lockService.release("some-key");

        verify(rLock, never()).forceUnlock();
    }
}
