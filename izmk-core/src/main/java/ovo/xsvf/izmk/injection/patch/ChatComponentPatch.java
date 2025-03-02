package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.module.ModuleManager;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

import javax.annotation.Nullable;
import java.awt.*;

@Patch(ChatComponent.class)
public class ChatComponentPatch {

    @Inject(method = "addMessage", desc = "(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V")
    public static void onAddMessage(ChatComponent self, Component pChatComponent, @Nullable MessageSignature pHeaderSignature, @Nullable GuiMessageTag pTag, CallbackInfo ci) {
        IZMK.INSTANCE.getLog().debug("Chat message: " + pChatComponent.getString());
        ci.result = onChatReceived(pChatComponent);
    }

    public static Component onChatReceived(Component component) {
        if (!ModuleManager.INSTANCE.get("chat-copy").getEnabled()) return component;
        Component copyComponent = Component.literal(" [C]").withStyle(
                Style.EMPTY
                        .withColor(new Color(18, 114, 126).getRGB())
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, component.getString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.nullToEmpty("Copy")))
        );

        return component.copy().append(copyComponent);
    }
}