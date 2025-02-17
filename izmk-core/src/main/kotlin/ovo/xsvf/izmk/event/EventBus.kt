package ovo.xsvf.izmk.event

import ovo.xsvf.izmk.event.annotations.EventPriority
import ovo.xsvf.izmk.event.annotations.EventTarget
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author LangYa466
 * @since 2025/2/16
 */
object EventBus {
    private val eventMethods = ConcurrentHashMap<Class<out Event>, MutableList<MethodWrapper>>()

    /** 注册事件监听对象 */
    fun register(vararg objs: Any) = objs.forEach(::register)

    private fun register(obj: Any) {
        obj.javaClass.declaredMethods
            .filter(::isValidEventMethod)
            .forEach { registerMethod(obj, it) }
    }

    private fun isValidEventMethod(method: Method): Boolean {
        return method.isAnnotationPresent(EventTarget::class.java) && method.parameterCount == 1
    }

    private fun registerMethod(obj: Any, method: Method) {
        val eventClass = method.parameterTypes[0] as Class<out Event>
        val priority = method.getAnnotation(EventPriority::class.java)?.value ?: 10
        eventMethods.getOrPut(eventClass) { CopyOnWriteArrayList() }
            .add(MethodWrapper(obj, method, priority))
    }

    /** 取消注册事件监听对象 */
    fun unregister(vararg objs: Any) = objs.forEach(::unregister)

    private fun unregister(obj: Any) {
        obj.javaClass.declaredMethods
            .filter(::isValidEventMethod)
            .forEach { unregisterMethod(obj, it) }
    }

    private fun unregisterMethod(obj: Any, method: Method) {
        val eventClass = method.parameterTypes[0] as Class<out Event>
        eventMethods[eventClass]?.removeIf { it.matches(obj, method) }
    }

    /** 调用事件 */
    fun call(event: Event): Event {
        eventMethods[event.javaClass]
            ?.sortedBy { it.priority }
            ?.forEach { it.invoke(event) }
        return event
    }
}
