package ovo.xsvf.izmk.injection.mixin.impl;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import ovo.xsvf.izmk.event.EventBus;
import ovo.xsvf.izmk.event.impl.SendMessageEvent;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.*;

import javax.annotation.Nullable;
import java.util.Arrays;


/**
 * @author LangYa466
 * @since 2025/2/19
 */
@Mixin(ChatComponent.class)
public class MixinChatComponent {

    @Inject(method = "addMessage", desc = "(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V")
    public static void addMessage(Component pChatComponent, @Nullable MessageSignature pHeaderSignature, @Nullable GuiMessageTag pTag, CallbackInfo ci) {
        String[] split = pChatComponent.getString().split(" ");
        if (split.length > 1) {
            String message = String.join(" ", Arrays.asList(split).subList(1, split.length));
            SendMessageEvent sendMessageEvent = new SendMessageEvent(message);
            EventBus.INSTANCE.post(sendMessageEvent);
            ci.cancelled = sendMessageEvent.isCancelled();
        }
    }
}
