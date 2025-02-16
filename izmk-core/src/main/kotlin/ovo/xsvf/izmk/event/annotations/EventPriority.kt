package ovo.xsvf.izmk.event.annotations

/**
 * @author LangYa466
 * @since 2025/2/16
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class EventPriority(val value: Int = 10)