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

    abstract fun getHeight(): Int
    abstract fun draw(screenWidth: Int, screenHeight: Int,
                      renderX: Float, renderY: Float,
                      fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
                      partialTicks: Float)
    open fun mouseClicked(mouseX: Double, mouseY: Double, isLeftClick: Boolean) {}
    open fun keyPressed(keyCode: Int, scanCode: Int): Boolean { return false }

    fun drawDefaultBackground(rectMulti: PosColor2DMultiDraw, renderX: Float, renderY: Float, screenWidth: Int) {
        rectMulti.addRectGradientHorizontal(
            renderX, renderY,
            screenWidth - 2 * 5f, getHeight().toFloat(),
            ColorRGB(0.2f, 0.2f, 0.2f),
            ColorRGB(0.25f, 0.25f, 0.25f)
        )
    }
}