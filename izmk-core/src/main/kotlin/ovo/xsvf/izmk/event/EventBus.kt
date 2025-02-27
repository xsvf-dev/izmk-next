package ovo.xsvf.izmk.event

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.command.CommandManager
import ovo.xsvf.izmk.module.ModuleManager
import java.lang.invoke.MethodHandles
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    private val listeners = ConcurrentHashMap<Class<out Event>, MutableList<Listener>>()
    private val lookup = MethodHandles.lookup()

    init {
        register(CommandManager)
        register(ModuleManager)
        register(IZMK)
    }

    @Suppress("unchecked_cast")
    fun register(listener: Any) {
        require(listener !is Class<*>) { "Event listener should be an instance of a class" }
        listener::class.java.declaredMethods.forEach { method ->
            if (method.isAnnotationPresent(EventTarget::class.java)) {
                require(method.parameterTypes.size == 1 && Event::class.java.isAssignableFrom(method.parameterTypes[0])) {
                    "Event listener method should have only one parameter of type Event"
                }
                method.isAccessible = true

                val methodHandle = lookup.unreflect(method).bindTo(listener)
                val eventTarget = method.getAnnotation(EventTarget::class.java)
                listeners.getOrPut(method.parameterTypes[0] as Class<out Event>) { mutableListOf() }
                    .let { list ->
                        list.add(Listener(listener::class.java,
                            { methodHandle.invoke(it) },
                            eventTarget.priority,
                            eventTarget.alwaysListening))
                        list.sortBy { it.priority }
                    }
            }
        }
    }

    fun unregister(listener: Any) {
        listeners.forEach { (_, listeners) ->
            listeners.removeAll { it.clazz == listener::class.java }
        }
    }

    fun post(event: Event) {
        listeners[event::class.java]?.forEach {
            if (event !is CancellableEvent || !event.isCancelled || it.alwaysListening) {
                it.handle(event)
            }
        }
    }
}

data class Listener(val clazz: Class<*>,
                    val method: (Event) -> Unit,
                    val priority: Int = 0,
                    val alwaysListening: Boolean = false) {
    val uuid: UUID = UUID.randomUUID()

    fun handle(event: Event) = method(event)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Listener) return false
        return other.uuid == uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }
}
