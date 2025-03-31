package ovo.xsvf.patchify.annotation;

import ovo.xsvf.patchify.api.Invocation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;

/**
 * This annotation is used to mark a method as a wrapper method for invoking a method in a target class.
 * <p>
 *     <b>Note:</b> The method annotated with this annotation should be public and static,
 *     and have a parameter of {@link Callable<Object>} type as the last parameter. If the
 *     target method is a non-static method, the wrapper method should have a parameter of the
 *     target class type as the first parameter. The method's return type should be the same
 *     as the target method's return type.
 * </p>
 * @see Invocation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WrapInvoke {
    /**
     * The name of the target method.
     * @return the name of the target method to be injected
     */
    String method();

    /**
     * The description of the target method to be injected.
     * @return the descriptor of the target method to be injected
     */
    String desc();

    /**
     * The name of the target method to be wrapped.
     * @return the name of the target method to be wrapped
     */
    String target();

    /**
     * The descriptor of the target method to be wrapped.
     * @return the descriptor of the target method to be wrapped
     */
    String targetDesc();


    /**
     * The slice of the target method to be wrapped.
     * @return the slice of the target method to be wrapped
     */
    Slice slice() default @Slice();
}
