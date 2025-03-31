package ovo.xsvf.izmk.event

open class Event {
    open fun post(): Event {
        return this.also { EventBus.post(it) }
    }
}

open class CancellableEvent : Event() {
    var isCancelled: Boolean = false

    override fun post(): CancellableEvent {
        return this.also { EventBus.post(it) }
    }
}