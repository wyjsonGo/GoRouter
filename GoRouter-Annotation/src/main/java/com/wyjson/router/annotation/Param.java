package com.wyjson.router.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({FIELD})
@Retention(SOURCE)
public @interface Param {

    // Mark param name
    String name() default "";

    // remark of the field
    String remark() default "";

    // If required, app will be crash when value is null.
    // Primitive type wont be check!
    boolean required() default false;
}