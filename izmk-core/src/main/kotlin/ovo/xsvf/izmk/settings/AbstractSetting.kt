package ovo.xsvf.izmk.settings

import com.google.gson.JsonElement
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.translation.TranslationString
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class AbstractSetting<T>(
    var name: TranslationString,
    var value: T,
    var visibility: () -> Boolean
) : ReadWriteProperty<Any, T> {
    private val defaultValue = value

    private val changeValueConsumers = CopyOnWriteArrayList<() -> Unit>()

    val settingId: String
        get() = "$name@${this::class.simpleName}"

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        value(value)
    }

    fun onChangeValue(run: () -> Unit): AbstractSetting<*> {
        return this.apply { changeValueConsumers.add(run) }
    }

    fun default() {
        value = defaultValue
    }

    //Builder
    fun key(key: TranslationString): AbstractSetting<T> {
        this.name = key
        return this
    }

    fun value(value: T): AbstractSetting<T> {
        this.value = value
        changeValueConsumers.forEach { it.invoke() }
        return this
    }

    fun visibility(visibility: () -> Boolean): AbstractSetting<T> {
        this.visibility = visibility
        return this
    }

    abstract fun toJson(): JsonElement
    abstract fun fromJson(json: JsonElement)

    abstract fun createWidget(screen: GuiScreen): AbstractSettingWidget
}