package ovo.xsvf.izmk.module;

import lombok.Getter;
import lombok.Setter;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.event.EventManager;

/**
 * @author LangYa466
 * @since 2025/2/16
 */
@Getter
@Setter
public class Module extends IZMK {
    private final String name;
    private final Category category;
    private final String description;
    private boolean enabled;

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
        this.description = "";
    }

    public Module(String name, Category category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public void toggle() {
        this.enabled = !enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) EventManager.getInstance().register(this);
        else EventManager.getInstance().unregister(this);
    }
}
