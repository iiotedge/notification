package com.iotmining.services.notification.ratelimiter;

import com.iotmining.services.notification.exceptions.RateLimitExceededException;
import com.iotmining.services.notification.model.Plan;
import com.iotmining.services.notification.model.Priority;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Service
@Slf4j
public class Bucket4jRateLimiterService {
//    private final ProxyManager<String> proxyManager;
    private final ConcurrentMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();
    private final CacheManager cacheManager;

    public Bucket4jRateLimiterService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

//    public Bucket4jRateLimiterService(ProxyManager<String> proxyManager) {
////        this.proxyManager = proxyManager;
//    }
//    public Bucket resolveBucket2(String userId, Plan plan, Priority priority) {
//        String key = userId + ":" + priority.name();
//
//        proxyManager.builder().getClass()
//        // Use ProxyManager to create or retrieve the bucket
//        return proxyManager.getC(key, (context) -> {
//            // Define the rate limit (bandwidth) for the bucket based on the plan and priority
//            Bandwidth bandwidth = switch (priority) {
//                case HIGH -> plan.getHighPriorityBandwidth();
//                default -> plan.getBasicBandwidth();
//            };
//
//            // Create a Bucket with the specified bandwidth
//            return Bucket.builder()
//                    .addLimit(bandwidth)
//                    .build();
//        });
//    }

    // Resolves bucket from cache or creates a new one if not present
    public Bucket resolveBucket(String userId, Plan plan, Priority priority) {
        String cacheKey = userId + ":" + (priority == Priority.HIGH ? "high" : "standard");

//        // Using CacheManager for more advanced cache management (TTL, eviction, etc.)
////        Bucket bucket = (Bucket) Objects.requireNonNull(cacheManager.getCache("rateLimitCache")).get(cacheKey);
//
//        if (bucket == null) {
//            log.info("Creating bucket for user {} with plan {} and priority {}", userId, plan, priority);
//
//            Bandwidth bandwidth;
//            if (priority == Priority.HIGH) {
//                bandwidth = plan.getHighPriorityBandwidth();
//            } else {
//                bandwidth = plan.getBasicBandwidth();
//            }
//
//            // Correct way to build a bucket with Bucket4j
//            bucket = Bucket.builder()
//                    .addLimit(bandwidth)
//                    .build();
//
//            // Store the bucket in cache with TTL if required
//            Objects.requireNonNull(cacheManager.getCache("rateLimitCache")).put(cacheKey, bucket);
//        }
//
//        return bucket;

        return bucketCache.computeIfAbsent(cacheKey, key -> {
            log.info("Creating bucket for user {} with plan {} and priority {}", userId, plan, priority);

            Bandwidth bandwidth = (priority == Priority.HIGH)
                    ? plan.getHighPriorityBandwidth()
                    : plan.getBasicBandwidth();

            return Bucket.builder()
                    .addLimit(bandwidth)
                    .build();
        });
    }

    // Executes the operation with rate limit enforcement
    public <T> T executeWithRateLimit(String userId, Plan plan, Priority priority, Supplier<T> operation) {
        // For CRITICAL priority, bypass rate limiting
        if (priority == Priority.CRITICAL) {
            return operation.get();
        }

        // Resolve or create a new bucket for the user
        Bucket bucket = resolveBucket(userId, plan, priority);

        // Check if we can consume a token from the bucket
        if (bucket.tryConsume(1)) {
//            log.debug("Rate limit allowed for user {} with plan {} and priority {}", userId, plan, priority);
            return operation.get();
        } else {
            // If it's HIGH priority, we allow the operation even if rate limit is exceeded
            if (priority == Priority.HIGH) {
//                log.warn("Rate limit exceeded for HIGH priority message from user {}, but allowing it", userId);
                return operation.get();
            }

            // For other priorities, throw an exception
//            log.warn("Rate limit exceeded for user {} with plan {} and priority {}", userId, plan, priority);
            throw new RateLimitExceededException(String.format("Rate limit exceeded for user %s with plan %s and priority %s", userId, plan, priority));
        }
    }
}

//@Service
//@Slf4j
//public class Bucket4jRateLimiterService {
//
//    private final ConcurrentMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();
//    private final CacheManager cacheManager;
//
//    public Bucket4jRateLimiterService(CacheManager cacheManager) {
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
//            // Correct way to build a bucket with Bucket4j
//            return Bucket.builder()
//                    .addLimit(bandwidth)
//                    .build();
//        });
//    }
//
//    public <T> T executeWithRateLimit(String userId, Plan plan, Priority priority, Supplier<T> operation) {
//        // For CRITICAL priority, bypass rate limiting
//        if (priority == Priority.CRITICAL) {
/// /            log.info("Bypassing rate limit for CRITICAL priority message from user {}", userId);
//            return operation.get();
//        }
//
//        Bucket bucket = resolveBucket(userId, plan, priority);
//
//        // Check if we can consume a token
//        if (bucket.tryConsume(1)) {
/// /            log.debug("Rate limit allowed for user {} with plan {} and priority {}", userId, plan, priority);
//            return operation.get();
//        } else {
//            // For HIGH priority, try to send anyway if rate limited
//            if (priority == Priority.HIGH) {
/// /                log.warn("Rate limit exceeded for HIGH priority message from user {}, but allowing it", userId);
//                return operation.get();
//            }
//
////            log.warn("Rate limit exceeded for user {} with plan {} and priority {}", userId, plan, priority);
//            throw new RateLimitExceededException("Rate limit exceeded for user with plan: " + plan);
//        }
//    }
//}