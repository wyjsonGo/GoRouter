package com.wyjson.router.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(CLASS)
public @interface Interceptor {
    /**
     * The priority of interceptor, GoRouter will be excute them follow the priority.
     */
    int priority();

    /**
     * The remark of interceptor, may be used to generate javadoc.
     */
    String remark() default "";
}
