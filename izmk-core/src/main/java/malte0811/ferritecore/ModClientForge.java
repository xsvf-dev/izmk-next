package malte0811.ferritecore;

import malte0811.ferritecore.impl.Deduplicator;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModClientForge {
    @SubscribeEvent
    public static void registerReloadListener(RenderLevelStageEvent.RegisterStageEvent ev) {
        Deduplicator.registerReloadListener();
    }

    /**
     * Called by IZMK Entry to register the event listener
     */
    public static void init() {
        MinecraftForge.EVENT_BUS.register(ModClientForge.class);
    }
}
