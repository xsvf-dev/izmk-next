package ovo.xsvf.izmk.translation

import ovo.xsvf.izmk.module.impl.ClickGUI

object TranslationManager {
    private val language get() = ClickGUI.language

    val en = TranslationMap("en_us")
    val cn = TranslationMap("zh_cn")

    val defaultMap get() = cn

    fun getTranslation(key: String): String {
        return when (language) {
            Language.EN_US -> en[TranslationKey(key)] ?: key
            Language.ZH_CN -> cn[TranslationKey(key)] ?: key
        }
    }

    fun getTranslation(key: TranslationKey): String {
        return when (language) {
            Language.EN_US -> en[key] ?: key.key
            Language.ZH_CN -> cn[key] ?: key.key
        }
    }

    fun getMapFromLanguage(language: String): TranslationMap =
        when (language) {
            "en_us" -> en
            "zh_cn" -> cn
            else -> en
        }
    
    enum class Language(override val key: CharSequence): DirectTranslationEnum {
        EN_US("languages.en_us"),
        ZH_CN("languages.zh_cn"),
    }
}