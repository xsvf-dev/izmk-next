package ovo.xsvf.izmk.gui.window

/**
 * @author LangYa466
 * @since 2/15/2025
 */
class DragWindow(var x: Float, var y: Float, var width: Float, var height: Float) {
    private var dragging: Boolean = false
    private var dragOffsetX: Float = 0f
    private var dragOffsetY: Float = 0f

    /**
     * 判断鼠标是否在窗口标题区域（高度 headerHeight 内）
     */
    fun shouldDrag(mouseX: Float, mouseY: Float, headerHeight: Float): Boolean {
        return mouseX in x..(x + width) && mouseY in y..(y + headerHeight)
    }

    /**
     * 判断鼠标是否在窗口区域内
     */
    fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX in x..(x + width) && mouseY in y..(y + height)
    }

    /**
     * 开始拖拽，并记录初始偏移量
     */
    fun startDrag(mouseX: Float, mouseY: Float) {
        dragging = true
        dragOffsetX = mouseX - x
        dragOffsetY = mouseY - y
    }

    /**
     * 拖拽中，更新窗口位置
     */
    fun update(mouseX: Float, mouseY: Float) {
        if (dragging) {
            x = mouseX - dragOffsetX
            y = mouseY - dragOffsetY
        }
    }

    /**
     * 停止拖拽
     */
    fun stopDrag(): Boolean {
        if (dragging) {
            dragging = false
            return true
        }
        return false
    }
}