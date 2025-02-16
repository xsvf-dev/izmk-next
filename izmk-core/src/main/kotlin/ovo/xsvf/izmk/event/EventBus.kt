package ovo.xsvf.izmk.event;

import ovo.xsvf.izmk.event.annotations.EventPriority;
import ovo.xsvf.izmk.event.annotations.EventTarget;
import ovo.xsvf.izmk.event.impl.Event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author LangYa466
 * @since 2025/2/16
 */
public class EventManager {
    private static EventManager INSTANCE;

    public static EventManager getInstance() {
        if (INSTANCE == null) INSTANCE = new EventManager();
        return INSTANCE;
    }

    private final Map<Class<? extends Event>, List<MethodWrapper>> eventMethods = new ConcurrentHashMap<>();

    /** 注册事件监听对象 */
    public void register(Object... objs) {
        for (Object obj : objs) register(obj);
    }

    private void register(Object obj) {
        Arrays.stream(obj.getClass().getDeclaredMethods())
                .filter(this::isValidEventMethod)
                .forEach(method -> registerMethod(obj, method));
    }

    private boolean isValidEventMethod(Method method) {
        return method.isAnnotationPresent(EventTarget.class) && method.getParameterCount() == 1;
    }

    private void registerMethod(Object obj, Method method) {
        Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
        int priority = Optional.ofNullable(method.getAnnotation(EventPriority.class)).map(EventPriority::value).orElse(10);
        eventMethods.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                .add(new MethodWrapper(obj, method, priority));
    }

    /** 取消注册事件监听对象 */
    public void unregister(Object obj) {
        Arrays.stream(obj.getClass().getDeclaredMethods())
                .filter(this::isValidEventMethod)
                .forEach(method -> unregisterMethod(obj, method));
    }

    private void unregisterMethod(Object obj, Method method) {
        List<MethodWrapper> methods = eventMethods.get((Class<? extends Event>) method.getParameterTypes()[0]);
        if (methods != null) methods.removeIf(wrapper -> wrapper.matches(obj, method));
    }

    /** 调用事件 */
    public Event call(Event event) {
        List<MethodWrapper> methods = eventMethods.get(event.getClass());
        if (methods != null) {
            methods.stream().sorted(Comparator.comparingInt(MethodWrapper::priority))
                    .forEach(wrapper -> wrapper.invoke(event));
        }
        return event;
    }
}
