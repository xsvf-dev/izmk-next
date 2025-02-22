import ovo.xsvf.izmk.event.CancellableEvent
import ovo.xsvf.izmk.event.Event
import ovo.xsvf.izmk.event.EventBus
import ovo.xsvf.izmk.event.EventListener

class MyEventListener {
    @EventListener(priority = 1, alwaysListening = false)
    fun onEvent(event: Event) {
        println("Event received: $event")
    }

    @EventListener(priority = 2, alwaysListening = true)
    fun onCancellableEvent(event: CancellableEvent) {
        if (!event.isCancelled) {
            println("Cancellable event received: $event")
        } else {
            println("Cancellable event was cancelled: $event")
        }
    }
}

fun main() {
    val listener = MyEventListener()

    // 注册事件监听器
    println("Registering listener...")
    EventBus.register(listener)

    // 创建并发布事件
    val event = Event()
    val cancellableEvent = CancellableEvent()
    EventBus.post(event)  // 触发普通事件
    EventBus.post(cancellableEvent)  // 触发可取消事件

    // 取消可取消事件
    cancellableEvent.isCancelled = true
    EventBus.post(cancellableEvent)  // 触发被取消的事件，应该不会触发 onCancellableEvent

    // 注销事件监听器
    println("Unregistering listener...")
    EventBus.unregister(listener)

    // 发布事件后，已经注销监听器，事件不再触发
    EventBus.post(event)  // 触发普通事件，不会触发
    EventBus.post(cancellableEvent)  // 触发可取消事件，不会触发
}
