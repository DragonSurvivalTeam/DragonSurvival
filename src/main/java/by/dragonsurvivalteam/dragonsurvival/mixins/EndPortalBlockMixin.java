package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonSpeciesTags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collections;
import java.util.List;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Unique private static final BlockPos END_CAVE_DRAGON_SPAWN_POINT = new BlockPos(-100, 50, 0);
    @Unique private static final BlockPos END_SEA_DRAGON_SPAWN_POINT = new BlockPos(0, 50, 100);
    @Unique private static final BlockPos END_FOREST_DRAGON_SPAWN_POINT = new BlockPos(0, 50, -100);

    @ModifyVariable(method = "getPortalDestination", at = @At(value = "STORE"), ordinal = 1)
    private BlockPos modifyBlockPosForEndSpawnPoint(BlockPos original, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true) ServerLevel level) {
        boolean travellingToTheEnd = level.dimension().equals(ServerLevel.OVERWORLD);
        if(entity instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            if(!handler.isDragon()) {
                return original;
            }

            if(travellingToTheEnd) {
                if(handler.species().is(DSDragonSpeciesTags.CAVE)) {
                    return END_CAVE_DRAGON_SPAWN_POINT;
                } else if(handler.species().is(DSDragonSpeciesTags.SEA)) {
                    return END_SEA_DRAGON_SPAWN_POINT;
                } else if(handler.species().is(DSDragonSpeciesTags.FOREST)) {
                    return END_FOREST_DRAGON_SPAWN_POINT;
                }
            }
        }

        return original;
    }

    @Unique private static ResourceLocation dragonSurvival$getDragonSpawnPlatformStructure(Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            if (!handler.isDragon()) {
                throw new IllegalArgumentException("Entity is not a dragon");
            }

            if (handler.species().is(DSDragonSpeciesTags.CAVE)) {
                return ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "end_spawn_platforms/cave_end_spawn_platform");
            } else if (handler.species().is(DSDragonSpeciesTags.SEA)) {
                return ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "end_spawn_platforms/sea_end_spawn_platform");
            } else if (handler.species().is(DSDragonSpeciesTags.FOREST)) {
                return ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "end_spawn_platforms/forest_end_spawn_platform");
            } else {
                throw new IllegalArgumentException("Entity is an invalid dragon type");
            }
    }

    @Unique private static StructureBlockEntity dragonSurvival$createStructureBlock(ResourceLocation structure, BlockPos pos, Rotation rotation, ServerLevel level) {
        level.setBlockAndUpdate(pos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity structureblockentity = (StructureBlockEntity)level.getBlockEntity(pos);
        structureblockentity.setMode(StructureMode.LOAD);
        structureblockentity.setRotation(rotation);
        structureblockentity.setIgnoreEntities(false);
        structureblockentity.setStructureName(structure);
        if (!structureblockentity.loadStructureInfo(level)) {
            throw new RuntimeException("Failed to load structure info for end platform.");
        }

        return structureblockentity;
    }

    @Unique private static void dragonSurvival$clearBlock(BlockPos pos, ServerLevel serverLevel) {
        BlockState blockstate = Blocks.AIR.defaultBlockState();
        BlockInput blockinput = new BlockInput(blockstate, Collections.emptySet(), null);
        blockinput.place(serverLevel, pos, 2);
        serverLevel.blockUpdated(pos, blockstate.getBlock());
    }

    @Unique private static void dragonSurvival$clearSpaceForStructure(BoundingBox boundingBox, ServerLevel level) {
        BlockPos.betweenClosedStream(boundingBox).forEach(blockPos -> dragonSurvival$clearBlock(blockPos, level));
        level.getBlockTicks().clearArea(boundingBox);
        level.clearBlockEvents(boundingBox);
        AABB aabb = AABB.of(boundingBox);
        List<Entity> list = level.getEntitiesOfClass(Entity.class, aabb, entity -> !(entity instanceof Player));
        list.forEach(Entity::discard);
    }

    @WrapOperation(method = "getPortalDestination", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/EndPlatformFeature;createEndPlatform(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Z)V"))
    private void spawnDragonPlatform(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, boolean dropBlocks, Operation<Void> original, @Local(argsOnly = true) Entity entity) {
        if(entity instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            if(handler.isDragon() && handler.species().is(DSDragonSpeciesTags.CAVE) || handler.species().is(DSDragonSpeciesTags.SEA) || handler.species().is(DSDragonSpeciesTags.FOREST)) {
                // Construct a different platform for the dragon to spawn on by getting structure data
                ServerLevel level = serverLevelAccessor.getLevel();
                Vec3i structureSize = level.getStructureManager().get(dragonSurvival$getDragonSpawnPlatformStructure(player)).get().getSize();
                // Offset the blockPos to the bottom left corner of the structure
                blockPos = blockPos.offset(-structureSize.getX() / 2, -structureSize.getY() / 2, -structureSize.getZ() / 2);
                BoundingBox boundingbox = StructureUtils.getStructureBoundingBox(blockPos, structureSize, Rotation.NONE);
                dragonSurvival$clearSpaceForStructure(boundingbox, level);
                StructureBlockEntity structureblockentity = dragonSurvival$createStructureBlock(dragonSurvival$getDragonSpawnPlatformStructure(player), blockPos, Rotation.NONE, level);
                structureblockentity.placeStructure(level);
                level.getBlockTicks().clearArea(boundingbox);
                level.clearBlockEvents(boundingbox);
                // Remove the structure block that was placed
                level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                return;
            }
        }

        original.call(serverLevelAccessor, blockPos, dropBlocks);
    }
}
