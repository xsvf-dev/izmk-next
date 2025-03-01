package ovo.xsvf.izmk.settings

import com.google.gson.JsonElement
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.gui.widget.impl.setting.NumberSettingWidget
import ovo.xsvf.izmk.translation.TranslationString

abstract class NumberSetting<N: Number>(
    name: TranslationString,
    value: N,
    val minValue: N,
    val maxValue: N,
    val step: N,
    visibility: () -> Boolean
) : AbstractSetting<N>(name, value, visibility) {
    override fun createWidget(screen: GuiScreen): AbstractSettingWidget {
        return NumberSettingWidget(screen, this)
    }
}

class IntSetting(
    name: TranslationString,
    value: Int,
    minValue: Int,
    maxValue: Int,
    step: Int,
    visibility: () -> Boolean
) : NumberSetting<Int>(name, value, minValue, maxValue, step, visibility) {
    override fun setWithJson(json: JsonElement) {
        value(json.asInt)
    }
}

class LongSetting(
    name: TranslationString,
    value: Long,
    minValue: Long,
    maxValue: Long,
    step: Long,
    visibility: () -> Boolean
) : NumberSetting<Long>(name, value, minValue, maxValue, step, visibility) {
    override fun setWithJson(json: JsonElement) {
        value(json.asLong)
    }
}

class FloatSetting(
    name: TranslationString,
    value: Float,
    minValue: Float,
    maxValue: Float,
    step: Float,
    visibility: () -> Boolean
) : NumberSetting<Float>(name, value, minValue, maxValue, step, visibility) {
    override fun setWithJson(json: JsonElement) {
        value(json.asFloat)
    }
}

class DoubleSetting(
    name: TranslationString,
    value: Double,
    minValue: Double,
    maxValue: Double,
    step: Double,
    visibility: () -> Boolean
) : NumberSetting<Double>(name, value, minValue, maxValue, step, visibility) {
    override fun setWithJson(json: JsonElement) {
        value(json.asDouble)
    }
}