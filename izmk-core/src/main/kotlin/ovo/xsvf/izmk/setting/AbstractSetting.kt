package ovo.xsvf.izmk.setting

abstract class AbstractSetting<T> (
    val name: String,
    defaultValue: T,
    val visibility: () -> Boolean = { true }
) {
    var value: T = defaultValue
        set(value) {
            field = value
            validators.forEach {
                if (!it(this.value, value)) {
                    return
                }
            }
            callbacks.forEach { it() }
        }

    private val callbacks: MutableList<() -> Unit> = mutableListOf()
    private val validators: MutableList<(T, T) -> Boolean> = mutableListOf()

    fun addCallback(callback: () -> Unit) {
        callbacks.add(callback)
    }

    fun addValidator(validator: (T, T) -> Boolean) {
        validators.add(validator)
    }
}