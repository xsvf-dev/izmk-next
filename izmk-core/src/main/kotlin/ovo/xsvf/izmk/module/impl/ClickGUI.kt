package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.gui.screen.SimpleListScreen
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
    private lateinit var screen: SimpleListScreen

    override fun onLoad() {
        screen = SimpleListScreen(mutableListOf(), "IZMK Next")
        screen.widgets.addAll(ModuleManager.modules()
            .filter { it.showInGui }
            .map { ModuleWidget(screen, it) }
        )
    }

    override fun onEnable() {
        enabled = false
        screen.openScreen(null)
    }
}
