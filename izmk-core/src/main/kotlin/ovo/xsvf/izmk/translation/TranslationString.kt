package ovo.xsvf.izmk.translation

data class TranslationString(
    val key: TranslationKey,
) {
    constructor(prefix: String, key: String) : this(TranslationKey(prefix, key))

    val translation: String get() = TranslationManager.getTranslation(key)
}