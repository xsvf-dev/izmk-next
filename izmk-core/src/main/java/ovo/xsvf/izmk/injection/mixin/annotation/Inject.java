package ovo.xsvf.izmk.injection.mixin.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject a method into the target method.
 * <p>
 *     The class containing the method should be annotated with {@link Mixin}.
 *     <br/>
 *     The method should have {@link ovo.xsvf.izmk.injection.mixin.CallbackInfo} as its last parameter.
 *     <br/>
 *     If the target method is a non-static method, the inject method should have a instance of the target class as its first parameter.
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
    At at() default @At(value = ovo.xsvf.izmk.injection.mixin.annotation.At.Type.HEAD);

    /**
     * The slice to inject.
     * <p>
     *     <b>Note: </b> Does not work with {@link At.Type#HEAD}.
     * </p>
     * @return the slice to inject, defaults to the whole method
     */
    Slice slice() default @Slice(start = @At(value = ovo.xsvf.izmk.injection.mixin.annotation.At.Type.HEAD), end = @At(value = ovo.xsvf.izmk.injection.mixin.annotation.At.Type.TAIL));
}
