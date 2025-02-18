package ovo.xsvf.izmk.module

import net.minecraft.client.Minecraft
import ovo.xsvf.izmk.event.EventAdapter
import ovo.xsvf.izmk.event.EventBus

/**
 * @author LangYa466
 * @since 2025/2/16
 */
abstract class Module(val name: String, val description: String = ""): EventAdapter(){
    var enabled = false
        set(value) {
            if (value) EventBus.register(this)
            else EventBus.unregister(this)
            field = value
        }
    protected val mc: Minecraft = Minecraft.getInstance()

    var keyCode = -1;

    fun toggle() {
        this.enabled = !enabled
    }
}
