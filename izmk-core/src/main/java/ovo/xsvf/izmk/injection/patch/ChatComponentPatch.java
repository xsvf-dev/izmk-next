package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.module.impl.ChatCopy;
import ovo.xsvf.patchify.annotation.ModifyLocals;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.api.ILocals;

@Patch(ChatComponent.class)
public class ChatComponentPatch {
    private static final Logger log = LogManager.getLogger(ChatComponentPatch.class);

    @ModifyLocals(method = "addMessage", desc = "(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V",
            indexes = {1}, types = {Component.class})
    public static void onAddMessage(ILocals locals) {
        if (ChatCopy.INSTANCE.getEnabled()) {
            locals.set(1, ChatCopy.INSTANCE.process((Component) locals.get(1)));
        }
    }
}