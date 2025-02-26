package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.gui.screen.impl.ModuleListScreen
import ovo.xsvf.izmk.module.Module

/**
 * @author LangYa466
 * @since 2/27/2025
 */
object ClickGUI : Module("ClickGUI", "Open click-gui module") {
    private val screen = ModuleListScreen()

    override fun onEnable() {
        screen.openScreen()
        super.onEnable()
    }
}
