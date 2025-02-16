package ovo.xsvf.izmk.event


import ovo.xsvf.izmk.event.annotations.EventPriority
import ovo.xsvf.izmk.event.annotations.EventTarget
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author LangYa466
 * @since 2025/2/16
 */
object EventBus {
    private val eventMethods: MutableMap<Class<out Event>, MutableList<MethodWrapper>> = ConcurrentHashMap()

    /** 注册事件监听对象  */
    fun register(vararg objs: Any) {
        for (obj in objs) register(obj)
    }

    private fun register(obj: Any) {
        Arrays.stream(obj.javaClass.declaredMethods)
            .filter { method: Method -> isValidEventMethod(method) }
            .forEach { method: Method -> registerMethod(obj, method) }
    }

    private fun isValidEventMethod(method: Method): Boolean {
        return method.isAnnotationPresent(EventTarget::class.java) && method.parameterCount == 1
                && method.parameterTypes[0].isInterface && Event::class.java.isAssignableFrom(method.parameterTypes[0])
    }

    private fun registerMethod(obj: Any, method: Method) {
        val eventClass = method.parameterTypes[0] as Class<out Event>
        val priority = Optional.ofNullable(
            method.getAnnotation(EventPriority::class.java)
        ).map { eventPriority: EventPriority -> eventPriority.value }.orElse(10)
        eventMethods.computeIfAbsent(eventClass) { CopyOnWriteArrayList() }
            .add(MethodWrapper(obj, method, priority))
    }

    /** 取消注册事件监听对象  */
    fun unregister(obj: Any) {
        Arrays.stream(obj.javaClass.declaredMethods)
            .filter { method: Method -> isValidEventMethod(method) }
            .forEach { method: Method -> unregisterMethod(obj, method) }
    }

    private fun unregisterMethod(obj: Any, method: Method) {
        val methods = eventMethods[method.parameterTypes[0] as Class<out Event>]
        methods?.removeIf { wrapper: MethodWrapper -> wrapper.matches(obj, method) }
    }

    /** 调用事件  */
    fun call(event: Event): Event {
        val methods: List<MethodWrapper>? = eventMethods[event.javaClass]
        methods?.stream()
            ?.sorted(Comparator.comparingInt(MethodWrapper::priority))
            ?.forEach { wrapper: MethodWrapper ->
                wrapper.invoke(event)
            }
        return event
    }
}
