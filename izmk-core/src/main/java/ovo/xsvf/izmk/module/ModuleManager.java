package ovo.xsvf.izmk.module;

import ovo.xsvf.izmk.misc.Constants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa466
 * @since 2025/2/16
 */
public class ModuleManager implements Constants {
    private static ModuleManager INSTANCE;

    public static ModuleManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ModuleManager();
        return INSTANCE;
    }

    public final Map<String, Module> modulesMap = new HashMap<>();

    public void init(Class<?>[] classes) throws Throwable {
        for (Class<?> clazz : classes) {
            if (clazz.getSuperclass() == Module.class && !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
                try {
                    Module module = (Module) clazz.getConstructor().newInstance();
                    addModule(module);
                } catch (NoSuchMethodException e) {
                    logger.error("Module " + clazz.getName() + " does not have a default constructor.");
                    throw e;
                } catch (IllegalAccessException e) {
                    logger.error("Module " + clazz.getName() + " constructor is not accessible.");
                    throw e;
                } catch (InstantiationException e) {
                    logger.error("Module " + clazz.getName() + " cannot be instantiated.");
                    throw e;
                } catch (InvocationTargetException e) {
                    logger.error("Module " + clazz.getName() + " constructor throws an exception.");
                    throw e.getTargetException();
                }
            }
        }
    }

    private void addModule(Module module) {
        modulesMap.put(module.getName(), module);
    }
}
