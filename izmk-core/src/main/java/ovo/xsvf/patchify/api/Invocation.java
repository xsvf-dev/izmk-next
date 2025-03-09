package ovo.xsvf.patchify.api;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Interface for a method invocation.
 * @param <T> the type of the object on which the method is invoked.
 * @param <R> the return type of the method.
 */
public interface Invocation<T, R> extends Callable<R> {
    /**
     * Gets the arguments of the method invocation.
     * @return the arguments of the method invocation.
     */
    List<Object> args();

    /**
     * Gets the instance of the object on which the method is invoked.
     * @return the instance of the object on which the method is invoked.
     */
    T instance();
}
