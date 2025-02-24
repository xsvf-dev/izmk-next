package ovo.xsvf.patchify;

import it.unimi.dsi.fastutil.Pair;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.patchify.annotation.*;
import ovo.xsvf.patchify.api.ILocals;
import ovo.xsvf.patchify.api.IPatchLoader;
import ovo.xsvf.patchify.api.Invocation;
import ovo.xsvf.patchify.asm.InvocationImpl;
import ovo.xsvf.patchify.asm.Locals;
import ovo.xsvf.patchify.asm.MethodWrapper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PatchLoader implements IPatchLoader {
    private final Consumer<String> debug;
    private final Consumer<String> info;
    private final Consumer<String> warn;

    public PatchLoader(Consumer<String> debug, Consumer<String> info, Consumer<String> warn) {
        this.debug = debug;
        this.info = info;
        this.warn = warn;
    }

    public PatchLoader() {
        this((s) -> {}, (s) -> {}, (s) -> {});
    }

    private static Inject getInject(Class<?> patchClass, Method method) {
        Inject inject = method.getAnnotation(Inject.class);

        // the method should be static
        if (!(method.getReturnType() == void.class))
            throw new IllegalArgumentException("inject method " + method.getName() + " in class " + patchClass.getName() + " should return void");
        Parameter[] params = method.getParameters();
        if (params[params.length - 1].getType() != CallbackInfo.class)
            throw new IllegalArgumentException("inject method " + method.getName() + " in class " + patchClass.getName() + " does not have a CallbackInfo parameter");
        if (inject.at().value() == At.Type.HEAD) {
            for (Parameter param : method.getParameters()) {
                if (param.isAnnotationPresent(Local.class))
                    throw new IllegalArgumentException("inject method " + method.getName() + " in class " + patchClass.getName() + " has a @Local parameter with HEAD inject point");
            }
        }

        return inject;
    }

    private static boolean swap(Type type, InsnList insnList) {
        // do swap
        if (type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE) {
            // STACK: [?, {arg1, arg2}]
            insnList.add(new InsnNode(Opcodes.DUP2_X1));
            // STACK: [{arg1, arg2}, ?, {arg1, arg2}]
            insnList.add(new InsnNode(Opcodes.POP2));
            // STACK: [{arg1, arg2}, ?]
            return true; // long and double take two slots
        } else {
            insnList.add(new InsnNode(Opcodes.SWAP));
            // STACK: [arg, ?]
            return false;
        }
    }

    private static List<AbstractInsnNode> getInjectionPoints(InsnList insnList, Slice slice, Predicate<AbstractInsnNode> filter) {
        final List<AbstractInsnNode> injectionPoints = new ArrayList<>();
        if (slice.start().value() == At.Type.HEAD && slice.end().value() == At.Type.TAIL && slice.startIndex() == -1 && slice.endIndex() == -1) {
            IZMK.INSTANCE.getLogger().debug("head-tail slice injection point found!");
            injectionPoints.addAll(Arrays.stream(insnList.toArray()).filter(filter).toList());
        } else if (slice.startIndex() != -1 || slice.endIndex() != -1) {
            // This is an index-based slice
            int count = 0;
            int endIndex = (slice.end().value() == At.Type.TAIL && slice.endIndex() == -1)
                    ? insnList.size() : slice.endIndex();
            for (AbstractInsnNode insnNode : insnList) {
                if (filter.test(insnNode)) {
                    count++;
                    IZMK.INSTANCE.getLogger().debug("index-based slice match found!, count = " + count);
                    if (count >= slice.startIndex() && count <= endIndex) {
                        IZMK.INSTANCE.getLogger().debug("index-based slice injection point found!");
                        injectionPoints.add(insnNode);
                    } else if (count > endIndex) {
                        break;
                    }
                }
            }
        } else {
            IZMK.INSTANCE.getLogger().debug("method/head-tail before/after slice injection point found!");
            // this is a method-based slice
            boolean head = slice.start().value() == At.Type.HEAD;
            boolean tail = slice.end().value() == At.Type.TAIL;

            var startSplit = ASMUtil.splitDesc(slice.start().method());
            var endSplit = ASMUtil.splitDesc(slice.end().method());

            boolean foundStart = head;
            for (AbstractInsnNode insnNode : insnList) {
                if (!head && insnNode instanceof MethodInsnNode m && m.owner.equals(startSplit.first()) && m.name.equals(startSplit.second()) && m.desc.equals(startSplit.second())) {
                    foundStart = true;
                } else if (!tail && insnNode instanceof MethodInsnNode m && m.owner.equals(endSplit.first()) && m.name.equals(endSplit.second()) && m.desc.equals(endSplit.second())) {
                    break;
                }
                if (foundStart) {
                    IZMK.INSTANCE.getLogger().debug("method-based slice injection point found!");
                    injectionPoints.add(insnNode);
                }
            }
        }
        return injectionPoints;
    }

    private static void injectHead(MethodNode method, Method inject) {
        var returnType = Type.getReturnType(method.desc);
        var injectOwner = Type.getInternalName(inject.getDeclaringClass());
        var injectName = inject.getName();
        var injectDesc = Type.getMethodDescriptor(inject);


        InsnList insnList = new InsnList(); LabelNode labelNode = new LabelNode();
        int index = 0; int callbackIndex = method.maxLocals; method.maxLocals += 1;

        // pass all parameters to the inject method
        if (!Modifier.isStatic(method.access)) insnList.add(new VarInsnNode(Opcodes.ALOAD, index++)); // 0
        for (Type type : Type.getArgumentTypes(method.desc)) {
            insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index++));
            // long and double take two slots
            if (type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE) index += 1;
        }

        // There are no local variables stored, so we don't need to push them onto the stack

        // STACK: [<INSTANCE>, args...]
        insnList.add(new InsnNode(Opcodes.ACONST_NULL));
        // STACK: [<INSTANCE>, args..., null]
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CallbackInfo.class), "create", "(" + Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(CallbackInfo.class), false));
        // STACK: [<INSTANCE>, args..., CallbackInfo]
        insnList.add(new InsnNode(Opcodes.DUP));
        // STACK: [<INSTANCE>, args..., CallbackInfo, CallbackInfo]
        insnList.add(new VarInsnNode(Opcodes.ASTORE, callbackIndex));
        // STACK: [<INSTANCE>, args..., CallbackInfo]
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, injectOwner, injectName, injectDesc, false));
        // STACK: []
        insnList.add(new VarInsnNode(Opcodes.ALOAD, callbackIndex));
        // STACK: [CallbackInfo]
        insnList.add(new InsnNode(Opcodes.DUP));
        // STACK: [CallbackInfo, CallbackInfo]
        insnList.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getInternalName(CallbackInfo.class), "cancelled", "Z"));
        // STACK: [CallbackInfo, isCancelled]
        insnList.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));
        // STACK: [CallbackInfo]

        if (returnType == Type.VOID_TYPE) {
            insnList.add(new InsnNode(Opcodes.POP));
            insnList.add(new InsnNode(Opcodes.RETURN));
        } else {
            insnList.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getInternalName(CallbackInfo.class), "result", Type.getDescriptor(Object.class)));
            // STACK: [result]
            insnList.add(ASMUtil.checkcastFromObject(returnType));
            insnList.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));
        }
        insnList.add(labelNode);
        // STACK: [CallbackInfo]
        insnList.add(new InsnNode(Opcodes.POP));

        method.instructions.insert(insnList);
    }

    private static void injectTail(MethodNode method, Method inject) {
        Type returnType = Type.getReturnType(method.desc);
        var injectOwner = Type.getInternalName(inject.getDeclaringClass());
        var injectName = inject.getName();
        var injectDesc = Type.getMethodDescriptor(inject);

        int retOpCode = returnType.getOpcode(Opcodes.IRETURN);
        List<AbstractInsnNode> toInsert = getInjectionPoints(method.instructions,
                inject.getDeclaredAnnotation(Inject.class).slice(),
                insnNode -> insnNode.getOpcode() == retOpCode);

        for (AbstractInsnNode insnNode : toInsert) {
            InsnList insnList = new InsnList();
            // STACK: [result]
            if (returnType == Type.VOID_TYPE) {
                insnList.add(new InsnNode(Opcodes.ACONST_NULL));
            } else {
                insnList.add(ASMUtil.checkcastToObject(returnType));
            }
            // STACK: [result(Object)]
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CallbackInfo.class), "create", "(" + Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(CallbackInfo.class), false));
            // STACK: [CallbackInfo]
            // now, load all the params, and pass them to the inject method
            int index = 0;
            int callbackIndex = method.maxLocals;
            method.maxLocals += 1;

            if (!Modifier.isStatic(method.access)) {
                insnList.add(new VarInsnNode(Opcodes.ALOAD, index++)); // 0
                // STACK: [CallbackInfo, <INSTANCE>]
                insnList.add(new InsnNode(Opcodes.SWAP));
                // STACK: [<INSTANCE>, CallbackInfo]
            }

            for (Type type : Type.getArgumentTypes(method.desc)) {
                insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index++));
                // STACK: [<INSTANCE>, CallbackInfo, arg]
                index += swap(type, insnList) ? 1 : 0;
                // STACK: [<INSTANCE>, arg, CallbackInfo]
            }

            // pass the params with @Local to the inject method
            for (Parameter param : inject.getParameters()) {
                if (param.isAnnotationPresent(Local.class)) {
                    Local local = param.getAnnotation(Local.class);
                    if (local.value() >= method.maxLocals)
                        throw new IllegalArgumentException("inject method " + inject.getName() + " in class " + inject.getDeclaringClass().getName() + " has a @Local parameter with an invalid value");

                    Type type = Type.getType(param.getType());
                    insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), local.value()));
                    swap(type, insnList);
                }
            }

            // STACK: [<INSTANCE>, args..., CallbackInfo]
            insnList.add(new InsnNode(Opcodes.DUP));
            // STACK: [<INSTANCE>, args..., CallbackInfo, CallbackInfo]
            insnList.add(new VarInsnNode(Opcodes.ASTORE, callbackIndex));
            // STACK: [<INSTANCE>, args..., CallbackInfo]
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, injectOwner, injectName, injectDesc, false));
            // STACK: []
            insnList.add(new VarInsnNode(Opcodes.ALOAD, callbackIndex));
            // STACK: [CallbackInfo]
            insnList.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getInternalName(CallbackInfo.class), "result", Type.getDescriptor(Object.class)));
            // STACK: [result(Object)]
            if (returnType == Type.VOID_TYPE) {
                // STACK: [null]
                insnList.add(new InsnNode(Opcodes.POP));
            } else {
                insnList.add(ASMUtil.checkcastFromObject(returnType));
            }

            method.instructions.insertBefore(insnNode, insnList);
        }
    }

    private static void injectMethod(MethodNode method, Method inject, Pair<String, String> invoke, boolean isBefore) {
        var returnType = Type.getReturnType(method.desc);
        var injectOwner = Type.getInternalName(inject.getDeclaringClass());
        var injectName = inject.getName();
        var injectDesc = Type.getMethodDescriptor(inject);

        var split = ASMUtil.splitDesc(invoke.first());

        List<AbstractInsnNode> toInject = getInjectionPoints(method.instructions,
                inject.getDeclaredAnnotation(Inject.class).slice(),
                insnNode -> insnNode instanceof MethodInsnNode m && m.owner.equals(split.first()) &&
                        m.name.equals(split.second()) && m.desc.equals(invoke.second()));

        if (toInject.isEmpty()) {
            IZMK.INSTANCE.getLogger().warn("method invocation with name = {}, desc = {} cannot be found in target method", invoke.first(), invoke.second());
            return;
        }

        // check local variables initialization
        Map<Integer, Boolean> locals = new HashMap<>();
        for (AbstractInsnNode insnNode : method.instructions) {
            if (insnNode == toInject.getFirst()) break;
            if (insnNode instanceof VarInsnNode var && var.getOpcode() >= Opcodes.ISTORE && var.getOpcode() <= Opcodes.ASTORE) {
                locals.put(var.var, true);
            }
        }

        int callbackIndex = method.maxLocals; method.maxLocals += 1;

        for (AbstractInsnNode insnNode : toInject) {
            InsnList insnList = new InsnList(); LabelNode labelNode = new LabelNode();
            int index = 0;

            // STACK: (before) [margs...] (after) [<ret_value>]
            if (!Modifier.isStatic(method.access)) {
                insnList.add(new VarInsnNode(Opcodes.ALOAD, index++)); // 0
                // STACK: (before) [margs..., <INSTANCE>] (after) [<ret_value>, <INSTANCE>]
            }

            for (Type type : Type.getArgumentTypes(method.desc)) {
                insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index++));
                if (type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE) index++;
                // STACK: (before) [margs..., <instane>, arg] (after) [<ret_value>, <instace>, arg]
            }
            // STACK: (before) [margs..., <instane>, args...] (after) [<ret_value>, <instace>, args...]

            // pass the params with @Local to the inject method
            for (Parameter param : inject.getParameters()) {
                if (param.isAnnotationPresent(Local.class)) {
                    Local local = param.getAnnotation(Local.class);
                    if (local.value() >= method.maxLocals)
                        throw new IllegalArgumentException("inject method " + inject.getName() + " in class " + inject.getDeclaringClass().getName() + " has a @Local parameter with an invalid index");
                    if (!locals.containsKey(local.value()) || !locals.get(local.value()))
                        throw new IllegalArgumentException("inject method " + inject.getName() + " in class " + inject.getDeclaringClass().getName() + " has a @Local parameter with an index that might be not initialized in the target method");
                    Type type = Type.getType(param.getType());
                    insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), local.value()));
                }
            }

            insnList.add(new InsnNode(Opcodes.ACONST_NULL));
            // STACK: (before) [margs..., <instane>, args..., null] (after) [<ret_value>, <instace>, args..., null]
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(CallbackInfo.class), "create", "(" + Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(CallbackInfo.class), false));
            // STACK: (before) [margs..., <instane>, args..., CallbackInfo] (after) [<ret_value>, <instace>, args..., CallbackInfo]
            insnList.add(new InsnNode(Opcodes.DUP));
            // STACK: (before) [margs..., <instane>, args..., CallbackInfo, CallbackInfo] (after) [<ret_value>, <instace>, args..., CallbackInfo, CallbackInfo]
            insnList.add(new VarInsnNode(Opcodes.ASTORE, callbackIndex));
            // STACK: (before) [margs..., <instane>, args..., CallbackInfo] (after) [<ret_value>, <instace>, args..., CallbackInfo]
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, injectOwner, injectName, injectDesc, false));
            // STACK: (before) [margs...] (after) [<ret_value>]
            insnList.add(new VarInsnNode(Opcodes.ALOAD, callbackIndex));
            // STACK: (before) [margs..., CallbackInfo] (after) [<ret_value>, CallbackInfo]
            insnList.add(new InsnNode(Opcodes.DUP));
            // STACK: (before) [margs..., CallbackInfo, CallbackInfo] (after) [<ret_value>, CallbackInfo, CallbackInfo]
            insnList.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getInternalName(CallbackInfo.class), "cancelled", Type.getDescriptor(boolean.class)));
            // STACK: (before) [margs..., CallbackInfo, isCancelled] (after) [<ret_value>, CallbackInfo, isCancelled]
            insnList.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));

            // STACK: (before) [margs..., CallbackInfo] (after) [<ret_value>, CallbackInfo]
            insnList.add(new FieldInsnNode(Opcodes.GETFIELD, Type.getInternalName(CallbackInfo.class), "result", Type.getDescriptor(Object.class)));
            // STACK: (before) [margs..., result(Object)] (after) [<ret_value>, result(Object)]
            if (returnType != Type.VOID_TYPE) {
                insnList.add(new InsnNode(Opcodes.POP));
            } else {
                insnList.add(ASMUtil.checkcastFromObject(returnType));
            }
            // STACK: [<result>]
            insnList.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));

            insnList.add(labelNode);
            // STACK: (before) [margs..., CallbackInfo] (after) [<ret_value>, CallbackInfo]
            insnList.add(new InsnNode(Opcodes.POP));
            // STACK: (before) [margs...] (after) [<ret_value>]

            if (isBefore) {
                method.instructions.insertBefore(insnNode, insnList);
            } else {
                method.instructions.insert(insnNode, insnList);
            }
        }
    }

    private static void overwrite(MethodNode method, Method inject) {
        InsnList insnList = new InsnList();
        int index = 0;
        if (!Modifier.isStatic(method.access)) {
            insnList.add(new VarInsnNode(Opcodes.ALOAD, index++)); // 0
            // STACK: [<INSTANCE>]
        }

        for (Type type : Type.getArgumentTypes(method.desc)) {
            insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index++));
            // STACK: [<INSTANCE>, arg]
        }
        // STACK: [<INSTANCE>, args...]
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(inject.getDeclaringClass()), inject.getName(), Type.getMethodDescriptor(inject), false));
        // STACK: [<result>]
        if (Type.getReturnType(method.desc) == Type.VOID_TYPE) {
            insnList.add(new InsnNode(Opcodes.RETURN));
        } else {
            insnList.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN)));
        }
        method.instructions.insert(insnList);
    }

    private static void wrapInvoke(MethodNode method, Method inject) {
        var injectOwner = Type.getInternalName(inject.getDeclaringClass());
        var injectName = inject.getName();
        var injectDesc = Type.getMethodDescriptor(inject);

        WrapInvoke wrapInvoke = inject.getDeclaredAnnotation(WrapInvoke.class);
        var wrap = Pair.of(wrapInvoke.target(), wrapInvoke.targetDesc());
        var split = ASMUtil.splitDesc(wrap.first());

        List<AbstractInsnNode> toWrap = getInjectionPoints(method.instructions,
                wrapInvoke.slice(),
                insnNode -> insnNode instanceof MethodInsnNode m && m.owner.equals(split.first()) &&
                        m.name.equals(split.second()) && m.desc.equals(wrap.second()));

        if (toWrap.isEmpty()) {
            IZMK.INSTANCE.getLogger().warn("method invocation with name = {}, desc = {} cannot be found in target method", wrap.first(), wrap.second());
            return;
        }

        // check local variables initialization
        Map<Integer, Boolean> locals = new HashMap<>();
        for (AbstractInsnNode insnNode : method.instructions) {
            if (insnNode == toWrap.getFirst()) break;
            if (insnNode instanceof VarInsnNode var && var.getOpcode() >= Opcodes.ISTORE && var.getOpcode() <= Opcodes.ASTORE) {
                locals.put(var.var, true);
            }
        }

        for (AbstractInsnNode insnNode : toWrap) {
            InsnList insnList = new InsnList();
            // STACK: [<INSTANCE>, margs...]
            boolean staticCall; int helperIndex = method.maxLocals; method.maxLocals += 1;
            if (toWrap.getFirst().getOpcode() == Opcodes.INVOKESTATIC) {
                staticCall = true;
            } else if (toWrap.getFirst().getOpcode() == Opcodes.INVOKEVIRTUAL) {
                staticCall = false;
            } else {
                throw new IllegalArgumentException("Unsupported method call for " + toWrap.getFirst() + ", wrapper method: " + inject.getName() + " in class " + inject.getDeclaringClass().getName());
            }
            // STACK: [<mInstance>, margs...]
            insnList.add(new LdcInsnNode(split.first()));
            insnList.add(new LdcInsnNode(split.second()));
            insnList.add(new LdcInsnNode(wrap.second()));
            // STACK: [<mInstance>, margs..., owner, name, desc]
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(MethodWrapper.class), "getInstance", "(" + Type.getDescriptor(String.class) + Type.getDescriptor(String.class) + Type.getDescriptor(String.class) + ")" + Type.getDescriptor(MethodWrapper.class), false));
            // STACK: [<mInstance>, margs..., MethodWrapper]
            insnList.add(new VarInsnNode(Opcodes.ASTORE, helperIndex));
            // STACK: [<mInstance>, margs...]
            Type[] argTypes = Type.getArgumentTypes(wrap.second());
            for (int i = argTypes.length - 1; i >= 0; i--) {
                // STACK: [<mInstance>, margs..., arg]
                insnList.add(ASMUtil.checkcastToObject(argTypes[i]));
                // STACK: [<mInstance>, margs..., (Object)arg]
                insnList.add(new VarInsnNode(Opcodes.ALOAD, helperIndex));
                // STACK: [<mInstance>, margs..., (Object)arg, MethodWrapper]
                insnList.add(new InsnNode(Opcodes.SWAP));
                // STACK: [<mInstance>, margs..., MethodWrapper, (Object)arg]
                insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(MethodWrapper.class), "addParam", "("+ Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(MethodWrapper.class), false));
                // STACK: [<mInstance>, margs..., MethodWrapper]
                insnList.add(new InsnNode(Opcodes.POP));
                // STACK: [<mInstance>, margs...]
            }
            // STACK: [<mInstance>]
            insnList.add(new VarInsnNode(Opcodes.ALOAD, helperIndex));
            // STACK: [<mInstance>, MethodWrapper]

            if (staticCall) {
                // STACK: [MethodWrapper]
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(InvocationImpl.class), "create", "(" + Type.getDescriptor(MethodWrapper.class) + ")" + Type.getDescriptor(InvocationImpl.class), false));
            } else {
                // STACK: [mInstance, MethodWrapper]
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(InvocationImpl.class), "create", "(" + Type.getDescriptor(Object.class) + Type.getDescriptor(MethodWrapper.class) + ")" + Type.getDescriptor(InvocationImpl.class), false));
            }
            // STACK: [Invocation]
            int index = 0;
            if (!Modifier.isStatic(method.access)) {
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new InsnNode(Opcodes.SWAP));
                index++;
            }
            // STACK: [<INSTANCE>, Invocation]
            for (Type type : Type.getArgumentTypes(method.desc)) {
                insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index++));
                index += swap(type, insnList) ? 1 : 0;
            }
            // STACK: [<INSTANCE>, args..., Invocation]
            // pass the params with @Local to the inject method
            for (Parameter param : inject.getParameters()) {
                if (param.isAnnotationPresent(Local.class)) {
                    Local local = param.getAnnotation(Local.class);
                    if (local.value() >= method.maxLocals)
                        throw new IllegalArgumentException("inject method " + inject.getName() + " in class " + inject.getDeclaringClass().getName() + " has a @Local parameter with an invalid index");
                    if (!locals.containsKey(local.value()) || !locals.get(local.value()))
                        throw new IllegalArgumentException("inject method " + inject.getName() + " in class " + inject.getDeclaringClass().getName() + " has a @Local parameter with an index that might be not initialized in the target method");

                    Type type = Type.getType(param.getType());
                    insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), local.value()));
                    // STACK: [<INSTANCE>, args..., Invocation, local]
                    swap(type, insnList);
                    // STACK: [<INSTANCE>, args..., local, Invocation]
                }
            }
            // STACK: [<INSTANCE>, args..., locals..., Invocation]
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, injectOwner, injectName, injectDesc, false));
            // STACK: [<result>]

            method.instructions.insertBefore(insnNode, insnList);
            method.instructions.remove(insnNode);
        }
    }

    private static void modifyLocals(MethodNode method, Method inject) {
        var injectOwner = Type.getInternalName(inject.getDeclaringClass());
        var injectName = inject.getName();
        var injectDesc = Type.getMethodDescriptor(inject);

        ModifyLocals modifyLocals = inject.getDeclaredAnnotation(ModifyLocals.class);
        if (modifyLocals.indexes().length != modifyLocals.types().length) {
            throw new IllegalArgumentException("ModifyLocals method " + inject.getName() + " in class " + inject.getDeclaringClass().getName() + " has different length of value and types arrays");
        }
        List<Type> localTypes = Arrays.stream(modifyLocals.types()).map(Type::getType).toList();

        At at = modifyLocals.at(); AbstractInsnNode toInject = method.instructions.getFirst();
        if (at.value() != At.Type.HEAD) {
            Map<Integer, Boolean> locals = new HashMap<>();
            if (at.value() == At.Type.TAIL) {
                int ret = Type.getReturnType(method.desc).getSize();
                for (AbstractInsnNode insnNode : method.instructions) {
                    if (insnNode.getOpcode() == ret) {
                        toInject = insnNode;
                        break;
                    }
                    if (insnNode instanceof VarInsnNode v && v.getOpcode() >= Opcodes.ISTORE && v.getOpcode() <= Opcodes.ASTORE) {
                        locals.put(v.var, true);
                    }
                }
            } else {
                var target = Pair.of(at.method(), at.desc());
                var split = ASMUtil.splitDesc(target.first());
                for (AbstractInsnNode insnNode : method.instructions) {
                    if (insnNode instanceof MethodInsnNode m && m.owner.equals(split.first()) &&
                            m.name.equals(split.second()) && m.desc.equals(target.second())) {
                        toInject = insnNode;
                        break;
                    }
                    if (insnNode instanceof VarInsnNode v && v.getOpcode() >= Opcodes.ISTORE && v.getOpcode() <= Opcodes.ASTORE) {
                        locals.put(v.var, true);
                    }
                }
            }
            for (int i : modifyLocals.indexes()) {
                if (!locals.containsKey(i) || !locals.get(i)) {
                    throw new IllegalArgumentException("ModifyLocals method " + inject.getName() + " in class " + inject.getDeclaringClass().getName() + " has a local index " + i + " that might be not initialized in the target method");
                }
            }
        }

        InsnList insnList = new InsnList();
        // STACK: []
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Locals.class), "create", "()" + Type.getDescriptor(Locals.class), false));
        // STACK: [Args]
        for (int i = 0; i < modifyLocals.indexes().length; i++) {
            Type type = localTypes.get(i);
            int index = modifyLocals.indexes()[i];

            insnList.add(new LdcInsnNode(index));
            // STACK: [Args, index]
            insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index));
            // STACK: [Args, index, local]
            insnList.add(ASMUtil.checkcastToObject(type));
            // STACK: [Args, index, (Object)local]
            insnList.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(ILocals.class), "set", "(" + Type.getDescriptor(int.class) + Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(ILocals.class), true));
            // STACK: [Args]
        }
        // STACK: [Args]
        insnList.add(new InsnNode(Opcodes.DUP));
        // STACK: [Args, Args]
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, injectOwner, injectName, injectDesc, false));
        // STACK: [Args]
        for (int i = 0; i < modifyLocals.indexes().length; i++) {
            int index = modifyLocals.indexes()[i];
            Type type = localTypes.get(i);

            insnList.add(new InsnNode(Opcodes.DUP));
            // STACK: [Args, Args]
            insnList.add(new LdcInsnNode(index));
            // STACK: [Args, Args, index]
            insnList.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Type.getInternalName(ILocals.class), "get", "(" + Type.getDescriptor(int.class) + ")" + Type.getDescriptor(Object.class), true));
            // STACK: [Args, local]
            insnList.add(ASMUtil.checkcastFromObject(type));
            // STACK: [Args, (T)local]
            insnList.add(new VarInsnNode(type.getOpcode(Opcodes.ISTORE), index));
            // STACK: [Args]
        }
        // STACK: [Args]
        insnList.add(new InsnNode(Opcodes.POP));
        // STACK: []

        method.instructions.insertBefore(toInject, insnList);

        ASMUtil.printOpcodesWithParameters(method, System.out::println);
    }

    @Override
    public Pair<Class<?>, byte[]> patch(Function<Class<?>, byte[]> bytesProvider,
                                        Class<?> patchClass) throws Exception {
        if (!patchClass.isAnnotationPresent(Patch.class))
            throw new IllegalArgumentException("Class " + patchClass.getName() + " is not annotated with @Patch");

        IZMK.INSTANCE.getLogger().debug("loading patch {}", patchClass.getName());

        Class<?> targetClass = patchClass.getAnnotation(Patch.class).value();
        ClassNode targetNode = ASMUtil.node(bytesProvider.apply(targetClass));

        // key: the method to be injected, value: the method to be invoked by the injected method
        Map<Pair<String, String>, List<Method>> injectMap = new HashMap<>();

        for (Method method : patchClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Inject.class) || method.isAnnotationPresent(Overwrite.class) ||
                    method.isAnnotationPresent(Transform.class) || method.isAnnotationPresent(WrapInvoke.class) ||
                    method.isAnnotationPresent(ModifyLocals.class)) {
                if (!Modifier.isStatic(method.getModifiers()))
                    throw new IllegalArgumentException("inject method " + method.getName() + " in class " + patchClass.getName() + " is not static");
                if (Modifier.isPrivate(method.getModifiers())) 
                     throw new IllegalArgumentException("inject method " + method.getName() + " in class " + patchClass.getName() + " is private");
                Class<?>[] parameterTypes = method.getParameterTypes();
                String name; String desc;
                if (method.isAnnotationPresent(Inject.class)) {
                    Inject inject = getInject(patchClass, method);
                    name = inject.method();
                    desc = inject.desc().isEmpty() ? Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(method)) : inject.desc();
                } else if (method.isAnnotationPresent(Overwrite.class)) {
                    Overwrite overwrite = method.getAnnotation(Overwrite.class);
                    name = overwrite.method();
                    desc = overwrite.desc().isEmpty() ? Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(method)) : overwrite.desc();
                } else if (method.isAnnotationPresent(Transform.class)) {
                    if (method.getParameterCount() != 1 || !parameterTypes[0].equals(MethodNode.class))
                        throw new IllegalArgumentException("Transform method " + method.getName() + " in class " + patchClass.getName() + " must have one parameter of type MethodNode");
                    Transform transform = method.getAnnotation(Transform.class);
                    name = transform.method();
                    desc = transform.desc().isEmpty() ? Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(method)) : transform.desc();
                } else if (method.isAnnotationPresent(WrapInvoke.class)) {
                    if (method.getParameterCount() == 0 || !Invocation.class.isAssignableFrom(parameterTypes[parameterTypes.length - 1]))
                        throw new IllegalArgumentException("WrapInvoke method " + method.getName() + " in class " + patchClass.getName() + " must have at least one parameter of type Invocation as the last parameter");
                    WrapInvoke wrapInvoke = method.getAnnotation(WrapInvoke.class);
                    name = wrapInvoke.method();
                    desc = wrapInvoke.desc().isEmpty() ? Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(method)) : wrapInvoke.desc();
                } else if (method.isAnnotationPresent(ModifyLocals.class)) {
                    ModifyLocals modifyLocals = method.getAnnotation(ModifyLocals.class);
                    name = modifyLocals.method();
                    desc = modifyLocals.desc().isEmpty() ? Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(method)) : modifyLocals.desc();
                } else {
                    throw new RuntimeException("wtf");
                }
                Pair<String, String> target = Pair.of(targetNode.name + "/" + name, desc);
                injectMap.computeIfAbsent(target, k -> new ArrayList<>()).add(method);
            }
        }

        for (MethodNode method : targetNode.methods) {
            if (injectMap.containsKey(Pair.of(targetNode.name + "/" + method.name, method.desc))) {
                var injectMethods = injectMap.get(Pair.of(targetNode.name + "/" + method.name, method.desc));

                for (var injectMethod : injectMethods) {
                    IZMK.INSTANCE.getLogger().info("processing method " + method.name + " in class " + targetClass.getName() + " with patch method " + injectMethod.getName() + " in class " + patchClass.getName());
                    if (injectMethod.isAnnotationPresent(Inject.class)) {
                        At at = injectMethod.getAnnotation(Inject.class).at();
                        switch (at.value()) {
                            case HEAD -> {
                                if (Arrays.stream(injectMethod.getParameters()).anyMatch(it -> it.isAnnotationPresent(Local.class)))
                                    throw new IllegalArgumentException("Inject method " + injectMethod.getName() + " in class " + patchClass.getName() + " has a @Local parameter, which is not allowed in HEAD position");
                                injectHead(method, injectMethod);
                            }
                            case TAIL -> injectTail(method, injectMethod);
                            case BEFORE_INVOKE, AFTER_INVOKE -> {
                                if (at.method().isEmpty() || at.desc().isEmpty())
                                    throw new IllegalArgumentException("At annotation in method " + injectMethod.getName() + " in class " + patchClass.getName() + " is missing method or desc");
                                injectMethod(method, injectMethod, Pair.of(at.method(), at.desc()), at.value() == At.Type.BEFORE_INVOKE);
                            }
                        }
                    } else if (injectMethod.isAnnotationPresent(Overwrite.class)) {
                        overwrite(method, injectMethod);
                    } else if (injectMethod.isAnnotationPresent(Transform.class)) {
                        IZMK.INSTANCE.getLogger().debug("transforming method " + method.name + " in class " + targetClass.getName());
                        injectMethod.invoke(null, method);
                    } else if (injectMethod.isAnnotationPresent(WrapInvoke.class)) {
                        int index = Modifier.isStatic(method.access) ? 0 : 1;
                        Class<?>[] types = injectMethod.getParameterTypes();
                        if (index + 1 + Type.getArgumentTypes(method.desc).length > injectMethod.getParameterCount())
                            throw new IllegalArgumentException("WrapInvoke method " + injectMethod.getName() + " in class " + patchClass.getName() + " has less parameters than the target method");
                        if (!Modifier.isStatic(method.access) && !Type.getInternalName(types[0]).equals(targetNode.name))
                            throw new IllegalArgumentException("WrapInvoke method " + injectMethod.getName() + " in class " + patchClass.getName() + " has a different type of the first parameter than the target class");
                        wrapInvoke(method, injectMethod);
                    } else if (injectMethod.isAnnotationPresent(ModifyLocals.class)) {
                        if (injectMethod.getParameterCount() != 1 || !ILocals.class.isAssignableFrom(injectMethod.getParameterTypes()[0]))
                            throw new IllegalArgumentException("ModifyLocals method " + injectMethod.getName() + " in class " + patchClass.getName() + " must have one parameter of type ILocals");
                        modifyLocals(method, injectMethod);
                    }
                }
            }
        }

        return Pair.of(targetClass, ASMUtil.rewriteClass(targetNode));
    }

    @Override
    public void loadPatches(Collection<Class<?>> patchClasses,
                            Function<Class<?>, byte[]> bytesProvider,
                            BiConsumer<Class<?>, byte[]> classTransformer) throws Exception {
        for (Class<?> patchClass : patchClasses) {
            loadPatch(patchClass, bytesProvider, classTransformer);
            classTransformer.accept(patchClass, bytesProvider.apply(patchClass));
        }
    }
}
