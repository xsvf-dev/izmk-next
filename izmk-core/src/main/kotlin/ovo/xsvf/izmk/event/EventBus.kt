package ovo.xsvf.izmk.event

import java.util.concurrent.CopyOnWriteArraySet

object EventBus {
    private val registeredHandlers = CopyOnWriteArraySet<Any>()

    fun register(listener: Any) {
        registeredHandlers.add(listener)
    }

    fun unregister(listener: Any) {
        registeredHandlers.remove(listener)
    }

    fun post(event: Event) {
        val eventType = event::class.java
        for (listener in registeredHandlers) {
            for (method in listener::class.java.declaredMethods) {
                if (method.isAnnotationPresent(EventListener::class.java)) {
                    val parameterTypes = method.parameterTypes
                    if (parameterTypes.size == 1 && parameterTypes[0].isAssignableFrom(eventType)) {
                        try {
                            method.isAccessible = true
                            method.invoke(listener, event)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}
