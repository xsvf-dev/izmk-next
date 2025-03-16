package ovo.xsvf.izmk.gui.widget

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen

abstract class AbstractWidget(val screen: GuiScreen) {
    val mc by lazy { IZMK.mc }
    val logger: Logger = LogManager.getLogger(javaClass)

    abstract fun getHeight(): Float
    abstract fun draw(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    )

    open fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {}
    open fun mouseReleased(mouseX: Float, mouseY: Float, isLeftClick: Boolean): Boolean = false
    open fun keyPressed(keyCode: Int, scanCode: Int): Boolean = false
    open fun onWindowClose() {}
    open fun isVisible(): Boolean = true

    fun drawDefaultBackground(rectMulti: PosColor2DMultiDraw, renderX: Float, renderY: Float, screenWidth: Float) {
        rectMulti.addRectGradientHorizontal(
            renderX, renderY,
            screenWidth - 2 * 5f, getHeight(),
            ColorRGB(0.2f, 0.2f, 0.2f),
            ColorRGB(0.25f, 0.25f, 0.25f)
        )
    }
}