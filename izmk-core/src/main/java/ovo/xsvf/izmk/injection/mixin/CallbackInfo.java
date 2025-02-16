package ovo.xsvf.izmk.injection.mixin;

import lombok.AllArgsConstructor;

/**
 * Describes the information passed to a inject method.
 * <p>
 *     if {@code cancelled} was set to true, the method will return
 *     immediately, with {@code result}, without executing the rest of the code.
 * </p>
 * <b>Note:</b> All fields' and methods' names are hard-coded in the asm code.DO NOT CHANGE THEM.
 */
@AllArgsConstructor
public class CallbackInfo {
    public Object result;
    public boolean cancelled;

    public static CallbackInfo create(Object result) {
        return new CallbackInfo(result, false);
    }
}
