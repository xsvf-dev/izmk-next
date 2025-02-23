package ovo.xsvf.patchify.asm;

import ovo.xsvf.patchify.api.ILocals;

import java.util.HashMap;
import java.util.Map;

/**
 * A local list implementation that stores values in a map.
 */
public class Locals implements ILocals {
    private final Map<Integer, Object> locals = new HashMap<>();

    private Locals() {

    }

    /**
     * Creates a new local list.
     *
     * @return a new local list
     */
    public static Locals create() {
        return new Locals();
    }

    /**
     * Gets the local variable at the specified index.
     * <p>
     *     <b>Note:</b> If the index is not defined in the local list, an exception will be thrown.
     * </p>
     * @param index the index of the variable
     * @return the local variable
     */
    @Override
    public Object get(int index) {
        if (!locals.containsKey(index))
            throw new IllegalArgumentException("Index " + index + " is not defined in the local list");
        return locals.get(index);
    }

    /**
     * Sets the local variable at the specified index.
     * <p>
     *     <b>Note:</b> If the index is not defined in the local list, an exception will be thrown.
     * </p>
     * @param index the index of the variable
     * @param value the value to set
     */
    @Override
    public Locals set(int index, Object value) {
        locals.put(index, value);
        return this;
    }
}
