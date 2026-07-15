package com.hotel.hotel_system.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * Wraps Redisson distributed locking so the booking flow can guarantee
 * that only one customer at a time can hold/confirm a given room for a
 * given date range, even across multiple application instances.
 */
@Service
public class LockService {

    private static final Logger log = LoggerFactory.getLogger(LockService.class);

    public static final long LOCK_TTL_MINUTES = 5;

    private final RedissonClient redissonClient;

    public LockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public String buildLockKey(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return "reservation-lock:room:%d:%s:%s".formatted(roomId, checkIn, checkOut);
    }

    /**
     * Attempts to immediately acquire the lock (no waiting - if another
     * customer already holds it, fail fast so the caller can return
     * HTTP 409). The lock auto-expires after {@link #LOCK_TTL_MINUTES}
     * even if never explicitly released, which is what gives us
     * automatic hold-expiry.
     *
     * @return the acquired RLock, or null if the lock is already held.
     */
    public RLock tryAcquire(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired;
        try {
            acquired = lock.tryLock(0, LOCK_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            acquired = false;
        }
        if (acquired) {
            log.info("Lock acquired key={} ttlMinutes={}", lockKey, LOCK_TTL_MINUTES);
            return lock;
        }
        log.info("Lock acquisition FAILED (already held) key={}", lockKey);
        return null;
    }

    /**
     * Releases the lock early (e.g. once payment confirms or fails and
     * the hold is no longer needed).
     *
     * NOTE: the hold (lock acquisition) and its eventual release
     * typically happen on different HTTP requests - and therefore
     * different threads - than the one that originally called
     * {@link #tryAcquire}. Redisson's plain unlock() enforces
     * thread-ownership (it's a reentrant lock) and would throw
     * IllegalMonitorStateException in that scenario, so we use
     * forceUnlock() which releases the key regardless of which
     * thread/request owns it. That's safe here because this lock is
     * used purely as a business-level mutex keyed by room+date-range,
     * not as an in-JVM reentrant lock.
     */
    public void release(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked()) {
            lock.forceUnlock();
            log.info("Lock released key={}", lockKey);
        }
    }
}
