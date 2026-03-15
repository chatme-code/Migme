package com.sleepycat.persist.model;
import java.lang.annotation.*;
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Persistent {
    int version() default 0;
    Class proxyFor() default void.class;
}
