package malte0811.ferritecore.mixin.mrl;

import malte0811.ferritecore.impl.Deduplicator;
import malte0811.ferritecore.mixin.accessors.ModelResourceLocationAccess;
import malte0811.ferritecore.mixin.accessors.ResourceLocationAccess;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.At;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

@Mixin(ModelResourceLocation.class)
public class ModelResourceLocationMixin {
    @Inject(method = "<init>", desc = "(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)V", at = @At(At.Type.TAIL))
    public static void constructTail(ModelResourceLocation instance, ResourceLocation location, String path, CallbackInfo ci) {
        // Do not use new strings for path and namespace, and deduplicate the variant string
        ((ResourceLocationAccess) instance).setPath(location.getPath());
        ((ResourceLocationAccess) instance).setNamespace(location.getNamespace());
        ((ModelResourceLocationAccess) instance).setVariant(Deduplicator.deduplicateVariant(((ModelResourceLocationAccess) instance).getVariant()));
    }
}
