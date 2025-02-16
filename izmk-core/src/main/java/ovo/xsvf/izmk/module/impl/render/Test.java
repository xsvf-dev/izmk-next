package ovo.xsvf.izmk.module.impl.render;

import ovo.xsvf.izmk.event.annotations.EventTarget;
import ovo.xsvf.izmk.event.impl.events.Render2DEvent;
import ovo.xsvf.izmk.module.Category;
import ovo.xsvf.izmk.module.Module;

/**
 * @author LangYa466
 * @since 2025/2/16
 */
public class Test extends Module {
    public Test() {
        super("Test", Category.Render);

        // Test code
        setEnabled(true);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        event.getGraphics().drawString(mc.font, "Hello, World!", 10, 10, 0xFFFFFFFF);
    }
}
