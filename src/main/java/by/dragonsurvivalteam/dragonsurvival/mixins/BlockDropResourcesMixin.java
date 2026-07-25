package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonBonusHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Block.class)
public class BlockDropResourcesMixin {
    @Unique private static final ThreadLocal<Entity> dragonSurvival$dropBreaker = new ThreadLocal<>();

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("HEAD"), remap = false)
    private static void dragonSurvival$trackDropBreaker(final BlockState state, final Level level, final BlockPos pos, final BlockEntity blockEntity,
                                                        final Entity entity, final ItemStack tool, final boolean dropXp, final CallbackInfo callback) {
        dragonSurvival$dropBreaker.set(entity);
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("RETURN"), remap = false)
    private static void dragonSurvival$clearDropBreaker(final BlockState state, final Level level, final BlockPos pos, final BlockEntity blockEntity,
                                                        final Entity entity, final ItemStack tool, final boolean dropXp, final CallbackInfo callback) {
        dragonSurvival$dropBreaker.remove();
    }

    @Inject(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setDefaultPickUpDelay()V", shift = At.Shift.AFTER))
    private static void dragonSurvival$markFireImmuneDragonDrop(final Level level, final Supplier<ItemEntity> itemSupplier, final ItemStack stack,
                                                                final CallbackInfo callback, final @Local ItemEntity itemEntity) {
        DragonBonusHandler.addFireProtectionToDragonDrop(dragonSurvival$dropBreaker.get(), itemEntity);
    }
}
