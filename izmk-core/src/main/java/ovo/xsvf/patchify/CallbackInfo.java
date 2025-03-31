package ovo.xsvf.patchify;

/**
 * Describes the information passed to a inject method.
 * <p>
 * if {@code cancelled} was set to true, the method will return
 * immediately, with {@code result}, without executing the rest of the code.
 * </p>
 * <b>Note:</b> All fields' and methods' names are hard-coded in the asm code.DO NOT CHANGE THEM.
 */
public final class CallbackInfo {
    public Object result;
    public boolean cancelled;

    public CallbackInfo(Object result, boolean cancelled) {
        this.result = result;
        this.cancelled = cancelled;
    }

    public static CallbackInfo create(Object result) {
        return new CallbackInfo(result, false);
    }

    public void cancel() {
        cancelled = true;
    }
}
