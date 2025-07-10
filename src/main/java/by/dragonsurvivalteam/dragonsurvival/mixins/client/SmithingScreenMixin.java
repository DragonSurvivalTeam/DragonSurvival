package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.SmithingScreenHandler;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
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

@Mixin(SmithingScreen.class)
public abstract class SmithingScreenMixin {
    @Unique private @Nullable DragonEntity dragonSurvival$dragon;

    /** Prepare the fake dragon which is to be rendered */
    @Inject(method = "subInit", at = @At("HEAD"))
    private void dragonSurvival$addDragonToInit(final CallbackInfo callback) {
        if (DragonStateProvider.isDragon(Minecraft.getInstance().player)) {
            DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
            dragonSurvival$dragon = FakeClientPlayerUtils.getFakeDragon(SmithingScreenHandler.FAKE_PLAYER, handler);
            FakeClientPlayerUtils.getFakePlayer(SmithingScreenHandler.FAKE_PLAYER, handler).animationSupplier = () -> "sit";
        } else {
            dragonSurvival$dragon = null;
        }
    }

    /** Update the items of the fake dragon with the items from the player */
    @Inject(method = "updateArmorStandPreview", at = @At("HEAD"))
    private void dragonSurvival$updateFakeDragon(final ItemStack stack, final CallbackInfo callback) {
        if (dragonSurvival$dragon != null) {
            //noinspection DataFlowIssue -> player is present
            DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
            FakeClientPlayer player = FakeClientPlayerUtils.getFakePlayer(1, handler);

            SmithingScreenHandler.copyEquipment(Minecraft.getInstance().player, player);

            if (!stack.isEmpty()) {
                ItemStack copied = stack.copy();

                if (stack.getItem() instanceof ArmorItem armor) {
                    player.setItemSlot(armor.getEquipmentSlot(), copied);
                } else {
                    player.setItemSlot(EquipmentSlot.OFFHAND, copied);
                }
            }
        }
    }

    /** Render the fake dragon instead of an armor stand */
    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V"), index = 7)
    private LivingEntity dragonSurvival$renderDragon(final LivingEntity armorStand) {
        if (dragonSurvival$dragon != null && DragonStateProvider.isDragon(Minecraft.getInstance().player)) {
            dragonSurvival$dragon.overrideUUIDWithLocalPlayerForTextureFetch = true;
            dragonSurvival$dragon.isInInventory = true;
            dragonSurvival$dragon.yBodyRot = 210;
            dragonSurvival$dragon.setXRot(25);
            return dragonSurvival$dragon;
        }

        return armorStand;
    }

    @Inject(method = "renderBg", at = @At("RETURN"))
    private void dragonSurvival$resetUUIDOverwriteFlag(final CallbackInfo callback) {
        if (dragonSurvival$dragon != null) {
            dragonSurvival$dragon.overrideUUIDWithLocalPlayerForTextureFetch = false;
            dragonSurvival$dragon.isInInventory = false;
            dragonSurvival$dragon.yBodyRot = 0;
            dragonSurvival$dragon.setXRot(0);
        }
    }
}
