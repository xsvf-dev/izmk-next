package ovo.xsvf.izmk.event

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

open class EventAdapter: EventHandler {
    open fun onTick(event: TickEvent) {}

    private val handlers: ConcurrentHashMap<Class<out Event>, Consumer<Event>> = ConcurrentHashMap()

    init {
        handlers[TickEvent::class.java] = Consumer { event -> onTick(event as TickEvent) }
    }

    final override fun handle(event: Event) {
        handlers[event::class.java]?.accept(event)
    }
}