package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.gui.screen.ClickGUIScreen
import ovo.xsvf.izmk.module.Module

/**
 * @author LangYa466
 * @since 2/27/2025
 */
object ClickGUI : Module(
    name = "click-gui",
) {
    override fun onEnable() {
        enabled = false
        if (mc.screen == null) {
            ClickGUIScreen.openScreen()
        }
    }
}
