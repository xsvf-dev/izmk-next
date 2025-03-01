//package ovo.xsvf.izmk.injection.patch;
//
//import net.minecraft.client.GuiMessageTag;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.components.ChatComponent;
//import net.minecraft.network.chat.Component;
//
//import ovo.xsvf.izmk.module.ModuleManager;
//import ovo.xsvf.patchify.CallbackInfo;
//import ovo.xsvf.patchify.annotation.Patch;
//import ovo.xsvf.patchify.annotation.Overwrite;
//
//@Patch(ChatComponent.class)
//public class MixinChatComponent {
//    @Overwrite(method = "addMessage", desc = "(Lnet/minecraft/network/chat/Component;)V")
//    public static void addMessage(ChatComponent self, Component pChatComponent, CallbackInfo callbackInfo) {
//        Component modifiedComponent = ((ChatCopy)ModuleManager.INSTANCE.get("chat-copy")).onChatReceived(pChatComponent);
//        self.addMessage(modifiedComponent, null,
//                Minecraft.getInstance().isSingleplayer() ? GuiMessageTag.systemSinglePlayer() : GuiMessageTag.system());
//    }
//}