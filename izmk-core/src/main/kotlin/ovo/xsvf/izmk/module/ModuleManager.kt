package ovo.xsvf.izmk.module

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.module.impl.Test
import java.lang.reflect.InvocationTargetException

/**
 * @author LangYa466
 * @since 2025/2/16
 */
object ModuleManager {
    val modulesMap: MutableMap<String, Module> = HashMap()

    fun init(classes: Array<Class<*>>) {
        addModule(Test())
        if (true) return
        for (clazz in classes) {
            val name = clazz.name ?: "null"
            val superclass = clazz.superclass ?: "null"

            if (superclass == Module::class) {
                IZMK.logger.info("Loading module $name")
                try {
                    val module = clazz.getConstructor().newInstance() as Module
                    addModule(module)
                } catch (e: NoSuchMethodException) {
                    IZMK.logger.error("Module $name does not have a default constructor.")
                    throw e
                } catch (e: IllegalAccessException) {
                    IZMK.logger.error("Module $name constructor is not accessible.")
                    throw e
                } catch (e: InstantiationException) {
                    IZMK.logger.error("Module $name cannot be instantiated.")
                    throw e
                } catch (e: InvocationTargetException) {
                    IZMK.logger.error("Module $name constructor throws an exception.")
                    throw e.targetException
                }
            }
        }
        IZMK.logger.info("Module map size ${modulesMap.size}")
    }

    private fun addModule(module: Module) {
        modulesMap[module.name] = module
    }
}
