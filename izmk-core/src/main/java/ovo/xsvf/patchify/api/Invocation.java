package ovo.xsvf.patchify.api;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Interface for a method invocation.
 */
public interface Invocation extends Callable<Object> {
    /**
     * Gets the arguments of the method invocation.
     * @return the arguments of the method invocation.
     */
    List<Object> args();

    /**
     * Gets the instance of the object on which the method is invoked.
     * @return the instance of the object on which the method is invoked.
     */
    Object instance();
}
