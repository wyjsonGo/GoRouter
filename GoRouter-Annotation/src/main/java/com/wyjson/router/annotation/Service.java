package com.wyjson.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Service {
    /**
     * The name of interceptor, may be used to generate javadoc.
     */
    String name() default "";
}
