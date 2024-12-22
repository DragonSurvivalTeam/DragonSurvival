package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(SmithingScreen.class)
public abstract class SmithingScreenMixin {
    @Unique private @Nullable DragonEntity dragonSurvival$dragon;

    /** Prepare the fake dragon which is to be rendered */
    @Inject(method = "subInit", at = @At("HEAD"))
    private void dragonSurvival$addDragonToInit(CallbackInfo ci) {
        if (DragonStateProvider.isDragon(Minecraft.getInstance().player)) {
            DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
            dragonSurvival$dragon = FakeClientPlayerUtils.getFakeDragon(1, handler);
            dragonSurvival$dragon.overrideUUIDWithLocalPlayerForTextureFetch = true;
            dragonSurvival$dragon.yBodyRot = 210.0F;
            dragonSurvival$dragon.setXRot(25.0F);
            FakeClientPlayerUtils.getFakePlayer(1, handler).animationSupplier = () -> "sit_head_locked";
        } else {
            dragonSurvival$dragon = null;
        }
    }

    /** Update the items of the fake dragon with the items from the player */
    @Inject(method = "updateArmorStandPreview", at = @At("HEAD"))
    private void dragonSurvival$updateFakeDragon(ItemStack pStack, CallbackInfo ci) {
        if (dragonSurvival$dragon != null) {
            for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                dragonSurvival$dragon.setItemSlot(equipmentslot, Objects.requireNonNull(Minecraft.getInstance().player).getItemBySlot(equipmentslot));
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

    /** Render the fake dragon instead of an armor stand */
    @ModifyArg(method = "renderBg", index = 7, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private LivingEntity dragonSurvival$renderDragon(LivingEntity armorStand) {
        if (dragonSurvival$dragon != null && DragonStateProvider.isDragon(Minecraft.getInstance().player)) {
            return dragonSurvival$dragon;
        }

        return armorStand;
    }
}
