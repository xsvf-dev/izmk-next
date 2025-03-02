package ovo.xsvf.izmk.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.gui.widget.impl.setting.*
import ovo.xsvf.izmk.translation.TranslationString
import ovo.xsvf.izmk.util.input.KeyBind
import java.util.concurrent.CopyOnWriteArrayList

class BooleanSetting @JvmOverloads constructor(
    name: TranslationString,
    value: Boolean = false,
    visibility: () -> Boolean = { true }
) : AbstractSetting<Boolean>(name, value, visibility) {
    fun toggle() {
        value = !value
    }

    override fun toJson(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun fromJson(json: JsonElement) {
        value(json.asBoolean)
    }

    override fun createWidget(screen: GuiScreen): AbstractSettingWidget {
        return BooleanSettingWidget(screen, this)
    }
}

class TextSetting @JvmOverloads constructor(
    name: TranslationString,
    value: String = "",
    visibility: () -> Boolean = { true }
) : AbstractSetting<String>(name, value, visibility) {
    override fun toJson(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun fromJson(json: JsonElement) {
        value(json.asString)
    }

    override fun createWidget(screen: GuiScreen): AbstractSettingWidget {
        return TextSettingWidget(screen, this)
    }
}

class ColorSetting @JvmOverloads constructor(
    name: TranslationString,
    value: ColorRGB = ColorRGB.WHITE,
    visibility: () -> Boolean = { true }
) : AbstractSetting<ColorRGB>(name, value, visibility) {
    override fun toJson(): JsonElement {
        return JsonPrimitive(value.rgba)
    }

    override fun fromJson(json: JsonElement) {
        value(ColorRGB(json.asInt))
    }

    override fun createWidget(screen: GuiScreen): AbstractSettingWidget {
        return ColorSettingWidget(screen, this)
    }
}

class EnumSetting<E: Enum<E>> @JvmOverloads constructor(
    name: TranslationString,
    value: E,
    visibility: () -> Boolean = { true }
) : AbstractSetting<E>(name, value, visibility) {
    fun forwardLoop() {
        this.value = this.value::class.java.enumConstants[(value.ordinal + 1) % value::class.java.enumConstants.size]
    }

    fun setWithName(name: String) {
        value::class.java.enumConstants.forEach {
            if (it.name == name) value = it
        }
    }

    override fun toJson(): JsonElement {
        return JsonPrimitive(value.name)
    }

    override fun fromJson(json: JsonElement) {
        setWithName(json.asString)
    }

    override fun createWidget(screen: GuiScreen): AbstractSettingWidget {
        return EnumSettingWidget(screen, this)
    }
}

class KeyBindSetting @JvmOverloads constructor(
    name: TranslationString,
    value: KeyBind = KeyBind(KeyBind.Type.KEYBOARD, -1, 1),
    val method: () -> Unit = {},
    visibility: () -> Boolean = { true }
) : AbstractSetting<KeyBind>(name, value, visibility) {
    override fun toJson(): JsonElement {
        return JsonPrimitive(value.valueToString())
    }

    override fun fromJson(json: JsonElement) {
        value = KeyBind.fromString(json.asString)
    }

    override fun createWidget(screen: GuiScreen): AbstractSettingWidget {
        return KeybindSettingWidget(screen, this)
    }
}