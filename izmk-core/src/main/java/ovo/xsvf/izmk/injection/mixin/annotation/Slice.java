package ovo.xsvf.izmk.injection.mixin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify the start and end points of a slice.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface Slice {
    At start() default @At(value = At.Type.HEAD);
    At end() default @At(value = At.Type.TAIL);

    int startIndex() default -1;
    int endIndex() default -1;
}
