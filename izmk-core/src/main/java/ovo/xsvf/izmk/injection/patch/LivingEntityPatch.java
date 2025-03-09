package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ovo.xsvf.izmk.module.impl.Particles;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.WrapInvoke;
import ovo.xsvf.patchify.api.Invocation;

@Patch(LivingEntity.class)
public class LivingEntityPatch {
    private static final Minecraft mc = Minecraft.getInstance();

    @WrapInvoke(method = "tickEffects", desc = "()V", target = "net/minecraft/world/level/Level/addParticle", targetDesc = "(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    public static void wrapTickEffects(LivingEntity entity, Invocation<Level, Void> invocation) throws Exception {
        if (entity == mc.player && mc.options.getCameraType().isFirstPerson() &&
                Particles.INSTANCE.getEnabled() && Particles.INSTANCE.getShowFirstPerson()) {
            return;
        }
        invocation.call();
    }
}
