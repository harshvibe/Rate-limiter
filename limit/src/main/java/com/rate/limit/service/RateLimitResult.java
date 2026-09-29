package com.rate.limit.service;

public class RateLimitResult {
    private final boolean allowed;
    private final int count;
    private final int remaining;
    private final int limit;

    public RateLimitResult(boolean allowed, int count, int remaining, int limit) {
        this.allowed = allowed;
        this.count = count;
        this.remaining = remaining;
        this.limit = limit;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public int getCount() {
        return count;
    }

    public int getRemaining() {
        return remaining;
    }

    public int getLimit() {
        return limit;
    }
}
