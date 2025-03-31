package ovo.xsvf.izmk.injection.patch;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import ovo.xsvf.izmk.module.impl.OldAnimations;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(Item.class)
public class ItemPatch {
    @Inject(method = "use", desc = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;")
    public static void use(Item item, Level level, Player player, InteractionHand hand, CallbackInfo callbackInfo) {
        if (OldAnimations.INSTANCE.getSwordBlocking() && item instanceof SwordItem) {
            callbackInfo.result = InteractionResultHolder.pass(player.getItemInHand(hand));
            callbackInfo.cancelled = true;
        }
    }

    @Inject(method = "getUseAnimation", desc = "(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/UseAnim;")
    public static void getUseAnimation(Item item, ItemStack itemStack, CallbackInfo callbackInfo) {
        if (item instanceof SwordItem && OldAnimations.INSTANCE.getSwordBlocking()) {
            callbackInfo.result = UseAnim.BLOCK;
            callbackInfo.cancelled = true;
        }
    }

    @Inject(method = "getUseDuration", desc = "(Lnet/minecraft/world/item/ItemStack;)I")
    public static void getUseDuration(Item item, ItemStack itemStack, CallbackInfo callbackInfo) {
        if (item instanceof SwordItem && OldAnimations.INSTANCE.getSwordBlocking()) {
            callbackInfo.result = 72000;
            callbackInfo.cancelled = true;
        }
    }
}
