package ovo.xsvf.izmk.event.impl;

/**
 * @author LangYa466
 * @since 2025/2/16
 */
public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean state);
}
