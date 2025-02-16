package ovo.xsvf.izmk.module;

import ovo.xsvf.izmk.module.impl.render.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LangYa466
 * @since 2025/2/16
 */
public class ModuleManager {
    private static ModuleManager INSTANCE;

    public static ModuleManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ModuleManager();
        return INSTANCE;
    }

    public final Map<String, Module> modulesMap = new HashMap<>();

    public void init() {
        addModule(new Test());
    }

    private void addModule(Module module) {
        modulesMap.put(module.getName(), module);
    }
}
