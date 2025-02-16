package ovo.xsvf.izmk.injection.mixin.annotation;

import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transform the target method by this method.
 * <p>
 *     <b>Note:</b> Method should be public and static, and have one, and only one, parameter of type {@link MethodNode}.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transform {
    /**
     * The name of the target method.
     * @return the name of the target method
     */
    String method();

    /**
     * The description of the target method.
     * @return the descriptor of the target method
     */
    String desc();
}
