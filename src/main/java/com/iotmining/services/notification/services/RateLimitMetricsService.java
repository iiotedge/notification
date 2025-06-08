
package com.iotmining.services.notification.services;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitMetricsService {

    private final Map<String, UserMetrics> userMetricsMap = new ConcurrentHashMap<>();

    public void recordSuccessfulRequest(String userId) {
        getUserMetrics(userId).incrementSuccessful();
    }

    public void recordRateLimitedRequest(String userId) {
        getUserMetrics(userId).incrementRateLimited();
    }

    public UserMetrics getUserMetrics(String userId) {
        return userMetricsMap.computeIfAbsent(userId, id -> new UserMetrics());
    }

    public Map<String, UserMetrics> getAllMetrics() {
        return userMetricsMap;
    }

    @Data
    public static class UserMetrics {
        private final LocalDateTime since = LocalDateTime.now();
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong rateLimitedRequests = new AtomicLong(0);

        public void incrementSuccessful() {
            successfulRequests.incrementAndGet();
        }

        public void incrementRateLimited() {
            rateLimitedRequests.incrementAndGet();
        }

        public long getTotal() {
            return successfulRequests.get() + rateLimitedRequests.get();
        }
    }
}