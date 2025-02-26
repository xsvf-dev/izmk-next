package ovo.xsvf.izmk.module

import ovo.xsvf.izmk.module.impl.*

object ModuleManager {
    val modulesMap = mutableMapOf<String, Module>()

    fun init() {
        addModule(NoHurtcam)
        addModule(FPSDisplay)
        addModule(RenderTest)
        addModule(OldAnimations)
        addModule(ClickGUI)
    }

    private fun addModule(module: Module) {
        modulesMap[module.name] = module
    }

    operator fun get(name: String): Module? {
        return modulesMap[name]
    }

    fun getOrThrow(name: String): Module {
        return modulesMap[name]!!
    }

    fun getOrElse(name: String, default: () -> Module): Module {
        return modulesMap.getOrDefault(name, default())
    }

    fun getOrElse(name: String, default: Module): Module {
        return modulesMap.getOrDefault(name, default)
    }

    inline fun <reified T: Module> get(): T {
        return modulesMap.values.first { it is T } as T
    }

    fun modules() : List<Module> {
        return modulesMap.values.toList()
    }
}
