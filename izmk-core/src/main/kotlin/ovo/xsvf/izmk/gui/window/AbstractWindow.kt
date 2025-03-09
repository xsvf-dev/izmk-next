package ovo.xsvf.izmk.gui.window

abstract class AbstractWindow(
    var title: String,
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
) {
    open fun draw(mouseX: Float, mouseY: Float, partialTicks: Float) {}
    open fun keyPressed(keyCode: Int, scanCode: Int): Boolean = true
    open fun mouseClicked(buttonId: Int, mouseX: Float, mouseY: Float): Boolean = true
    open fun mouseReleased(buttonId: Int, mouseX: Float, mouseY: Float): Boolean = true
    open fun mouseScrolled(mouseX: Float, mouseY: Float, scrollAmount: Int): Boolean = true
}