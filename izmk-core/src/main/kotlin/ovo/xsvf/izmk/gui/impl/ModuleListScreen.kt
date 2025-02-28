package ovo.xsvf.izmk.gui.impl

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.module.ModuleManager

class ModuleListScreen: GuiScreen("ModuleList") {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val listX = 50f
        val listY = 50f
        val listWidth = 300f
        val listHeight = 400f
        val entryHeight = 20f

        // 绘制列表背景
        RenderUtils2D.drawRectFilled(listX, listY, listWidth, listHeight, ColorRGB(0.15f, 0.15f, 0.15f))

        var offsetY = listY + 10f

        fun drawModule(enabled: Boolean, name: String) {
            val moduleX = listX + 5f
            val moduleY = offsetY

            // 背景渐变
            RenderUtils2D.drawRectGradientH(
                moduleX, moduleY, listWidth - 10f, entryHeight,
                ColorRGB(0.2f, 0.2f, 0.2f), ColorRGB(0.25f, 0.25f, 0.25f)
            )

            // 开关状态指示
            RenderUtils2D.drawCircleFilled(moduleX + 10f, moduleY + entryHeight / 2, 5f, 16,
                if (enabled) ColorRGB(0f, 1f, 0f) else ColorRGB(1f, 0f, 0f)
            )

            // 模块名称
            FontRenderers.drawString(name, moduleX + 25f, moduleY + 5f, ColorRGB.WHITE)

            offsetY += entryHeight + 5f
        }

        // 渲染所有模块
        ModuleManager.modulesMap.values.forEach { drawModule(it.enabled, it.getDisplayName()) }
    }

    override fun mouseClicked(buttonID: Int, mouseX: Double, mouseY: Double) {
        val listX = 50f
        val listY = 50f
        val listWidth = 300f
        var offsetY = listY + 10f
        val entryHeight = 20f

        fun toggleModule(toggle: () -> Unit) =
            RenderUtils2D.isMouseOver(mouseX.toFloat(), mouseY.toFloat(), listX + 5f, offsetY, listX + listWidth - 5f, offsetY + entryHeight)
                .also { if (it) toggle() }
                .also { offsetY += entryHeight + 5f }

        ModuleManager.modulesMap.values.firstOrNull { toggleModule { it.toggle() } }
    }
}
