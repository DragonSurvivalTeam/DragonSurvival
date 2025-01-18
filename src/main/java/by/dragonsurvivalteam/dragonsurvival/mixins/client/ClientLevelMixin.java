package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.DragonDestructionHandler;
import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicPlaceholder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {
    protected ClientLevelMixin(WritableLevelData pLevelData, ResourceKey<Level> pDimension, RegistryAccess pRegistryAccess, Holder<DimensionType> pDimensionTypeRegistration, Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed, int pMaxChainedNeighborUpdates) {
        super(pLevelData, pDimension, pRegistryAccess, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed, pMaxChainedNeighborUpdates);
    }

    // TODO :: there is probably an event which could be used instead
    /** Keep track of which block is currently being broken to render the overlay for additional blocks if needed */
    @SuppressWarnings("DataFlowIssue") // level should not be null
    @Inject(method = "destroyBlockProgress", at = @At("RETURN"))
    private void dragonSurvival$storeCenterOfDestruction(int breakerId, BlockPos position, int progress, CallbackInfo callback) {
        if (Minecraft.getInstance().level.getEntity(breakerId) == Minecraft.getInstance().player) {
            DragonDestructionHandler.centerOfDestruction = position;
        }
    }

    /** Apply the destruction progress to the root block */
    @ModifyArg(method = "destroyBlockProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"), index = 1)
    private BlockPos dragonSurvival$handleSourceOfMagicDestroyProgress(final BlockPos position) {
        BlockState state = getBlockState(position);

        if (state.getBlock() instanceof SourceOfMagicBlock) {
            if (!state.getValue(SourceOfMagicBlock.PRIMARY_BLOCK)) {
                BlockEntity blockEntity = getBlockEntity(position);

                if (blockEntity instanceof SourceOfMagicPlaceholder placeholder) {
                    return placeholder.rootPos;
                }
            }
        }

        return position;
    }

    /** Update the ore vision handler while a search is pending or not scheduled */
    @Inject(method = "sendBlockUpdated", at = @At("HEAD"))
    private void dragonSurvival$updateBlockVision(final BlockPos position, final BlockState oldState, final BlockState newState, final int flags, final CallbackInfo callback) {
        BlockVisionHandler.updateEntry(position, oldState, newState);
    }
}