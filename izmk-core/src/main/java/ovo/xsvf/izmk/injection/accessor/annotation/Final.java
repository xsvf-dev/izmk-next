package ovo.xsvf.izmk.injection.accessor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a field setter accessor as final.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface Final {
}
