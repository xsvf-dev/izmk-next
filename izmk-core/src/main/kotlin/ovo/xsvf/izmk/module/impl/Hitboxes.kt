package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.module.Module

object Hitboxes: Module("hit-boxes") {
    val color by setting("color", ColorRGB.WHITE)
    val showViewVector by setting("show-eye-vector", true)
    val viewVectorColor by setting("eye-vector-color", ColorRGB(0, 0, 255, 254)) { showViewVector }

    val players by setting("players", true)
    val items by setting("items", true)
    val mobs by setting("mobs", true)
    val projectiles by setting("projectiles", true)

    override fun onEnable() {
        if (!mc.entityRenderDispatcher.shouldRenderHitBoxes()) {
            mc.entityRenderDispatcher.setRenderHitBoxes(true)
        }
    }

    override fun onDisable() {
        if (mc.entityRenderDispatcher.shouldRenderHitBoxes()) {
            mc.entityRenderDispatcher.setRenderHitBoxes(false)
        }
    }
}