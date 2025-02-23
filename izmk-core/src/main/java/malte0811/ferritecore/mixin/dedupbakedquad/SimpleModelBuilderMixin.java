package malte0811.ferritecore.mixin.dedupbakedquad;

import malte0811.ferritecore.impl.Deduplicator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

@Mixin(SimpleBakedModel.Builder.class)
public class SimpleModelBuilderMixin {
    @Inject(method = "addUnculledFace", desc = "(Lnet/minecraft/client/renderer/block/model/BakedQuad;)Lnet/minecraft/client/renderer/block/model/SimpleBakedModel$Builder;")
    public static void deduplicate(SimpleBakedModel.Builder builder, BakedQuad quad, CallbackInfo cir) {
        Deduplicator.deduplicate(quad);
    }

    @Inject(method = "addCulledFace", desc = "(Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/model/BakedQuad;)Lnet/minecraft/client/renderer/block/model/SimpleBakedModel$Builder;")
    public static void deduplicate(SimpleBakedModel.Builder builder, Direction direction, BakedQuad quad, CallbackInfo cir) {
        Deduplicator.deduplicate(quad);
    }
}
