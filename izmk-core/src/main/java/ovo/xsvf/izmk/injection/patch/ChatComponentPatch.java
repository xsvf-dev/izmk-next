package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.module.ModuleManager;
import ovo.xsvf.patchify.annotation.ModifyLocals;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.api.ILocals;

import java.awt.*;

@Patch(ChatComponent.class)
public class ChatComponentPatch {
    private static final Logger log = LogManager.getLogger(ChatComponentPatch.class);

    @ModifyLocals(method = "addMessage", desc = "(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            indexes = {1}, types = {Component.class})
    public static void onAddMessage(ILocals locals) {
        if (ModuleManager.INSTANCE.get("chat-copy").getEnabled()) {
            locals.set(1, onChatReceived((Component) locals.get(1)));
        }
    }

    public static Component onChatReceived(Component component) {
        Component copyComponent = Component.literal(" [C]").withStyle(
                Style.EMPTY
                        .withColor(new Color(18, 114, 126).getRGB())
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, component.getString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.nullToEmpty("Copy")))
        );

        return component.copy().append(copyComponent);
    }
}