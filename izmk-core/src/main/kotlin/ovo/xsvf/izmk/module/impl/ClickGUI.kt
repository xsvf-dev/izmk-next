package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.graphics.RenderSystem
import ovo.xsvf.izmk.gui.screen.ClickGUIScreen
import ovo.xsvf.izmk.module.Module
import ovo.xsvf.izmk.translation.TranslationEnum

/**
 * @author LangYa466
 * @since 2/27/2025
 */
object ClickGUI : Module(
    name = "click-gui",
) {
    var fontRenderer by setting("font-renderer", FontRendererType.GENERAL)
        .onChangeValue { setting ->
            if (setting.value == FontRendererType.SPARSE
                && (RenderSystem.gpuType == RenderSystem.GPUType.INTEL
                        || RenderSystem.gpuType == RenderSystem.GPUType.OTHER))
                setting.value(FontRendererType.GENERAL)
        }

    override fun onEnable() {
        enabled = false
        if (mc.screen == null) {
            ClickGUIScreen.openScreen()
        }
    }

    enum class FontRendererType(override val key: CharSequence): TranslationEnum {
        GENERAL("general"),
        SPARSE("sparse"),
    }
}
