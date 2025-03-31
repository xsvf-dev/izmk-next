package ovo.xsvf.patchify.annotation;


import ovo.xsvf.patchify.api.ILocals;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify the local variables that need to be modified by the inject method.
 * <p>
 *     <b>Note:</b> The method should be public and static, with only
 *     one parameter of {@link ILocals} and return type of void.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.METHOD})
public @interface ModifyLocals {
    /**
     * The name of the method to be injected.
     *
     * @return the name of the method to be injected.
     */
    String method();

    /**
     * The descriptor of the method to be injected.
     *
     * @return the descriptor of the method to be injected.
     */
    String desc();

    /**
     * The local variable indexes that need to be modified.
     *
     * @return the local variable indexes that need to be modified.
     */
    int[] indexes();

    /**
     * The types of the local variables that need to be modified.
     * <p>
     *      <b>Note:</b> The length of this array should be the same as the length of {@link #indexes()} .
     * </p>
     *
     * @return the types of the local variables that need to be modified.
     */
    Class<?>[] types();

    /**
     * The position of the injection point.
     * <p>
     *     <b>Note:</b> The method will only be injected for one time at the specified position.
     * </p>
     * @return the position of the injection point.
     */
    At at() default @At(value = At.Type.HEAD);
}
