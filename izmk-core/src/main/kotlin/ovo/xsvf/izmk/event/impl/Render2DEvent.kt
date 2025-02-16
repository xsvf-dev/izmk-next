package ovo.xsvf.izmk.event.impl.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import ovo.xsvf.izmk.event.impl.Event;

/**
 * @author LangYa466
 * @since 2025/2/16
 */
@AllArgsConstructor
@Getter
@Setter
public class Render2DEvent implements Event {
    private final GuiGraphics graphics;
    private final float partialTick;
}
