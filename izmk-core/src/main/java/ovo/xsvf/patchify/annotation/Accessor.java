package ovo.xsvf.patchify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class as an accessor for anothor class.
 * <p>
 *     <b>Note:</b> The annotated class must be an interface.
 * </p>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Accessor {
    Class<?> value();
}
