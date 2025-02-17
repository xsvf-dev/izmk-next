package ovo.xsvf.izmk.module

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.module.impl.Test
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier

/**
 * @author LangYa466
 * @since 2025/2/16
 */
object ModuleManager {
    @JvmStatic
    val modulesMap: MutableMap<String, Module> = HashMap()

    @Throws(Throwable::class)
    fun init(classes: Array<Class<*>>) {
        for (clazz in classes) {
            if (clazz.superclass == Module::class.java && !Modifier.isAbstract(clazz.modifiers) &&
                !Modifier.isInterface(clazz.modifiers)
            ) {
                try {
                    val module = clazz.getConstructor().newInstance() as Module
                    addModule(module)
                } catch (e: NoSuchMethodException) {
                    IZMK.logger?.error("Module " + clazz.name + " does not have a default constructor.")
                    throw e
                } catch (e: IllegalAccessException) {
                    IZMK.logger?.error("Module " + clazz.name + " constructor is not accessible.")
                    throw e
                } catch (e: InstantiationException) {
                    IZMK.logger?.error("Module " + clazz.name + " cannot be instantiated.")
                    throw e
                } catch (e: InvocationTargetException) {
                    IZMK.logger?.error("Module " + clazz.name + " constructor throws an exception.")
                    throw e.targetException
                }
            }
        }
    }

    private fun addModule(module: Module) {
        modulesMap[module.name] = module
    }
}
