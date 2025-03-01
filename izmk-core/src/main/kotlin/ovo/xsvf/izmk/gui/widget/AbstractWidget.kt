package ovo.xsvf.izmk.gui.widget

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen

abstract class AbstractWidget(val screen: GuiScreen) {
    val mc by lazy { IZMK.mc }
    val logger: Logger = LogManager.getLogger(javaClass)

    abstract fun draw(screenWidth: Int, screenHeight: Int,
                      renderX: Float, renderY: Float,
                      fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
                      partialTicks: Float)

    abstract fun mouseClicked(mouseX: Double, mouseY: Double, isLeftClick: Boolean)

    abstract fun getHeight(): Int
}