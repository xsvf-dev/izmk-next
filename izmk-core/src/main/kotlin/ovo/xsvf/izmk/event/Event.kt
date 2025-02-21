package ovo.xsvf.izmk.event

open class Event {
    fun post() {
        EventBus.post(this)
    }
}

open class CancellableEvent : Event() {
    var isCancelled: Boolean = false
}