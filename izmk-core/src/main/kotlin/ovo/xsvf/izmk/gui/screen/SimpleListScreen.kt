package ovo.xsvf.izmk.gui.screen

import org.apache.logging.log4j.LogManager
import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.graphics.ScissorBox
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.widget.AbstractWidget
import ovo.xsvf.izmk.gui.window.AbstractWindow
import ovo.xsvf.izmk.gui.window.DragWindow

class SimpleListScreen(
    private val widgets: MutableList<AbstractWidget>,
    private val screenTitle: () -> String = { "" }
) :
    AbstractWindow(screenTitle(), 50f, 50f, 275f, 300f) {

    private val log = LogManager.getLogger(javaClass)
    private val rectMulti = PosColor2DMultiDraw()
    private val fontMulti = FontMultiDraw()

    private val window = DragWindow(x, y, width, height)
    private val padding = 35f

    /* scroll bar */
    private var showScrollBar = false
    private var scrollOffset = 0f
    private val scrollBarWidth = 6f
    private val scrollBarPadding = 3f
    private var draggingScrollBar = false
    private var lastDragY = 0f

    // 预计算属性
    private val contentHeight
        get() = widgets.filter { it.isVisible() }.sumOf { it.getHeight().toDouble() + 5 }.toFloat()
    private val viewportHeight get() = window.height - padding - scrollBarPadding
    private val maxScrollOffset get() = (contentHeight - viewportHeight).coerceAtLeast(0f)

    override fun draw(mouseX: Float, mouseY: Float, partialTicks: Float) {
        if (draggingScrollBar) {
            val deltaY = mouseY - lastDragY
            val scrollPerPixel = maxScrollOffset / (viewportHeight - (viewportHeight * viewportHeight / contentHeight))
            scrollOffset = (scrollOffset + deltaY * scrollPerPixel).coerceIn(0f, maxScrollOffset)
            lastDragY = mouseY
        } else {
            window.update(mouseX, mouseY)
        }

        drawWindowFrame()
        drawContent(mouseX, mouseY, partialTicks)

        rectMulti.draw()
        fontMulti.draw()
    }

    private fun drawWindowFrame() {
        // 绘制窗口背景
        rectMulti.addRect(
            window.x, window.y,
            window.width, window.height,
            ColorRGB(0.15f, 0.15f, 0.15f)
        )

        // 绘制标题
        fontMulti.addText(
            screenTitle(),
            window.x + 5f,
            window.y + 5f,
            ColorRGB.WHITE,
            false, 2f
        )

        rectMulti.draw()
        fontMulti.draw()
    }

    private fun drawContent(mouseX: Float, mouseY: Float, partialTicks: Float) {
        val visibleWidgets = widgets.filter { it.isVisible() }
        showScrollBar = contentHeight > viewportHeight

        val renderWidth = if (showScrollBar) window.width - scrollBarWidth - scrollBarPadding else window.width
        var offsetY = window.y + padding - scrollOffset

        scissorBox.updateAndDraw(window.x, window.y + padding, renderWidth, window.height - padding) {
            visibleWidgets.forEach { widget ->
                if (offsetY in (window.y)..(window.y + window.height)) {
                    widget.draw(
                        renderWidth, viewportHeight,
                        mouseX, mouseY,
                        window.x + 5f, offsetY,
                        fontMulti, rectMulti, partialTicks
                    )
                }
                offsetY += widget.getHeight() + 5f
            }
            rectMulti.draw()
            fontMulti.draw()
        }

        if (showScrollBar) {
            drawScrollBar(mouseY)
        }
    }

    private fun drawScrollBar(mouseY: Float) {
        // 滚动条背景
        rectMulti.addRect(
            window.x + window.width - scrollBarWidth - scrollBarPadding,
            window.y + padding,
            scrollBarWidth, viewportHeight,
            ColorRGB(0.2f, 0.2f, 0.2f)
        )

        // 滚动条滑块
        val handleHeight = (viewportHeight * (viewportHeight / contentHeight)).coerceIn(10f, viewportHeight)
        val scrollRatio = scrollOffset / maxScrollOffset
        val handleY = window.y + padding + (viewportHeight - handleHeight) * scrollRatio

        rectMulti.addRect(
            window.x + window.width - scrollBarWidth - scrollBarPadding,
            handleY,
            scrollBarWidth, handleHeight,
            if (draggingScrollBar) ColorRGB(0.7f, 0.7f, 0.7f) else ColorRGB(0.5f, 0.5f, 0.5f)
        )
    }

    override fun mouseScrolled(mouseX: Float, mouseY: Float, scrollAmount: Int): Boolean {
        if (showScrollBar && window.isHovered(mouseX, mouseY)) {
            scrollOffset = (scrollOffset - scrollAmount * 20f).coerceIn(0f, maxScrollOffset)
            return true
        }
        return false
    }

    override fun mouseClicked(buttonId: Int, mouseX: Float, mouseY: Float): Boolean {
        if (handleScrollBarClick(mouseX, mouseY)) return true
        if (window.shouldDrag(mouseX, mouseY, padding)) {
            window.startDrag(mouseX, mouseY)
            return true
        }
        return checkWidgetClicks(mouseX, mouseY, buttonId)
    }

    private fun handleScrollBarClick(mouseX: Float, mouseY: Float): Boolean {
        if (!showScrollBar) return false

        val scrollBarX = window.x + window.width - scrollBarWidth - scrollBarPadding
        if (mouseX in scrollBarX..(scrollBarX + scrollBarWidth) &&
            mouseY in window.y + padding..(window.y + padding + viewportHeight)
        ) {
            draggingScrollBar = true
            lastDragY = mouseY
            return true
        }
        return false
    }

    private fun checkWidgetClicks(mouseX: Float, mouseY: Float, buttonId: Int): Boolean {
        if (!window.isHovered(mouseX, mouseY)) return false
        var offsetY = window.y + padding - scrollOffset
        for (widget in widgets.filter { it.isVisible() }) {
            if (mouseY in offsetY..(offsetY + widget.getHeight()) &&
                mouseX in window.x + 5f..(window.x + window.width - 5f - if (showScrollBar) scrollBarWidth else 0f)
            ) {
                widget.mouseClicked(mouseX, mouseY, buttonId == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            }
            offsetY += widget.getHeight() + 5f
        }
        return true
    }

    override fun mouseReleased(buttonId: Int, mouseX: Float, mouseY: Float): Boolean {
        draggingScrollBar = false
        widgets.forEach { it.mouseReleased(mouseX, mouseY, buttonId == GLFW.GLFW_MOUSE_BUTTON_LEFT) }
        return window.stopDrag()
    }

    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        return widgets.any { it.keyPressed(keyCode, scanCode) }
    }

    override fun onClose() {
        widgets.forEach { it.onWindowClose() }
    }

    companion object {
        private val scissorBox = ScissorBox()
    }
}
