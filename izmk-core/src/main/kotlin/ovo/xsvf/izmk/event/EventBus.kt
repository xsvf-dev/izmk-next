package ovo.xsvf.izmk.event

object EventBus {
    private val eventMap = mutableListOf<EventHandler>()

    fun register(handler: EventHandler) {
        eventMap.add(handler)
    }

    fun unregister(handler: EventHandler) {
        eventMap.remove(handler)
    }

    fun post(event: Event) {
        eventMap.forEach { it.handle(event) }
    }
}