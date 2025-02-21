package ovo.xsvf.izmk.module

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.module.impl.Test
import java.lang.reflect.InvocationTargetException

object ModuleManager {
    val modulesMap = mutableMapOf<String, Module>()

    fun init(classes: Array<Class<*>>) {
        addModule(Test())

        for (clazz in classes) {
            if (Module::class.java.isAssignableFrom(clazz) && clazz != Module::class.java) {
                loadModule(clazz)
            }
        }
        IZMK.logger.info("Module map size: ${modulesMap.size}")
    }

    private fun loadModule(clazz: Class<*>) {
        val name = clazz.name
        IZMK.logger.info("Loading module: $name")
        try {
            val module = clazz.getDeclaredConstructor().newInstance() as Module
            addModule(module)
        } catch (e: Exception) {
            handleModuleLoadException(name, e)
        }
    }

    private fun addModule(module: Module) {
        modulesMap[module.name] = module
    }

    private fun handleModuleLoadException(name: String, e: Exception) {
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
