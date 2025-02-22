package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.event.EventListener
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.gui.HUDManager
import ovo.xsvf.izmk.module.Module

/**
 * @author LangYa466
 * @since 2025/2/16
 */
class Test : Module("Test") {
    init {
        enabled = true
    }

    @EventListener
    fun onRender2D(event: Render2DEvent) {
        FontRenderers.drawString("Hello, world!", 10f, 10f, ColorRGB.WHITE)
    }

    override fun onEnable() {
        HUDManager.enableHUD("NeneHud")
    }

    override fun onDisable() {
        HUDManager.disableHUD("NeneHud")
    }

}
