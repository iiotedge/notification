package com.iotmining.services.notification.annotations;

import com.iotmining.services.notification.model.Plan;
import com.iotmining.services.notification.model.Priority;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SmsRateLimited {
    Plan plan() default Plan.BASIC;

    Priority priority() default Priority.LOW;

    /**
     * SpEL expression to evaluate userId from method arguments.
     * Example: "#smsRequest.userId"
     */
    String userId();
}
