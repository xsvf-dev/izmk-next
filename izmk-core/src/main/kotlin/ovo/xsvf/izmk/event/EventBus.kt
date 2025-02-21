package ovo.xsvf.izmk.event

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.command.CommandManager
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
        val methods = registeredHandlers.flatMap { listener ->
            listener::class.java.declaredMethods
                .filter { it.isAnnotationPresent(EventListener::class.java) }
                .filter { it.parameterTypes.singleOrNull()?.isAssignableFrom(eventType) == true }
                .map { it to listener }
        }.sortedByDescending { (method, _) ->
            method.getAnnotation(EventListener::class.java).priority
        }

        for ((method, listener) in methods) {
            val annotation = method.getAnnotation(EventListener::class.java)
            if (event is CancellableEvent && event.isCancelled && !annotation.alwaysListening) continue
            try {
                method.isAccessible = true
                method.invoke(listener, event)
            } catch (e: Exception) {
                IZMK.logger.error("Error while handling event ${event.javaClass.simpleName}", e)
            }
        }
    }
}