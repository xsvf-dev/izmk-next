package ovo.xsvf.patchify.annotation;

import ovo.xsvf.patchify.CallbackInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Pass the local variable with specific value to the method.
 * <p>
 *     The local variable should be stored before the inject point.
 *     <br/>
 *     The local parameter should be after the original parameters and before the {@link CallbackInfo} parameter.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Local {
    /**
     * The index of the local variable.
     * <p>
     *     <b>Note:</b> The index starts from 0. If the index is out of range or the local variable is not initialized,
     *     an exception will be thrown.
     * </p>
     * @return the index of the local variable.
     */
    int value();
}
