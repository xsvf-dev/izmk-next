import org.jetbrains.annotations.TestOnly
import ovo.xsvf.izmk.event.CancellableEvent
import ovo.xsvf.izmk.event.Event
import ovo.xsvf.izmk.event.EventBus
import ovo.xsvf.izmk.event.EventTarget

class MyEventListener {
    @EventTarget(priority = 1, alwaysListening = false)
    fun onEvent(event: Event) {
//        println("Event received: $event")
    }

    @EventTarget(priority = 2, alwaysListening = true)
    fun onCancellableEvent(event: CancellableEvent) {
        if (!event.isCancelled) {
            println("Cancellable event received: $event")
        } else {
            println("Cancellable event was cancelled: $event")
        }
    }
}

@TestOnly
fun main() {
    val listener = MyEventListener()

    // 注册事件监听器
    println("Registering listener...")
    EventBus.register(listener)

    // 创建并发布事件
    val event = Event()
//    val cancellableEvent = CancellableEvent()
//    EventBus.post(event)  // 触发普通事件
//    EventBus.post(cancellableEvent)  // 触发可取消事件

    println("100 times posting event...")
    val time0 = System.nanoTime()
    for (i in 0 until 100) {
//        println("Posting event... $i")
        EventBus.post(event)
    }
    val time1 = System.nanoTime()
    println("Time elapsed: ${time1 - time0} ns = ${(time1 - time0) / 1e6} ms")

    // 取消可取消事件
//    cancellableEvent.isCancelled = true
//    EventBus.post(cancellableEvent)  // 触发被取消的事件，应该不会触发 onCancellableEvent
//
//    // 注销事件监听器
//    println("Unregistering listener...")
//    EventBus.unregister(listener)
//
//    // 发布事件后，已经注销监听器，事件不再触发
//    EventBus.post(event)  // 触发普通事件，不会触发
//    EventBus.post(cancellableEvent)  // 触发可取消事件，不会触发
}
