package ovo.xsvf.patchify.asm;

import org.jetbrains.annotations.NotNull;
import ovo.xsvf.patchify.api.Invocation;


public class VirtualInvocation implements Invocation {
    private final Object methodInstance;
    private final MethodHelper helper;
    private final String owner;
    private final String name;
    private final String desc;

    private final Object[] args;

    private VirtualInvocation(Object methodInstance, MethodHelper helper, String owner, String name, String desc) {
        this.methodInstance = methodInstance;
        this.helper = helper;
        this.owner = owner;
        this.name = name;
        this.desc = desc;

        Object[] args = helper.getMethodParams().toArray(new Object[0]);
        this.args = new Object[args.length];
        for (int i = args.length - 1,j = 0; i >= 0; i--, j++) this.args[j] = args[i];
    }

    /**
     * Factory method to create a new virtual method invocation INSTANCE.
     * <p>
     *     <b>Note:</b> The owner, name and desc should be already remapped.
     * </p>
     * @param helper the method helper, with all arguments
     * @param owner the owner class of the method
     * @param name the name of the method
     * @param desc the descriptor of the method
     * @return a new virtual method invocation INSTANCE
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
        return args;
    }

    @Override
    public void setArg(int index, Object value) {
        helper.getMethodParams().set(index, value);
    }

    public Object getTargetInstance() {
        return methodInstance;
    }
}
