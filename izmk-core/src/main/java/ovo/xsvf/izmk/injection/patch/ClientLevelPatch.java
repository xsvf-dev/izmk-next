package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import ovo.xsvf.izmk.module.impl.Particles;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(ClientLevel.class)
public class ClientLevelPatch {
    @Inject(method = "addDestroyBlockEffect", desc = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
    public static void addDestroyBlockEffect(ClientLevel level, BlockPos pos, BlockState state, CallbackInfo ci) {
        ci.cancelled = Particles.INSTANCE.getEnabled() && Particles.INSTANCE.getBlockBreaking();
    }
}
