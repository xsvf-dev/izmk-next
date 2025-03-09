package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(LivingEntity.class)
public class LivingEntityPatch {
    private static final Minecraft mc = Minecraft.getInstance();

    // FIXME: Caused by: java.lang.RuntimeException: java.lang.invoke.WrongMethodTypeException: cannot convert MethodHandle(Level,ParticleOptions,double,double,double,double,double,double)void to (Object,Object[])Object
//    @WrapInvoke(method = "tickEffects", desc = "()V", target = "net/minecraft/world/level/Level/addParticle", targetDesc = "(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
//    public static void wrapTickEffects(LivingEntity entity, Invocation invocation) throws Exception {
//        if (entity == mc.player && mc.options.getCameraType().isFirstPerson() &&
//                Particles.INSTANCE.getEnabled() && Particles.INSTANCE.getShowFirstPerson()) {
//            return;
//        }
//        invocation.call();
//    }
}
