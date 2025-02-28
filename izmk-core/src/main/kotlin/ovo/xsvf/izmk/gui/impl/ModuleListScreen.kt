package ovo.xsvf.izmk.gui.impl

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.*
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.module.Module
import ovo.xsvf.izmk.module.ModuleManager

class ModuleListScreen : GuiScreen("ModuleList") {
    private val rectMulti = PosColor2DMultiDraw()
    private val fontMulti = FontMultiDraw()
    private val valueListScreen = HashMap<Module, GuiScreen>()

    private val listX = 50f
    private val listY = 50f
    private val listWidth = 300f
    private val listHeight = 400f
    private val entryHeight = 20f
    private val padding = 5f

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // 绘制列表背景
        rectMulti.addRect(listX, listY, listWidth, listHeight, ColorRGB(0.15f, 0.15f, 0.15f))

        var offsetY = listY + padding

        ModuleManager.modulesMap.values.forEach {
            val moduleX = listX + padding
            val moduleY = offsetY

            // 背景渐变
            rectMulti.addRectGradientHorizontal(
                moduleX, moduleY, listWidth - 2 * padding, entryHeight,
                ColorRGB(0.2f, 0.2f, 0.2f), ColorRGB(0.25f, 0.25f, 0.25f)
            )

            // 模块名称
            fontMulti.addText(
                it.getDisplayName(),
                moduleX + padding + 2f,
                moduleY + padding - 3f,
                if (it.enabled) ColorRGB.WHITE else ColorRGB.GRAY
            )

            offsetY += entryHeight + padding
        }

        rectMulti.draw()
        fontMulti.draw()
    }

    override fun mouseClicked(buttonID: Int, mouseX: Double, mouseY: Double) {
        var offsetY = listY + padding

        for (module in ModuleManager.modulesMap.values) {
            val moduleX = listX + padding
            val moduleY = offsetY

            val isMouseOver = RenderUtils2D.isMouseOver(
                mouseX.toFloat(),
                mouseY.toFloat(),
                moduleX, moduleY,
                moduleX + listWidth - 2 * padding,
                moduleY + entryHeight
            )

            if (isMouseOver) {
                when (buttonID) {
                    GLFW.GLFW_MOUSE_BUTTON_LEFT -> module.toggle()
                    GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                        valueListScreen.getOrPut(module) { ValueListScreen(module) }.openScreen()
                    }
                }
                break
            }

            offsetY += entryHeight + padding
        }
    }
}
