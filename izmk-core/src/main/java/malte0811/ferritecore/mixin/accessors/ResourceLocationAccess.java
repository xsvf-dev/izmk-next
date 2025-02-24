package malte0811.ferritecore.mixin.accessors;

import net.minecraft.resources.ResourceLocation;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;

@Accessor(ResourceLocation.class)
public interface ResourceLocationAccess {
    @FieldAccessor(value = "namespace", getter = false)
    void setNamespace(String newNamespace);

    @FieldAccessor(value = "path", getter = false)
    void setPath(String newPath);
}
