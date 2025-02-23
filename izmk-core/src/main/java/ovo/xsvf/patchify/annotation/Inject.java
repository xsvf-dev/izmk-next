package ovo.xsvf.patchify.annotation;

import ovo.xsvf.patchify.CallbackInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject a method into the target method.
 * <p>
 *     The class containing the method should be annotated with {@link Patch}.
 *     <br/>
 *     The method should have {@link CallbackInfo} as its last parameter.
 *     <br/>
 *     If the target method is a non-static method, the inject method should have a INSTANCE of the target class as its first parameter.
 * </p>
 * <b> Note: </b> The method should be public and static.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Inject {
    String method();

    /**
     * The description of the target method.
     * @return the descriptor of the target method
     */
    String desc();

    /**
     * The injection point of the target method.
     * @return the injection point of the target method
     */
    At at() default @At(value = At.Type.HEAD);

    /**
     * The slice to inject.
     * <p>
     *     <b>Note: </b> Does not work with {@link At.Type#HEAD}.
     * </p>
     * @return the slice to inject, defaults to the whole method
     */
    Slice slice() default @Slice(start = @At(value = At.Type.HEAD), end = @At(value = At.Type.TAIL));
}
