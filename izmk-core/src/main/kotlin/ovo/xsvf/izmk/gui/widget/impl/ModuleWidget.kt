package ovo.xsvf.izmk.gui.widget.impl

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.screen.ValueListScreen
import ovo.xsvf.izmk.gui.widget.AbstractWidget
import ovo.xsvf.izmk.module.Module

class ModuleWidget(screen: GuiScreen, val module: Module) : AbstractWidget(screen) {
    private val valueListScreen = ValueListScreen(module)

    override fun draw(
        screenWidth: Int, screenHeight: Int,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        rectMulti.addRectGradientHorizontal(
            renderX, renderY, (screenWidth - 2 * 5f), getHeight().toFloat(),
            ColorRGB(0.2f, 0.2f, 0.2f), ColorRGB(0.25f, 0.25f, 0.25f)
        )

        // 模块名称
        fontMulti.addText(
            module.getDisplayName(),
            renderX + 7f, renderY + 2f,
            if (module.enabled) ColorRGB.WHITE else ColorRGB.GRAY,
            false, 1.2f
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, isLeftClick: Boolean) {
        if (isLeftClick) {
            module.toggle()
        } else {
            valueListScreen.openScreen(screen)
        }
    }

    override fun getHeight(): Int {
        return 20
    }
}