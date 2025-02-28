package ovo.xsvf.izmk.gui.impl

/**
 * @author LangYa466
 * @since 2/15/2025
 */
open class DragWindow(var x: Int, var y: Int, var width: Int, var height: Int) {
    private var dragging: Boolean = false
    private var dragOffsetX: Int = 0
    private var dragOffsetY: Int = 0

    /**
     * 判断鼠标是否在窗口标题区域（高度 headerHeight 内）
     */
    fun isHoveringHeader(mouseX: Int, mouseY: Int, headerHeight: Int): Boolean {
        return mouseX in x..(x + width) && mouseY in y..(y + headerHeight)
    }

    /**
     * 开始拖拽，并记录初始偏移量
     */
    fun startDrag(mouseX: Int, mouseY: Int) {
        dragging = true
        dragOffsetX = mouseX - x
        dragOffsetY = mouseY - y
    }

    /**
     * 拖拽中，更新窗口位置
     */
    fun drag(mouseX: Int, mouseY: Int) {
        if (dragging) {
            x = mouseX - dragOffsetX
            y = mouseY - dragOffsetY
        }
    }

    /**
     * 停止拖拽
     */
    fun stopDrag() {
        dragging = false
    }
}