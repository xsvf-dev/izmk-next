package ovo.xsvf.izmk.gui.screen.impl

import net.minecraft.client.gui.GuiGraphics
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.gui.screen.GuiScreen
import ovo.xsvf.izmk.module.ModuleManager

class ModuleListScreen: GuiScreen("ModuleList") {
    override fun drawScreen(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val listX = 50f
        val listY = 50f
        val listWidth = 300f
        val listHeight = 400f

        // 列表背景
        RenderUtils2D.drawRectFilled(listX, listY, listWidth, listHeight, ColorRGB(0.15f, 0.15f, 0.15f))

        var offsetY = listY + 10f
        val entryHeight = 20f

        for (module in ModuleManager.modulesMap.values) {
            // 背景渐变
            RenderUtils2D.drawRectGradientH(
                listX + 5f,
                y = offsetY,
                listWidth - 10f,
                height = entryHeight,
                startColor = ColorRGB(0.2f, 0.2f, 0.2f),
                endColor = ColorRGB(0.25f, 0.25f, 0.25f)
            )

            // 开关状态
            val color = if (module.enabled) ColorRGB(0f, 1f, 0f) else ColorRGB(1f, 0f, 0f)
            RenderUtils2D.drawCircleFilled(listX + 15f, offsetY + entryHeight / 2, 5f, segments = 16, color)

            // 模块名称
            FontRenderers.drawString(module.name, listX + 30f, offsetY + 5f, ColorRGB.WHITE)

            offsetY += entryHeight + 5f
        }
    }

    override fun mouseClicked(buttonID: Int, mouseX: Double, mouseY: Double) {
        val listX = 50f
        val listY = 50f
        val listWidth = 300f
        var offsetY = listY + 10f
        val entryHeight = 20f

        for (module in ModuleManager.modulesMap.values) {
            val x = listX + 5f
            val y = offsetY
            val width = x + listWidth - 10f
            val height = y + entryHeight

            if (RenderUtils2D.isMouseOver(mouseX.toFloat(), mouseY.toFloat(), x, y, width, height)) {
                module.toggle()
                break
            }
            offsetY += entryHeight + 5f
        }
    }
}
