package ovo.xsvf.izmk.module

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventBus

/**
 * @author LangYa466
 * @since 2025/2/16
 */
abstract class Module(val name: String, val description: String = "") {
    var enabled = false
        set(value) {
            if (field == value) return
            field = value
            IZMK.logger.debug("$name state is set to $value")
            if (value) {
                EventBus.register(this)
                onEnable()
            }
            else {
                onDisable()
                EventBus.unregister(this)
            }
        }

    protected val mc by lazy { IZMK.mc }
    protected val logger by lazy { IZMK.logger }

    var keyCode = -1

    fun toggle() {
        enabled = !enabled
    }

    open fun onEnable() {}
    open fun onDisable() {}
}
