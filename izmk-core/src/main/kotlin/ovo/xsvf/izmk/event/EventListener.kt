package ovo.xsvf.izmk.event

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventListener(
    val priority: Int = 0,
    val alwaysListening: Boolean = false
)
