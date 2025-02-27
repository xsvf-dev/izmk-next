package ovo.xsvf.izmk.injection.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import ovo.xsvf.izmk.event.impl.SendMessageEvent;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Inject(method = "sendChat", desc = "(Ljava/lang/String;)V")
    public static void sendChat(ClientPacketListener instance, String message, CallbackInfo ci) {
        ci.cancelled = new SendMessageEvent(message).post().isCancelled();
    }
}
