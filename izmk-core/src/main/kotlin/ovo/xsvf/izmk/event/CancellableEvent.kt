package ovo.xsvf.izmk.event

/**
 * @author LangYa466
 * @since 2025/2/16
 */
abstract class CancellableEvent : Event, Cancellable {
    override var isCancelled: Boolean = false
}
