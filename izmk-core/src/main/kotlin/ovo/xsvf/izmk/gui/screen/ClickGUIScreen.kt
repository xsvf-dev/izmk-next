package ovo.xsvf.izmk.gui.screen

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.config.ConfigManager
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.impl.ModuleWidget
import ovo.xsvf.izmk.module.ModuleManager
import ovo.xsvf.izmk.module.impl.ClickGUI

object ClickGUIScreen : GuiScreen("ClickGUI") {
    private val modulesWindow = SimpleListScreen(
        ModuleManager
            .modules()
            .map { ModuleWidget(this, it) }
            .toMutableList()
    ) { "IZMK" }

    var settingsWindow: SimpleListScreen? = null
        set(value) {
            if (value == null) field?.onClose()
            field = value
        }

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

    override fun mouseScrolled(mouseX: Float, mouseY: Float, scrollAmount: Int): Boolean {
        if (settingsWindow?.mouseScrolled(mouseX, mouseY, scrollAmount) == true) return true
        return modulesWindow.mouseScrolled(mouseX, mouseY, scrollAmount)
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
        if (ClickGUI.saveCfgOnCloseGui) ConfigManager.saveAllConfig()
        modulesWindow.onClose()
    }
}