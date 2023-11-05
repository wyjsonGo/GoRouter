package com.wyjson.router.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(SOURCE)
public @interface Route {

    /**
     * Path of route
     */
    String path();

    /**
     * remark of route, used to generate javadoc.
     */
    String remark() default "";

    /**
     * Extra data, can be set by user.
     * Ps. U should use the integer num sign the switch, Range from 1 to 1 << 30
     */
    int tag() default 0;
}