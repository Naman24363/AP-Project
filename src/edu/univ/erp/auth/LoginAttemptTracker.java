package edu.univ.erp.auth;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks failed login attempts globally (across all user roles).
 * After 5 failed attempts, locks login for 15 seconds.
 */
public class LoginAttemptTracker {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15000; // 15 seconds

    private static class AttemptRecord {
        long timestamp;
        int count;

        AttemptRecord(long timestamp, int count) {
            this.timestamp = timestamp;
            this.count = count;
        }
    }

    // Global tracker: Maps username to attempt records
    private static final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    /**
     * Records a failed login attempt for a username.
     * Returns the current attempt count after recording.
     */
    public static int recordFailedAttempt(String username) {
        long now = System.currentTimeMillis();

        attempts.compute(username, (key, record) -> {
            if (record == null) {
                // First attempt - record timestamp of first attempt in this cycle
                return new AttemptRecord(now, 1);
            } else {
                // Don't reset based on time - keep incrementing until lockout expires
                // Only increment count
                return new AttemptRecord(record.timestamp, record.count + 1);
            }
        });

        return attempts.get(username).count;
    }

    /**
     * Checks if a username is currently locked out.
     * Returns remaining lockout time in seconds (0 if not locked).
     */
    public static long getLockedOutSeconds(String username) {
        AttemptRecord record = attempts.get(username);
        if (record == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - record.timestamp;

        if (record.count >= MAX_ATTEMPTS) {
            long remaining = LOCKOUT_DURATION_MS - elapsed;
            if (remaining > 0) {
                return (remaining + 999) / 1000; // Round up to nearest second
            } else {
                // Lockout expired, reset
                attempts.remove(username);
                return 0;
            }
        }

        return 0;
    }

    /**
     * Clears failed attempts for a username after successful login.
     */
    public static void clearAttempts(String username) {
        attempts.remove(username);
    }

    /**
     * Checks if a username is locked out.
     */
    public static boolean isLockedOut(String username) {
        return getLockedOutSeconds(username) > 0;
    }

    /**
     * Gets the current attempt count for a username.
     */
    public static int getAttemptCount(String username) {
        AttemptRecord record = attempts.get(username);
        if (record == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        if (record.count >= MAX_ATTEMPTS) {
            // If locked out, check if lockout has expired
            long elapsed = now - record.timestamp;
            if (elapsed >= LOCKOUT_DURATION_MS) {
                attempts.remove(username);
                return 0;
            }
        } else if (now - record.timestamp >= LOCKOUT_DURATION_MS) {
            // Not locked out yet, but time window expired - reset
            attempts.remove(username);
            return 0;
        }

        return Math.min(record.count, MAX_ATTEMPTS);
    }
}
