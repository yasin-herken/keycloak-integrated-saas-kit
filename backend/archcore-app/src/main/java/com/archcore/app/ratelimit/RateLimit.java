package com.archcore.app.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    int requests() default 60;

    int periodSeconds() default 60;

    String key() default "";

    RateLimitScope scope() default RateLimitScope.USER;

    enum RateLimitScope {
        IP,
        USER,
        ENDPOINT
    }
}
