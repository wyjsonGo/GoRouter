package com.wyjson.router.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE})
@Retention(SOURCE)
public @interface Service {

    /**
     * alias
     */
    String alias() default "";

    /**
     * The remark of interceptor, may be used to generate javadoc.
     */
    String remark() default "";
}
