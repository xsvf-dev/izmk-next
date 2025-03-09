package ovo.xsvf.izmk.gui.screen

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.impl.ModuleWidget
import ovo.xsvf.izmk.module.ModuleManager

object ClickGUIScreen: GuiScreen("ClickGUI") {
    private val modulesWindow = SimpleListScreen(
        ModuleManager
            .modules()
            .map { ModuleWidget(this, it) }
            .toMutableList(),
        "IZMK Next"
    )

    var settingsWindow: SimpleListScreen? = null

    override fun drawScreen(mouseX: Float, mouseY: Float, partialTicks: Float) {
        modulesWindow.draw(mouseX, mouseY, partialTicks)
        settingsWindow?.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(buttonId: Int, mouseX: Float, mouseY: Float) {
        if (settingsWindow?.mouseClicked(buttonId, mouseX, mouseY) == true) return
        modulesWindow.mouseClicked(buttonId, mouseX, mouseY)
    }

    override fun mouseReleased(buttonId: Int, mouseX: Float, mouseY: Float) {
        if (settingsWindow?.mouseReleased(buttonId, mouseX, mouseY) == true) return
        modulesWindow.mouseReleased(buttonId, mouseX, mouseY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        // If settings window is open, close it on escape
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && settingsWindow != null) {
            settingsWindow = null
            return true
        }
        if (settingsWindow?.keyPressed(keyCode, scanCode) == true) return true
        return modulesWindow.keyPressed(keyCode, scanCode)
    }

    override fun shouldCloseOnEsc(): Boolean {
        // If settings window is open, don't close the screen
        return settingsWindow == null
    }

    override fun onClose() {
        settingsWindow = null
    }
}