package ovo.xsvf.izmk.event

import ovo.xsvf.izmk.command.CommandManager
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArraySet

object EventBus {
    private val registeredHandlers = CopyOnWriteArraySet<Any>()

    init {
        register(CommandManager)
    }

    fun register(listener: Any) {
        registeredHandlers.add(listener)
    }

    fun unregister(listener: Any) {
        registeredHandlers.remove(listener)
    }

    fun post(event: Event) {
        val eventType = event::class.java
        val methods = mutableListOf<Pair<Method, Any>>()

        // Collect all methods annotated with @EventListener that accept the event type.
        for (listener in registeredHandlers) {
            for (method in listener::class.java.declaredMethods) {
                if (method.isAnnotationPresent(EventListener::class.java)) {
                    val parameterTypes = method.parameterTypes
                    if (parameterTypes.size == 1 && parameterTypes[0].isAssignableFrom(eventType)) {
                        methods.add(method to listener)
                    }
                }
            }
        }

        // Sort the methods so that those with higher priority are invoked first.
        methods.sortByDescending { (method, _) ->
            method.getAnnotation(EventListener::class.java).priority
        }

        // Invoke each method. If the event is cancellable and cancelled, skip methods unless alwaysListening is true.
        for ((method, listener) in methods) {
            val annotation = method.getAnnotation(EventListener::class.java)
            if (event is CancellableEvent && event.isCancelled && !annotation.alwaysListening) {
                continue
            }
            try {
                method.isAccessible = true
                method.invoke(listener, event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
