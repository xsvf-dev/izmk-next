package ovo.xsvf.izmk.module.impl

import net.minecraft.client.renderer.texture.OverlayTexture
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.PreTickEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.injection.accessor.GameRendererAccessor
import ovo.xsvf.izmk.module.Module

object HitColor: Module(
    name = "hit-color"
) {
    val color by setting("color", ColorRGB(50, 60, 700, 140))
        .onChangeValue { updateOverlayTexture() }
    private var inited = false

    override fun onEnable() {
        updateOverlayTexture()
    }

    override fun onDisable() {
        updateOverlayTexture()
    }

    @EventTarget
    fun onTick(e: PreTickEvent) {
        if (!inited) {
            updateOverlayTexture()
            inited = true
        }
    }

    private fun updateOverlayTexture() {
        (mc.gameRenderer as GameRendererAccessor).setOverlayTexture(OverlayTexture())
    }
}