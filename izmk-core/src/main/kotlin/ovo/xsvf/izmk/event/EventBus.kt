package ovo.xsvf.izmk.event

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.command.CommandManager
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object EventBus {
    private val listenerEventMapping = ConcurrentHashMap<Any, Set<Class<out Event>>>()
    private val eventMethodCache = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<Triple<MethodHandle, Int, Boolean>>>()
    private val registeredHandlers = ConcurrentHashMap.newKeySet<Any>()

    init {
        register(CommandManager)
    }

    fun register(listener: Any) {
        if (!registeredHandlers.add(listener)) return

        val lookup = MethodHandles.lookup()
        val eventTypes = ConcurrentHashMap.newKeySet<Class<out Event>>()

        listener.javaClass.declaredMethods.forEach { method ->
            val annotation = method.getAnnotation(EventListener::class.java) ?: return@forEach
            val params = method.parameterTypes
            if (params.size != 1 || !Event::class.java.isAssignableFrom(params[0])) return@forEach
            val eventType = params[0] as Class<out Event>

            try {
                val handle = generateMethodHandle(lookup, method, listener)
                val priority = annotation.priority
                val alwaysListening = annotation.alwaysListening

                eventMethodCache.computeIfAbsent(eventType) { CopyOnWriteArrayList() }
                    .apply {
                        add(Triple(handle, priority, alwaysListening))
                        sortByDescending { it.second }
                    }

                eventTypes.add(eventType)
            } catch (e: Throwable) {
                IZMK.logger.error("Failed to register event method: ${method.name}", e)
            }
        }

        if (eventTypes.isNotEmpty()) {
            listenerEventMapping[listener] = eventTypes
        }
    }

    fun unregister(listener: Any) {
        if (!registeredHandlers.remove(listener)) return
        listenerEventMapping.remove(listener)?.forEach { eventType ->
            eventMethodCache[eventType]?.removeIf { it.first == listener }
        }
    }

    fun post(event: Event) {
        eventMethodCache[event.javaClass]?.forEach { (handle, _, alwaysListening) ->
            if (event !is CancellableEvent || !event.isCancelled || alwaysListening) {
                runCatching { handle.invoke(event) }
                    .onFailure { IZMK.logger.error("Error executing event: $event", it) }
            }
        }
    }

    private fun generateMethodHandle(lookup: MethodHandles.Lookup, method: Method, listener: Any): MethodHandle {
        return lookup.unreflect(method).bindTo(listener)
    }
}
