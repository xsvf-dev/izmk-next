package ovo.xsvf.izmk.event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventTarget (
    val priority: Int = 0,
    val alwaysListening: Boolean = false
)
