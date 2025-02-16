package ovo.xsvf.izmk.event;

import lombok.Getter;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.event.impl.Event;

import java.lang.reflect.Method;

/**
 * @author LangYa466
 * @since 2025/2/16
 */
public record MethodWrapper(Object obj, Method method, @Getter int priority) {

    public void invoke(Event event) {
        try {
            method.setAccessible(true);
            method.invoke(obj, event);
        } catch (Exception e) {
            IZMK.logger.error(e);
        }
    }

    public boolean matches(Object obj, Method method) {
        return this.obj.equals(obj) && this.method.equals(method);
    }
}