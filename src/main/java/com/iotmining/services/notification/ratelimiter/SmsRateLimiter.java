//package com.iotmining.services.notification.ratelimiter;
//
//import com.iotmining.services.notification.exceptions.RateLimitExceededException;
//import com.iotmining.services.notification.model.Plan;
//import com.iotmining.services.notification.model.Priority;
//import io.github.bucket4j.Bandwidth;
//import io.github.bucket4j.Bucket;
//import io.github.bucket4j.BucketConfiguration;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.CachePut;
//import org.springframework.stereotype.Service;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.function.Supplier;
//
//@Service
//@Slf4j
//public class SmsRateLimiter {
//
//    private final ConcurrentMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();
//    private final CacheManager cacheManager;
//
//    public SmsRateLimiter(CacheManager cacheManager) {
//        this.cacheManager = cacheManager;
//    }
//
//    public Bucket resolveBucket(String userId, Plan plan, Priority priority) {
//        String cacheKey = userId + ":" + (priority == Priority.HIGH ? "high" : "standard");
//
//        return bucketCache.computeIfAbsent(cacheKey, key -> {
//            log.info("Creating bucket for user {} with plan {} and priority {}", userId, plan, priority);
//
//            Bandwidth bandwidth;
//            if (priority == Priority.HIGH) {
//                bandwidth = plan.getHighPriorityBandwidth();
//            } else {
//                bandwidth = plan.getBasicBandwidth();
//            }
//
//            BucketConfiguration configuration = BucketConfiguration.builder()
//                    .addLimit(bandwidth)
//                    .build();
//
//            return Bucket.builder().withConfiguration(configuration).build();
//        });
//    }
//
//    public <T> T executeWithRateLimit(String userId, Plan plan, Priority priority, Supplier<T> operation) {
//        // For CRITICAL priority, bypass rate limiting
//        if (priority == Priority.CRITICAL) {
//            log.info("Bypassing rate limit for CRITICAL priority message from user {}", userId);
//            return operation.get();
//        }
//
//        Bucket bucket = resolveBucket(userId, plan, priority);
//
//        // Check if we can consume a token
//        if (bucket.tryConsume(1)) {
//            log.debug("Rate limit allowed for user {} with plan {} and priority {}", userId, plan, priority);
//            return operation.get();
//        } else {
//            // For HIGH priority, try to send anyway if rate limited
//            if (priority == Priority.HIGH) {
//                log.warn("Rate limit exceeded for HIGH priority message from user {}, but allowing it", userId);
//                return operation.get();
//            }
//
//            log.warn("Rate limit exceeded for user {} with plan {} and priority {}", userId, plan, priority);
//            throw new RateLimitExceededException("Rate limit exceeded for user with plan: " + plan);
//        }
//    }
//}