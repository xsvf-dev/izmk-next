package malte0811.ferritecore.mixin.dedupbakedquad;

import malte0811.ferritecore.impl.Deduplicator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

@Mixin(SimpleBakedModel.Builder.class)
public class SimpleModelBuilderMixin {
    @Inject(method = "addUnculledFace", desc = "(Lnet/minecraft/client/renderer/block/model/BakedQuad;)Lnet/minecraft/client/renderer/block/model/SimpleBakedModel$Builder;")
    public void deduplicate(BakedQuad quad, CallbackInfoReturnable<SimpleBakedModel.Builder> cir) {
        Deduplicator.deduplicate(quad);
    }

    @Inject(method = "addCulledFace", desc = "(Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/model/BakedQuad;)Lnet/minecraft/client/renderer/block/model/SimpleBakedModel$Builder;")
    public void deduplicate(Direction direction, BakedQuad quad, CallbackInfoReturnable<SimpleBakedModel.Builder> cir) {
        Deduplicator.deduplicate(quad);
    }
}
