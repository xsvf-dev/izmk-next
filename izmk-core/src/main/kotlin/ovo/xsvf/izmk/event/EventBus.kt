package ovo.xsvf.izmk.event

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.command.CommandManager
import ovo.xsvf.izmk.gui.HUDManager
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    private val listenerEventMapping = ConcurrentHashMap<Any, Set<Pair<Class<out Event>, Method>>>()
    private val eventHandlers = ConcurrentHashMap<Class<out Event>, MutableList<Triple<Method, Int, Boolean>>>()
    private val registeredHandlers = ConcurrentHashMap.newKeySet<Any>()

    init {
        register(CommandManager)
        register(HUDManager)
    }

    fun register(listener: Any) {
        if (!registeredHandlers.add(listener)) return  // 防止重复注册

        val eventTypes = mutableSetOf<Pair<Class<out Event>, Method>>()

        listener.javaClass.declaredMethods.forEach { method ->
            val annotation = method.getAnnotation(EventListener::class.java) ?: return@forEach
            val params = method.parameterTypes
            if (params.size != 1 || !Event::class.java.isAssignableFrom(params[0])) return@forEach
            val eventType = params[0] as Class<out Event>

            val priority = annotation.priority
            val alwaysListening = annotation.alwaysListening

            eventHandlers.computeIfAbsent(eventType) { mutableListOf() }
                .add(Triple(method, priority, alwaysListening))

            eventTypes.add(Pair(eventType, method))
        }

        eventHandlers.forEach { (_, handlers) ->
            handlers.sortByDescending { it.second }
        }

        if (eventTypes.isNotEmpty()) {
            listenerEventMapping[listener] = eventTypes
        }
    }

    fun unregister(listener: Any) {
        if (!registeredHandlers.remove(listener)) return
        listenerEventMapping.remove(listener)
        eventHandlers.forEach { (_, handlers) ->
            handlers.removeIf { it.first.declaringClass == listener.javaClass }
        }
    }

    fun post(event: Event) {
        eventHandlers[event.javaClass]?.forEach { (method, _, alwaysListening) ->
            listenerEventMapping.forEach { (listener, events) ->
                if (events.any { it.first == event.javaClass }) {
                    if (event !is CancellableEvent || !event.isCancelled || alwaysListening) {
                        try {
                            method.isAccessible = true
                            method.invoke(listener, event)
                        } catch (e: Throwable) {
                            IZMK.logger.error("Error executing event: $event", e)
                        }
                    }
                }
            }
        }
    }
}
