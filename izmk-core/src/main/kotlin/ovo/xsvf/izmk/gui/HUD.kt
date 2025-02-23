package ovo.xsvf.izmk.gui

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.translation.TranslationString

/**
 * @author xiaojiang233
 * @since 2025/2/22
 */
abstract class HUD(
    val name: String,
    x: Float,
    y: Float,
    var width: Float,
    var height: Float
) {
    val translation = TranslationString("hud", name)

    var x: Float = x
        set(value) {
            field = value.coerceIn(0f, IZMK.mc.window.width - width)
        }
    var y: Float = y
        set(value) {
            field = value.coerceIn(0f, IZMK.mc.window.height - height)
        }

    var enabled: Boolean = false
    var visible: Boolean = true

    private var dragging: Boolean = false
    private var dragOffsetX: Float = 0f
    private var dragOffsetY: Float = 0f

    val y1: Float
        get() = y + height

    val x1: Float
        get() = x + width

    open fun render(event: Render2DEvent) {}

    fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return mouseX in x..x1 && mouseY in y..y1
    }
}