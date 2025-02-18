package ovo.xsvf.izmk.event

open class Event

open class CancellableEvent : Event() {
    var isCancelled: Boolean = false
}