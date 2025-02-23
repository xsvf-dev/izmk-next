package ovo.xsvf.patchify.api;

import java.util.concurrent.Callable;

/**
 * An interface for a callable object that represents an invocation of a method.
 * <p>
 *     <b>Note:</b> All the field and method names are hard-coded
 *     in the ASM code. DO NOT CHANGE THEM.
 * </p>
 */
public interface Invocation extends Callable<Object> {
    Object[] getArgs();
    void setArg(int index, Object value);
}
