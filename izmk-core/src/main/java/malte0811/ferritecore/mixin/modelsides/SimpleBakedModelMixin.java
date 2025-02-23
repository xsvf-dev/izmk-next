package malte0811.ferritecore.mixin.modelsides;

import net.minecraft.client.resources.model.SimpleBakedModel;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;


@Mixin(SimpleBakedModel.class)
public class SimpleBakedModelMixin {
    //FIXME: Verify error
//    @Inject(method = "<init>", desc = "(Ljava/util/List;Ljava/util/Map;ZZZLnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/block/model/ItemTransforms;Lnet/minecraft/client/renderer/block/model/ItemOverrides;Lnet/minecraftforge/client/RenderTypeGroup;)V", at = @At(At.Type.TAIL))
//    public static void minimizeFaceList(SimpleBakedModelMixin instance, List<BakedQuad> p_119489_, Map<Direction, List<BakedQuad>> p_119490_, boolean p_119491_, boolean p_119492_, boolean p_119493_, TextureAtlasSprite p_119494_, ItemTransforms p_119495_, ItemOverrides p_119496_, RenderTypeGroup renderTypes, CallbackInfo ci) {
//        ((SimpleBakedModelAccess) instance).setUnculledFaces(ModelSidesImpl.minimizeUnculled(((SimpleBakedModelAccess) instance).getUnculledFaces()));
//        ((SimpleBakedModelAccess) instance).setCulledFaces(ModelSidesImpl.minimizeCulled(((SimpleBakedModelAccess) instance).getCulledFaces()));
//    }
}
