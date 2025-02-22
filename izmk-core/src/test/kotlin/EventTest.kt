import ovo.xsvf.izmk.event.CancellableEvent
import ovo.xsvf.izmk.event.Event
import ovo.xsvf.izmk.event.EventBus
import ovo.xsvf.izmk.event.EventTarget

// 用于测试的事件类
data class TestEvent(val message: String) : Event()

// 用于测试的可取消事件类
class TestCancellableEvent(var message: String) : CancellableEvent()

// 测试用的监听器
object TestListener {
    // 记录方法调用的顺序
    val invocationOrder = mutableListOf<String>()

    @EventTarget(priority = 10)
    fun highPriorityListener(event: TestEvent) {
        invocationOrder.add("highPriorityListener")
    }

    @EventTarget(priority = 5)
    fun mediumPriorityListener(event: TestEvent) {
        invocationOrder.add("mediumPriorityListener")
    }

    @EventTarget(priority = 1, alwaysListening = true)
    fun lowPriorityAlwaysListener(event: TestEvent) {
        invocationOrder.add("lowPriorityAlwaysListener")
    }

    // 取消事件的监听器
    @EventTarget(priority = 20)
    fun cancelEventListener(event: TestCancellableEvent) {
        event.isCancelled = true
        invocationOrder.add("cancelEventListener")
    }

    // 此方法在事件被取消时不会被调用（除非 alwaysListening = true）
    @EventTarget(priority = 10)
    fun cancelledHandler(event: TestCancellableEvent) {
        invocationOrder.add("cancelledHandler")
    }

    // alwaysListening = true，即使事件被取消仍然会被调用
    @EventTarget(priority = 5, alwaysListening = true)
    fun alwaysHandler(event: TestCancellableEvent) {
        invocationOrder.add("alwaysHandler")
    }
}

// 断言函数，模拟测试框架的 assertEquals
fun assertEquals(expected: List<String>, actual: List<String>, message: String) {
    if (expected != actual) {
        throw AssertionError("$message\nExpected: $expected\nActual: $actual")
    } else {
        println("$message - ✅ 测试通过")
    }
}

fun main() {
    // ========== 测试 1: 普通事件处理顺序 ==========
    TestListener.invocationOrder.clear()
    EventBus.register(TestListener)

    println("测试 1: 普通事件处理顺序")
    EventBus.post(TestEvent("Hello"))

    val expectedOrder1 = listOf("highPriorityListener", "mediumPriorityListener", "lowPriorityAlwaysListener")
    assertEquals(expectedOrder1, TestListener.invocationOrder, "普通事件处理顺序错误")

    EventBus.unregister(TestListener)

    // ========== 测试 2: 可取消事件 ==========
    TestListener.invocationOrder.clear()
    EventBus.register(TestListener)

    println("\n测试 2: 可取消事件")
    EventBus.post(TestCancellableEvent("Cancellable Event"))

    val expectedOrder2 = listOf("cancelEventListener", "alwaysHandler")
    assertEquals(expectedOrder2, TestListener.invocationOrder, "可取消事件处理错误")

    EventBus.unregister(TestListener)
}
