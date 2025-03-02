package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.gui.screen.ClickGUIScreen
import ovo.xsvf.izmk.gui.window.SimpleListWindow
import ovo.xsvf.izmk.gui.widget.impl.ModuleWidget
import ovo.xsvf.izmk.module.Module
import ovo.xsvf.izmk.module.ModuleManager

/**
 * @author LangYa466
 * @since 2/27/2025
 */
object ClickGUI : Module(
    name = "click-gui",
    description = "Open click-gui module",
    loadFromConfig = false
) {
    override fun onLoad() {
    }

    override fun onEnable() {
        enabled = false
        ClickGUIScreen.openScreen()
    }
}
