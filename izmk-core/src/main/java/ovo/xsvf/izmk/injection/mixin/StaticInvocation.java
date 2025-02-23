package ovo.xsvf.izmk.injection.mixin;

import ovo.xsvf.izmk.injection.MethodHelper;
import ovo.xsvf.izmk.injection.mixin.annotation.WrapInvoke;
import ovo.xsvf.izmk.injection.mixin.api.Invocation;

/**
 * This class is used to represent a static method invocation.
 * <p>
 *     <b>Note:</b> All field and method names are hard-coded in the ASM code. DO NOT CHANGE THEM.
 * </p>
 * @see VirtualInvocation
 * @see WrapInvoke
 */
public class StaticInvocation implements Invocation {
    private final MethodHelper helper;
    private final String owner;
    private final String name;
    private final String desc;

    private final Object[] args;

    private StaticInvocation(MethodHelper helper, String owner, String name, String desc) {
        this.helper = helper;
        this.owner = owner;
        this.name = name;
        this.desc = desc;

        Object[] args = helper.getMethodParams().toArray(new Object[0]);
        this.args = new Object[args.length];
        for (int i = args.length - 1,j = 0; i >= 0; i--, j++) this.args[j] = args[i];
    }

    /**
     * Factory method to create a new StaticInvocation object.
     * @param helper the method helper, with all arguments
     * @param owner the owner class of the method
     * @param name the name of the method
     * @param desc the descriptor of the method
     * @return a new static method invocation INSTANCE
     */
    public static StaticInvocation create(MethodHelper helper, String owner, String name, String desc) {
        return new StaticInvocation(helper, owner, name, desc);
    }

    @Override
    public Object call() throws Exception {
        return helper.callStatic(owner, name, desc);
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public void setArg(int index, Object value) {
        helper.getMethodParams().set(index, value);
    }
}
