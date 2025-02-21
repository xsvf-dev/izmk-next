package ovo.xsvf.izmk.module

import net.minecraft.client.Minecraft
import ovo.xsvf.izmk.event.EventBus

/**
 * @author LangYa466
 * @since 2025/2/16
 */
abstract class Module(val name: String, val description: String = "") {
    var enabled = false
        set(value) {
            if (field != value) { // 只有状态变化时才操作
                field = value
                if (value) EventBus.register(this) else EventBus.unregister(this)
            }
        }

    protected val mc: Minecraft by lazy { Minecraft.getInstance() }

    var keyCode = -1

    fun toggle() {
        enabled = !enabled
    }
}
