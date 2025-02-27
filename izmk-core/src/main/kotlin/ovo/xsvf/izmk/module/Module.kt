package ovo.xsvf.izmk.module

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventBus
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.settings.AbstractSetting
import ovo.xsvf.izmk.settings.SettingsDesigner
import ovo.xsvf.izmk.translation.TranslationString
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author LangYa466, xsvf
 * @since 2025/2/16
 */
abstract class Module(val name: String,
                      val description: String = "",
                      var keyCode: Int = -1,
                      val loadFromConfig: Boolean = true
): SettingsDesigner<Module> {
    private val settings = CopyOnWriteArrayList<AbstractSetting<*>>()

    val translation = TranslationString("modules", name)

    var enabled = false
        set(value) {
            if (field == value) return
            field = value
            if (value) {
                EventBus.register(this)
                onEnable()
            }
            else {
                onDisable()
                EventBus.unregister(this)
            }
        }

    protected val mc by lazy { IZMK.mc }
    protected val logger by lazy { IZMK.logger }

    fun toggle() {
        enabled = !enabled
    }

    open fun onEnable() {}
    open fun onDisable() {}
    open fun onLoad() {}

    override fun <S : AbstractSetting<*>> Module.setting(setting: S): S {
        setting.key.key.prefix = "modules.$name"
        settings.add(setting)
        return setting
    }

    fun getDisplayName(): String {
        return translation.translation
    }
}

abstract class RenderableModule(
    name: String,
    description: String = "",
    keyCode: Int = -1,
    defaultX: Float,
    defaultY: Float,
    var width: Float,
    var height: Float
): Module(name, description, keyCode) {
    var x: Float = defaultX
        set(value) {
            field = value.coerceIn(0f, mc.window.width - width)
        }
    var y: Float = defaultY
        set(value) {
            field = value.coerceIn(0f, mc.window.height - height)
        }

    val y1: Float
        get() = y + height

    val x1: Float
        get() = x + width

    open fun render(event: Render2DEvent) {}

    fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return RenderUtils2D.isMouseOver(mouseX, mouseY, x, y, x1, y1)
    }
}