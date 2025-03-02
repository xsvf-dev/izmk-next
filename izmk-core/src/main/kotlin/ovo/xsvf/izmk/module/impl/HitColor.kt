package ovo.xsvf.izmk.module.impl

import net.minecraft.client.renderer.texture.OverlayTexture
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.injection.accessor.GameRendererAccessor
import ovo.xsvf.izmk.module.Module

object HitColor: Module(
    name = "HitColor",
    description = "Change hit color"
) {
    val color by setting("color", ColorRGB.WHITE)
        .onChangeValue { updateOverlayTexture() }

    override fun onLoad() {
        updateOverlayTexture()
    }

    private fun updateOverlayTexture() {
        logger.debug("Updating overlay texture")
        (mc.gameRenderer as GameRendererAccessor).setOverlayTexture(OverlayTexture())
    }
}