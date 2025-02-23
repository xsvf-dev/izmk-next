package malte0811.ferritecore.mixin.accessors;

import net.minecraft.client.renderer.block.model.multipart.AndCondition;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.FieldAccessor;
import ovo.xsvf.izmk.injection.accessor.annotation.Final;

@Accessor(AndCondition.class)
public interface AndConditionAccess {
    @Final
    @FieldAccessor("conditions")
    Iterable<? extends Condition> getConditions();

    @Final
    @FieldAccessor(value = "conditions", getter = false)
    void setConditions(Iterable<? extends Condition> conditions);
}
