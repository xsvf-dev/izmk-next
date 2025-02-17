package ovo.xsvf.izmk.event

import ovo.xsvf.izmk.IZMK
import java.lang.reflect.Method

/**
 * @author LangYa466, xsvf
 * @since 2025/2/16
 */
data class MethodWrapper(val obj: Any, val method: Method, val priority: Int) {
    fun invoke(event: Event) {
        try {
            method.invoke(obj, event)
        } catch (e: Exception) {
            IZMK.logger?.error(e)
        }
    }

    fun matches(obj: Any, method: Method): Boolean {
        return this.obj == obj && this.method == method
    }
}