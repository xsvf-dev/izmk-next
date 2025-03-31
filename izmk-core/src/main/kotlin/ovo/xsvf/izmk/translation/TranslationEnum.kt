package ovo.xsvf.izmk.translation

interface TranslationEnum {

    val key: CharSequence

    val keyString: String
        get() = key.toString()

}

interface DirectTranslationEnum {

    val key: CharSequence

    val keyString: String
        get() = key.toString()

    val translation: String get() =
        TranslationManager.getTranslation(keyString)

}