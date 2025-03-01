package ovo.xsvf.izmk.gui.screen

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.module.Module

/**
 * ValueListScreen 修改后，增加了 window 变量。
 */
class ValueListScreen(val module: Module) : GuiScreen("value-list") {
    private val window = DraggableWindow(80, 50, 300, 400)

    private val rectMulti = PosColor2DMultiDraw()
    private val fontMulti = FontMultiDraw()

    private val padding = 5f

    private val widgets = mutableListOf<AbstractSettingWidget>()

    init {
        module.settings.forEach {
            widgets.add(it.createWidget(this))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        window.update(mouseX, mouseY)
        rectMulti.clear()
        fontMulti.clear()

        // 使用 window 的位置和尺寸作为绘制区域
        val listX = window.x.toFloat()
        val listY = (window.y + 35f)

        val listWidth = window.width.toFloat()
        val listHeight = window.height.toFloat()

        // 绘制整体背景
        rectMulti.addRect(listX, window.y.toFloat(), listWidth, listHeight, ColorRGB(0.15f, 0.15f, 0.15f))
        fontMulti.addText(module.getDisplayName(),window.x.toFloat() + 5f, window.y.toFloat() + 5f,ColorRGB.WHITE,false,2f)

        var offsetY = listY + padding

        widgets.forEach { widget ->
            val settingX = listX + padding
            val settingY = offsetY

            widget.draw(
                window.width, window.height,
                settingX, settingY,
                fontMulti, rectMulti,
                partialTicks
            )

            offsetY += widget.getHeight() + padding
        }

        rectMulti.draw()
        fontMulti.draw()
    }

    override fun mouseClicked(buttonID: Int, mouseX: Double, mouseY: Double) {
        // 如果点击在窗口标题区域（这里假定标题区域高度为 20），则启动拖拽
        if (window.shouldDrag(mouseX.toInt(), mouseY.toInt(), 20)) {
            window.startDrag(mouseX.toInt(), mouseY.toInt())
            return
        }

        val listX = window.x.toFloat()
        val listY = (window.y + 35f)
        val listWidth = window.width.toFloat()

        var offsetY = listY + padding

        widgets.forEach {
            val settingX = listX + padding

            val isMouseOverRow = RenderUtils2D.isMouseOver(
                mouseX.toFloat(), mouseY.toFloat(),
                settingX, offsetY,
                settingX + listWidth - 2 * padding,
                offsetY + it.getHeight()
            )

            if (isMouseOverRow) {
                it.mouseClicked(mouseX, mouseY, buttonID == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            }

            offsetY += it.getHeight() + padding
        }
    }

    override fun mouseReleased(buttonID: Int, mouseX: Double, mouseY: Double) {
        window.stopDrag()
    }
}