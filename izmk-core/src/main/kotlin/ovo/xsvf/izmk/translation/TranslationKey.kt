package ovo.xsvf.izmk.translation

class TranslationKey(
    prefix0: String,
    key0: String,
) {
    constructor(fullKey: String) : this(
        fullKey.substringBeforeLast('.'),
        fullKey.substringAfterLast('.')
    )

    var prefix: String = prefix0
        set(value) {
            field = value
            fullKey = "$value.$key"
        }

    var key: String = key0
        set(value) {
            field = value
            fullKey = "$prefix.$value"
        }

    var fullKey: String = "$prefix.$key"

    override fun equals(other: Any?): Boolean {
        if (other !is TranslationKey) return false
        return fullKey == other.fullKey
    }

    override fun hashCode(): Int = fullKey.hashCode()

    override fun toString(): String = fullKey
}