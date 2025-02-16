package ovo.xsvf.izmk.injection.mixin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a class as a mixin class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Mixin {
    /**
     * The target class to be mixed in.
     * @return the target class.
     */
    Class<?> value();
}
