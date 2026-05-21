package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonAnimations;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmithingScreen.class)
public abstract class SmithingScreenMixin extends ItemCombinerScreen<SmithingMenu> {
    @Unique private static final Identifier dragonSurvival$smithingTexture = Identifier.withDefaultNamespace("textures/gui/container/smithing.png");
    @Unique private static final int dragonSurvival$smithingFakePlayerIndex = 1;
    @Unique private static final float dragonSurvival$smithingBodyRotation = 210.0F;
    @Unique private static final float dragonSurvival$smithingPitch = 25.0F;
    @Unique private @Nullable DragonEntity dragonSurvival$dragon;

    public SmithingScreenMixin(final SmithingMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title, dragonSurvival$smithingTexture);
    }

    @WrapOperation(
        method = "extractBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;entity(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;IIII)V"
        )
    )
    private void dragonSurvival$renderDragonPreview(
        final GuiGraphicsExtractor graphics,
        final EntityRenderState renderState,
        final float size,
        final Vector3f translation,
        final Quaternionf rotation,
        final Quaternionf orientation,
        final int x0,
        final int y0,
        final int x1,
        final int y1,
        final Operation<Void> original
    ) {
        Player player = Minecraft.getInstance().player;

        if (!DragonStateProvider.isDragon(player)) {
            original.call(graphics, renderState, size, translation, rotation, orientation, x0, y0, x1, y1);
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        dragonSurvival$dragon = FakeClientPlayerUtils.getFakeDragon(dragonSurvival$smithingFakePlayerIndex, handler);

        FakeClientPlayer fakePlayer = FakeClientPlayerUtils.getFakePlayer(dragonSurvival$smithingFakePlayerIndex, handler);
        fakePlayer.animationSupplier = DragonAnimations.SIT::getAnimationName;
        dragonSurvival$copyEquipment(player, fakePlayer);
        dragonSurvival$applyPreviewItem(fakePlayer, this.menu.getSlot(3).getItem());
        dragonSurvival$preparePreviewEntity(player, fakePlayer, dragonSurvival$dragon);

        LivingEntityRenderState dragonRenderState = (LivingEntityRenderState)DragonRenderer.createUIRenderState(
            dragonSurvival$dragon,
            DragonSurvival.PROXY.getPartialTick(),
            0.0F,
            0.0F,
            0.0F,
            player
        );

        float scale = (float)player.getAttribute(Attributes.SCALE).getValue();
        EntityDimensions dimensions = DragonSizeHandler.calculateDimensions(handler, player, handler.previousPose);
        dragonRenderState.boundingBoxWidth = dimensions.width() / scale;
        dragonRenderState.boundingBoxHeight = dimensions.height() / scale;
        dragonRenderState.scale = 1.0F;
        dragonRenderState.bodyRot = dragonSurvival$smithingBodyRotation;
        dragonRenderState.yRot = 0.0F;
        dragonRenderState.xRot = dragonSurvival$smithingPitch;
        graphics.entity(dragonRenderState, size, translation, rotation, orientation, x0, y0, x1, y1);
    }

    @Unique
    private void dragonSurvival$copyEquipment(final Player source, final FakeClientPlayer target) {
        target.setItemSlot(EquipmentSlot.HEAD, source.getItemBySlot(EquipmentSlot.HEAD).copy());
        target.setItemSlot(EquipmentSlot.CHEST, source.getItemBySlot(EquipmentSlot.CHEST).copy());
        target.setItemSlot(EquipmentSlot.LEGS, source.getItemBySlot(EquipmentSlot.LEGS).copy());
        target.setItemSlot(EquipmentSlot.FEET, source.getItemBySlot(EquipmentSlot.FEET).copy());
        target.setItemSlot(EquipmentSlot.MAINHAND, source.getItemBySlot(EquipmentSlot.MAINHAND).copy());
        target.setItemSlot(EquipmentSlot.OFFHAND, source.getItemBySlot(EquipmentSlot.OFFHAND).copy());
    }

    @Unique
    private void dragonSurvival$applyPreviewItem(final FakeClientPlayer fakePlayer, final ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack copied = stack.copy();
        Equippable equippable = copied.get(DataComponents.EQUIPPABLE);
        EquipmentSlot slot = equippable != null ? equippable.slot() : null;

        if (slot != null) {
            fakePlayer.setItemSlot(slot, copied);
        } else {
            fakePlayer.setItemSlot(EquipmentSlot.OFFHAND, copied);
        }
    }

    @Unique
    private void dragonSurvival$preparePreviewEntity(final Player source, final FakeClientPlayer fakePlayer, final DragonEntity dragon) {
        Vec3 position = source.position();
        int tickCount = source.tickCount;

        dragon.setPos(position);
        dragon.xOld = position.x();
        dragon.yOld = position.y();
        dragon.zOld = position.z();
        dragon.xo = position.x();
        dragon.yo = position.y();
        dragon.zo = position.z();
        dragon.tickCount = tickCount;
        dragon.setYRot(0.0F);
        dragon.setYBodyRot(0.0F);
        dragon.setXRot(0.0F);
        dragon.yRotO = 0.0F;
        dragon.yBodyRotO = 0.0F;
        dragon.xRotO = 0.0F;
        dragon.prevXRot = 0.0F;
        dragon.prevZRot = 0.0F;

        fakePlayer.tickCount = tickCount;
    }
}
