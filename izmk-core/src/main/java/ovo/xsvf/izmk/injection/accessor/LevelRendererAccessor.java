package ovo.xsvf.izmk.injection.accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;

@Accessor(LevelRenderer.class)
public interface LevelRendererAccessor {
    @FieldAccessor("level")
    ClientLevel level();
}
