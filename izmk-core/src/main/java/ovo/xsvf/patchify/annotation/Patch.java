package ovo.xsvf.patchify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a class as a patch for another class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Patch {
    /**
     * The target class to be patched.
     * @return the target class.
     */
    Class<?> value();
}
