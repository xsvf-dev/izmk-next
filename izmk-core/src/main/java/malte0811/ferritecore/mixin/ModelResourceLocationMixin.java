package malte0811.ferritecore.mixin;

import malte0811.ferritecore.impl.Deduplicator;
import malte0811.ferritecore.mixin.accessors.ModelResourceLocationAccess;
import malte0811.ferritecore.mixin.accessors.ResourceLocationAccess;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(ModelResourceLocation.class)
public class ModelResourceLocationMixin {
    @Inject(method = "<init>", desc = "(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)V", at = @At(At.Type.TAIL))
    public static void constructTail(ModelResourceLocation instance, ResourceLocation location, String path, CallbackInfo ci) {
        // Do not use new strings for path and namespace, and deduplicate the variant string
        ((ResourceLocationAccess) instance).setPath(location.getPath());
        ((ResourceLocationAccess) instance).setNamespace(location.getNamespace());
        ((ModelResourceLocationAccess) instance).setVariant(Deduplicator.deduplicateVariant(((ModelResourceLocationAccess) instance).getVariant()));
    }
}
