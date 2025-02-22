package ovo.xsvf.izmk.module

import net.minecraft.client.Minecraft
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventBus

/**
 * @author LangYa466
 * @since 2025/2/16
 */
abstract class Module(val name: String, val description: String = "") {
    var enabled = false
        set(value) {
            if (field != value) {
                field = value
                if (value) {
                    EventBus.register(this)
                    onEnable()
                } else {
                    EventBus.unregister(this)
                    onDisable()
                }
                IZMK.logger.debug("$name state is set to $value")
            }
        }

    protected val mc: Minecraft by lazy { Minecraft.getInstance() }

    var keyCode = -1

    /**
     * Called when the module is enabled
     */
    protected open fun onEnable() {}

    /**
     * Called when the module is disabled
     */
    protected open fun onDisable() {}

    fun toggle() {
        enabled = !enabled
    }
}