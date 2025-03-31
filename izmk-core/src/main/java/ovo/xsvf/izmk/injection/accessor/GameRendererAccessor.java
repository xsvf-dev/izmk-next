package ovo.xsvf.izmk.injection.accessor;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;
import ovo.xsvf.patchify.annotation.Final;

@Accessor(GameRenderer.class)
public interface GameRendererAccessor {
    @Final
    @FieldAccessor(value = "overlayTexture", getter = false)
    void setOverlayTexture(OverlayTexture overlayTexture);
}
