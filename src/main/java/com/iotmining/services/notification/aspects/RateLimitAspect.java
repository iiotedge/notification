package com.iotmining.services.notification.aspects;


//import com.iotmining.common.base.notifications.NotificationDtoImpl;
import com.iotmining.common.base.notifications.dto.BaseRequest;
import com.iotmining.common.base.notifications.dto.NotificationWrapper;
import com.iotmining.common.base.notifications.dto.payload.SmsPayload;
import com.iotmining.services.notification.annotations.SmsRateLimited;
import com.iotmining.services.notification.exceptions.RateLimitExceededException;
import com.iotmining.services.notification.model.Plan;
import com.iotmining.services.notification.model.Priority;
import com.iotmining.services.notification.ratelimiter.Bucket4jRateLimiterService;

import com.iotmining.services.notification.services.RateLimitMetricsService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.ProceedingJoinPoint;


@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private final Bucket4jRateLimiterService rateLimiterService;
    private final RateLimitMetricsService metricsService;

    public RateLimitAspect(Bucket4jRateLimiterService rateLimiterService, RateLimitMetricsService metricsService) {
        this.rateLimiterService = rateLimiterService;
        this.metricsService = metricsService;
    }

    //    @SneakyThrows
    @Around("@annotation(SmsRateLimited)")
    public Object applyRateLimit(ProceedingJoinPoint pjp, SmsRateLimited smsRateLimited) {
        // Extract method arguments (smsRequest or similar)
        Object[] args = pjp.getArgs();

        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Argument missing for rate limiting check.");
        }

        // Assuming the first argument is of type NotificationDtoImpl
        NotificationWrapper<SmsPayload>  smsRequest = (NotificationWrapper<SmsPayload>) args[0];

        // Ensure smsRequest and userId are not null
        if (smsRequest == null || smsRequest.getUserId() == null) {
            throw new IllegalArgumentException("DTO or userId is null");
        }

        // Extract userId, plan, and priority either from the annotation or dynamic values
        String userId = smsRequest.getUserId().toString();
        Plan plan = smsRateLimited.plan();
        Priority priority = smsRateLimited.priority();

        try {
            return rateLimiterService.executeWithRateLimit(userId, plan, priority, () -> {
                try {
                    Object proceed = pjp.proceed();
                    metricsService.recordSuccessfulRequest(userId);
                    return proceed;
                } catch (Throwable e) {
                    throw new RateLimitExceededException(e.getMessage());
                }
            });
        } catch (RuntimeException e) {
            metricsService.recordRateLimitedRequest(userId);
//            log.error("Error during rate limiting: {}", e.getMessage());
            throw new RateLimitExceededException(e.getMessage());
        }
    }

}

