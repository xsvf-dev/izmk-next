package ovo.xsvf.izmk.injection.accessor;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.MethodAccessor;

@Accessor(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
    @MethodAccessor
    float getWhiteOverlayProgress(LivingEntity pLivingEntity, float pPartialTicks);
}
