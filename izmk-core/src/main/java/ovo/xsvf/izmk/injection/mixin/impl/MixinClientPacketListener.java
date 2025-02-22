package ovo.xsvf.izmk.injection.mixin.impl;

import net.minecraft.client.multiplayer.ClientPacketListener;
import ovo.xsvf.izmk.event.EventBus;
import ovo.xsvf.izmk.event.impl.SendMessageEvent;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {
    @Inject(method = "sendChat", desc = "(Ljava/lang/String;)V")
    public static void sendChat(ClientPacketListener instance, String message, CallbackInfo ci) {
        SendMessageEvent event = new SendMessageEvent(message);
        EventBus.INSTANCE.post(event);
        ci.cancelled = event.isCancelled();
    }
}
