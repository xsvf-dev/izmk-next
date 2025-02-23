package ovo.xsvf.patchify.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indictates the injection point of the method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
public @interface At {
    /**
     * The type of the injection point.
     * @return the type of the injection point
     */
    Type value() default Type.HEAD;

    /**
     * The method name of the method to be invoked.
     * <p>Only works with the {@link Type#BEFORE_INVOKE} and {@link Type#AFTER_INVOKE} types.</p>
     * @return the method name
     */
    String method() default "";

    /**
     * The method descriptor of the method to be invoked.
     * <p>Only works with the {@link Type#BEFORE_INVOKE} and {@link Type#AFTER_INVOKE} types.</p>
     * @return the method name
     */
    String desc() default "";


    enum Type {
        /**
         * Inject the method before a invocation.
         */
        BEFORE_INVOKE,

        /**
         * Inject the method after a invocation.
         */
        AFTER_INVOKE,

        /**
         * Inject the method at the beginning of the method.
         */
        HEAD,

        /**
         * Inject the method before all return statements.
         */
        TAIL
    }
}
