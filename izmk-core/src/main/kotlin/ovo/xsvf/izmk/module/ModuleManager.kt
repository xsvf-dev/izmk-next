package ovo.xsvf.izmk.module

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.module.impl.Test
import java.lang.reflect.InvocationTargetException

object ModuleManager {
    val modulesMap = mutableMapOf<String, Module>()

    fun init() {
        addModule(Test())

        IZMK.logger.info("Module map size: ${modulesMap.size}")
    }

    private fun loadModule(clazz: Class<*>) {
        val name = clazz.name
        IZMK.logger.info("Loading module: $name")
        kotlin.runCatching {
            addModule(clazz.getDeclaredConstructor().newInstance() as Module)
        }.onFailure { handleModuleLoadException(name, it) }
    }

    private fun addModule(module: Module) {
        modulesMap[module.name] = module
    }

    private fun handleModuleLoadException(name: String, e: Throwable) {
        val message = when (e) {
            is NoSuchMethodException -> "does not have a default constructor."
            is IllegalAccessException -> "constructor is not accessible."
            is InstantiationException -> "cannot be instantiated."
            is InvocationTargetException -> "constructor threw an exception: ${e.targetException.message}"
            else -> "unexpected error occurred."
        }
        IZMK.logger.error("Module $name $message", e)
        throw e
    }
}
