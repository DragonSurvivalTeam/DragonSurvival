package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreen.class)
public class SmithingScreenMixin {
    @Unique private static DragonEntity dragonSurvival$dragon;
    @Unique
    private Player dragonSurvival$player;

    @Inject(method="subInit", at=@At("HEAD"))
    private void addDragonToInit(CallbackInfo ci){
        DragonStateHandler handler = new DragonStateHandler();
        dragonSurvival$player = ((SmithingScreen)(Object)this).getMinecraft().player;
        if (DragonStateProvider.isDragon(dragonSurvival$player)) {
            handler.setBody(DragonStateProvider.getOrGenerateHandler(dragonSurvival$player).getBody());
        }
        dragonSurvival$dragon = FakeClientPlayerUtils.getFakeDragon(1, handler);
        dragonSurvival$dragon.yBodyRot = 210.0F;
        dragonSurvival$dragon.setXRot(25.0F);
        FakeClientPlayerUtils.getFakePlayer(1, handler).animationSupplier = () -> "sit_head_locked";
    }

    @Inject(method="updateArmorStandPreview", at=@At("HEAD"))
    private void dragonSurvival$updateFakeDragon(ItemStack pStack, CallbackInfo ci){
        if (dragonSurvival$dragon != null) {
            for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                dragonSurvival$dragon.setItemSlot(equipmentslot, dragonSurvival$player.getItemBySlot(equipmentslot));
            }

            if (!pStack.isEmpty()) {
                ItemStack itemstack = pStack.copy();
                if (pStack.getItem() instanceof ArmorItem armoritem) {
                    dragonSurvival$dragon.setItemSlot(armoritem.getEquipmentSlot(), itemstack);
                } else {
                    dragonSurvival$dragon.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
                }
            }
        }
    }

    @ModifyArg(method="renderBg", index = 7, at=@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private LivingEntity d(LivingEntity pEntity){
        if (DragonStateProvider.isDragon(((SmithingScreen)(Object)this).getMinecraft().player)) {
            return dragonSurvival$dragon;
        }
        return pEntity;
    }
}
