package malte0811.ferritecore.mixin.accessors;

import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.client.renderer.block.model.multipart.OrCondition;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.FieldAccessor;
import ovo.xsvf.izmk.injection.accessor.annotation.Final;

@Accessor(OrCondition.class)
public interface OrConditionAccess {
    @Final
    @FieldAccessor("conditions")
    Iterable<? extends Condition> getConditions();

    @Final
    @FieldAccessor(value = "conditions", getter = false)
    void setConditions(Iterable<? extends Condition> conditions);
}
