package ovo.xsvf.patchify.asm;

import ovo.xsvf.patchify.api.Invocation;

import java.util.List;

public class InvocationImpl implements Invocation {
    private final Object optionalInstance;
    private final MethodWrapper wrapper;

    private InvocationImpl(Object optionalInstance, MethodWrapper wrapper) {
        this.optionalInstance = optionalInstance;
        this.wrapper = wrapper;
    }

    public static InvocationImpl create(Object instance, MethodWrapper wrapper) {
        return new InvocationImpl(instance, wrapper);
    }

    public static InvocationImpl create(MethodWrapper wrapper) {
        return new InvocationImpl(null, wrapper);
    }

    @Override
    public List<Object> args() {
        return wrapper.getMethodParams();
    }

    @Override
    public Object instance() {
        if (optionalInstance == null) {
            throw new IllegalStateException("static method invocation");
        }
        return optionalInstance;
    }

    @Override
    public Object call() throws Exception {
        return wrapper.call(optionalInstance);
    }
}
