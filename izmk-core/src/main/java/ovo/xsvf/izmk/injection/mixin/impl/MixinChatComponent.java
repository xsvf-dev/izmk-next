package ovo.xsvf.izmk.injection.mixin.impl;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.event.EventBus;
import ovo.xsvf.izmk.event.impl.SendMessageEvent;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.*;


/**
 * @author LangYa466
 * @since 2025/2/19
 */
@Mixin(ChatComponent.class)
public class MixinChatComponent {

    @Inject(method = "addMessage", desc = "(Lnet/minecraft/network/chat/Component;)V")
    public static void addMessage(Component pChatComponent, CallbackInfo ci) {
        SendMessageEvent sendMessageEvent = new SendMessageEvent(pChatComponent);
        EventBus.INSTANCE.post(sendMessageEvent);
        IZMK.logger.debug("Received chat message: " + sendMessageEvent.getComponent().getString());
        ci.cancelled = sendMessageEvent.isCancelled();
    }
}
