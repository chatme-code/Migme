package com.sleepycat.persist.model;
import java.lang.annotation.*;
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    int version() default 0;
    String minimumVersion() default "";
}
