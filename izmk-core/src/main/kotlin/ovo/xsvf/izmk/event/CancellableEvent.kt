package ovo.xsvf.izmk.event.impl;

/**
 * @author LangYa466
 * @since 2025/2/16
 */
public abstract class CancellableEvent implements Event, Cancellable {
	private boolean cancelled;

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
}
