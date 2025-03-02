package ovo.xsvf.izmk.injection.accessor;

import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;

@Accessor(ItemEntityRenderer.class)
public interface ItemEntityRendererAccessor {
    @FieldAccessor("itemRenderer")
    ItemRenderer getItemRenderer();
}
