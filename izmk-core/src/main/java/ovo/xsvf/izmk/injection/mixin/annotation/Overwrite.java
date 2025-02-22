package ovo.xsvf.izmk.injection.mixin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate that a method overwrites the method to be injected.
 * <p>
 *     <b>Note:</b> The method should be public and static, with the first parameter being the target class instance
 *     and have the other parameters and return type as the same as the method to be injected.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Overwrite {
    /**
     * The name of the method to be injected.
     * @return the name of the method to be injected
     */
    String method();

    /**
     * The description of the method to be injected.
     * @return the description of the method to be injected
     */
    String desc();
}
