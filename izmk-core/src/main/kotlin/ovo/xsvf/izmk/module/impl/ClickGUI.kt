package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.gui.impl.ModuleListScreen
import ovo.xsvf.izmk.module.Module

/**
 * @author LangYa466
 * @since 2/27/2025
 */
object ClickGUI : Module(
    name = "click-gui",
    description = "Open click-gui module",
    loadFromConfig = false,
) {
    private val screen = ModuleListScreen()

    override fun onLoad() {
        enabled = false
    }

    override fun onEnable() {
        screen.openScreen()
    }

    override fun onDisable() {
        screen.closeScreen()
    }
}
