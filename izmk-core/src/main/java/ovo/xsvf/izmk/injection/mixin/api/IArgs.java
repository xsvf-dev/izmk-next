package ovo.xsvf.izmk.injection.mixin.api;

/**
 * Interface for accessing the arguments of a method.
 */
public interface IArgs {
    /**
     * Get the number of arguments.
     * @param index the index of the argument to get.
     * @return the number of arguments.
     */
    Object get(int index);

    /**
     * Set the value of an argument.
     * @param index the index of the argument to set.
     * @param value the value to set.
     */
    IArgs set(int index, Object value);
}
