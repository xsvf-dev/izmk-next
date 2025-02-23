package ovo.xsvf.izmk.module

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventBus
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

    override fun <S : AbstractSetting<*>> Module.setting(setting: S): S {
        setting.key.key.prefix = "modules.$name"
        settings.add(setting)
        return setting
    }
}