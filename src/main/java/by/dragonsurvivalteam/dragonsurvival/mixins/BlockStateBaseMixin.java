package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.BlockStateBaseExtension;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin extends StateHolder<Block, BlockState> implements BlockStateBaseExtension {
    // Code adapted from https://github.com/apace100/apoli/blob/1.20/src/main/java/io/github/apace100/apoli/mixin/AbstractBlockStateMixin.java

    @Shadow public abstract Block getBlock();

    @Shadow
    protected abstract BlockState asState();

    protected BlockStateBaseMixin(Block owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> values, MapCodec<BlockState> propertiesCodec) {
        super(owner, values, propertiesCodec);
    }
    @Inject(
            method = {"getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"},
            at = {@At("RETURN")},
            cancellable = true
    )
    public void dragonSurvival$phaseThroughBlocks(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir)  {
        VoxelShape original = cir.getReturnValue();
        Entity entity;
        if (original.isEmpty() || !(context instanceof EntityCollisionContext esc) || (entity = esc.getEntity()) == null) {
            return;
        }

        Level level = entity.level();
        Vec3 upVector = entity.getUpVector(1.0F);
        Vec3 entityPos = entity.getPosition(1.0F);

        boolean result = entity.getExistingData(DSDataAttachments.PHASING).map(phasing -> phasing.testValidBlocks(level, pos, upVector, entityPos)).orElse(false);

       cir.setReturnValue(result ? Shapes.empty() : original);
    }

    /* Potentially is pushEntitiesUp in this version?  Unsure if even necessary anymore
    @WrapWithCondition(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
    private boolean apoli$preventOnEntityCollisionCallWhenPhasing(Block instance, BlockState state, World world, BlockPos blockPos, Entity entity) {
        return !PowerHolderComponent.hasPower(entity, PhasingPower.class, p -> p.doesApply(blockPos));
    }*/

    @Override
    public VoxelShape dragonSurvival$getOriginalCollisionShape(BlockGetter world, BlockPos pos, CollisionContext context) {
        return ((BlockBehaviourAccessor) this.getBlock()).dragonSurvival$getCollisionShape(this.asState(), world, pos, context);
    }
}
