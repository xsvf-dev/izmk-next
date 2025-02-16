package ovo.xsvf.izmk.injection.accessor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If the method or field is inherited from a superclass,
 * use this annotation to specify the superclass that the method or field is inherited from.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Relocate {
    Class<?> value();
}
