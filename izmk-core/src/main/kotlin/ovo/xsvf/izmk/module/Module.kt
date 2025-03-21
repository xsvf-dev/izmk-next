package ovo.xsvf.izmk.module

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventBus
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.settings.*
import ovo.xsvf.izmk.translation.TranslationString
import ovo.xsvf.izmk.util.input.KeyBind
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author LangYa466, xsvf
 * @since 2025/2/16
 */
abstract class Module(val name: String,
                      val loadFromConfig: Boolean = true,
                      val showInGui: Boolean = true,
                      defaultKeybind: Int? = null,
): SettingsDesigner<Module> {
    val settings = CopyOnWriteArrayList<AbstractSetting<*>>()
    val translation = TranslationString("modules", name)
    val description = TranslationString("modules.$name", "description")

    private val enabled0 = BooleanSetting(TranslationString("modules", "enabled"), false) { false }
    var enabled by enabled0

    private val keyBind0 = KeyBindSetting(TranslationString("modules", "key-bind"), KeyBind())
    var keyBind by keyBind0

    init {
        settings.add(enabled0)
        settings.add(keyBind0)

        enabled0.onChangeValue {
            if (enabled) {
                EventBus.register(this)
                onEnable()
            } else {
                onDisable()
                EventBus.unregister(this)
            }
        }

        defaultKeybind?.let {
            keyBind = KeyBind(KeyBind.Type.KEYBOARD, it, GLFW.glfwGetKeyScancode(it))
        }
    }

    protected val mc by lazy { IZMK.mc }
    protected val logger: Logger by lazy { LogManager.getLogger(javaClass) }

    fun toggle() {
        enabled = !enabled
    }

    open fun onEnable() {}
    open fun onDisable() {}
    open fun onLoad() {}

    final override fun <S : AbstractSetting<*>> Module.setting(setting: S): S {
        setting.name.key.prefix = "modules.$name"
        settings.add(setting)
        return setting
    }

    fun getDisplayName(): String {
        return translation.translation
    }
}

abstract class RenderableModule(
    name: String,
    defaultX: Float,
    defaultY: Float,
    var width: Float = 0f,
    var height: Float  = 0f
): Module(name) {
    private val x0 = FloatSetting(
        TranslationString("modules.renderable", "x"), defaultX,
        minValue = 0f, maxValue = mc.window.width.toFloat(), step = mc.window.width / 500f,
        visibility = { false }
    )
    var x by x0

    private val y0 = FloatSetting(
        TranslationString("modules.renderable", "y"), defaultY,
        minValue = 0f, maxValue = mc.window.height.toFloat(), step = mc.window.height / 500f,
        visibility = { false }
    )
    var y by y0

    val x1: Float
        get() = x + width

    val y1: Float
        get() = y + height

    init {
        settings.add(x0)
        settings.add(y0)
    }

    open fun render(event: Render2DEvent) {}

    fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return RenderUtils2D.isMouseOver(mouseX, mouseY, x, y, x1, y1)
    }
}