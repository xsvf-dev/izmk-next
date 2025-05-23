package ovo.xsvf.izmk.module.impl

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.graphics.RenderSystem
import ovo.xsvf.izmk.graphics.font.FontRendererType
import ovo.xsvf.izmk.gui.screen.ClickGUIScreen
import ovo.xsvf.izmk.module.Module
import ovo.xsvf.izmk.translation.TranslationManager

/**
 * @author LangYa466
 * @since 2/27/2025
 */
object ClickGUI : Module(
    name = "click-gui",
    defaultKeybind = GLFW.GLFW_KEY_RIGHT_SHIFT,
) {
    var fontRenderer by setting("font-renderer", FontRendererType.GENERAL)
        .onChangeValue { setting ->
            if (setting.value == FontRendererType.SPARSE
                && (RenderSystem.gpuType == RenderSystem.GPUType.INTEL
                        || RenderSystem.gpuType == RenderSystem.GPUType.OTHER)
            )
                setting.value(FontRendererType.GENERAL)
        }

    val language by setting("language", TranslationManager.Language.ZH_CN)

    val saveCfgOnCloseGui by setting("save-cfg-on-close-gui", true)

    override fun onEnable() {
        enabled = false
        ClickGUIScreen.openScreen()
        logger.info("ClickGUI enabled")
    }
}
