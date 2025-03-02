package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.event.impl.KeyEvent;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(KeyboardHandler.class)
public class KeyboardHandlerPatch {
    @Inject(method = "keyPress", desc = "(JIIII)V")
    public static void keyPress(KeyboardHandler instance, long pWindowPointer, int pKey, int pScanCode,
                         int pAction, int pModifiers, CallbackInfo ci) {
        if (pKey == -1) return;
        new KeyEvent(pKey, pScanCode, pAction, pModifiers).post();
    }
}
