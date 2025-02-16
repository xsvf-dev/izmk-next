package ovo.xsvf.izmk.injection.mixin;

import org.jetbrains.annotations.NotNull;
import ovo.xsvf.izmk.injection.MethodHelper;
import ovo.xsvf.izmk.injection.mixin.api.Invocation;


public class VirtualInvocation implements Invocation {
    private final Object methodInstance;
    private final MethodHelper helper;
    private final String owner;
    private final String name;
    private final String desc;

    private VirtualInvocation(Object methodInstance, MethodHelper helper, String owner, String name, String desc) {
        this.methodInstance = methodInstance;
        this.helper = helper;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    /**
     * Factory method to create a new virtual method invocation instance.
     * <p>
     *     <b>Note:</b> The owner, name and desc should be already remapped.
     * </p>
     * @param helper the method helper, with all arguments
     * @param owner the owner class of the method
     * @param name the name of the method
     * @param desc the descriptor of the method
     * @return a new virtual method invocation instance
     */
    public static VirtualInvocation create(@NotNull Object methodInstance, MethodHelper helper, String owner, String name, String desc) {
        return new VirtualInvocation(methodInstance, helper, owner, name, desc);
    }

    @Override
    public Object call() {
        return helper.call(methodInstance, owner, name, desc);
    }

    @Override
    public Object[] getArgs() {
        Object[] args = helper.getMethodParams().toArray(new Object[0]);
        Object[] result = new Object[args.length];
        for (int i = args.length - 1,j = 0; i >= 0; i--, j++) result[j] = args[i];
        return result;
    }

    @Override
    public void setArg(int index, Object value) {
        helper.getMethodParams().set(index, value);
    }
}
