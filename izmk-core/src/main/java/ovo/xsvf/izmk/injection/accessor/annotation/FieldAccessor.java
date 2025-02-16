package ovo.xsvf.izmk.injection.accessor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as a field accessor.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface FieldAccessor {
    String value();

    /**
     * Whether this accessor is a getter or a setter.
     * @return true if this accessor is a getter, false if it is a setter.
     */
    boolean getter() default true;
}
