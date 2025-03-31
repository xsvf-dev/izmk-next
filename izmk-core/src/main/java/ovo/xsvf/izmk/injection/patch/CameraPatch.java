package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import ovo.xsvf.izmk.injection.accessor.CameraAccessor;
import ovo.xsvf.izmk.module.impl.OldAnimations;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(Camera.class)
public class CameraPatch {
    @Inject(method = "setup", desc = "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", at = @At(At.Type.TAIL))
    public static void setup(Camera camera, BlockGetter pLevel, Entity pEntity,
                             boolean pDetached, boolean pThirdPersonReverse,
                             float pPartialTick, CallbackInfo callbackInfo) {
        if (OldAnimations.INSTANCE.getOldCamera()) {
            ((CameraAccessor) camera).move(-0.05000000074505806F, 0.0F, 0.0F);
            ((CameraAccessor) camera).move(0.1F, 0.0F, 0.0F);
            ((CameraAccessor) camera).move(-0.15F, 0, 0);
        }
    }
}
