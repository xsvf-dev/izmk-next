package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.event.impl.SendMessageEvent;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(ClientPacketListener.class)
public class ClientPacketListenerPatch {
    private static final Logger log = LogManager.getLogger(ClientPacketListenerPatch.class);

    @Inject(method = "sendChat", desc = "(Ljava/lang/String;)V")
    public static void sendChat(ClientPacketListener instance, String message, CallbackInfo ci) {
        log.info("Sending chat message: {}", message);
        ci.cancelled = new SendMessageEvent(message).post().isCancelled();
    }
}
