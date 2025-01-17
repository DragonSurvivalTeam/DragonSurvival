package by.dragonsurvivalteam.dragonsurvival.common.structures;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PlacedEndPlatforms;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonSpeciesTags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class EndPlatformHandler {
    // TODO :: make this stuff configurable (data loader -> species : position+platform)
    private static final BlockPos CAVE_SPAWN = new BlockPos(-200, 50, 0);
    private static final BlockPos SEA_SPAWN = new BlockPos(0, 50, 200);
    private static final BlockPos FOREST_SPAWN = new BlockPos(0, 50, -200);

    private static final ResourceLocation CAVE_PLATFORM = DragonSurvival.res("end_spawn_platforms/cave_end_spawn_platform");
    private static final ResourceLocation SEA_PLATFORM = DragonSurvival.res("end_spawn_platforms/sea_end_spawn_platform");
    private static final ResourceLocation FOREST_PLATFORM = DragonSurvival.res("end_spawn_platforms/forest_end_spawn_platform");

    // Can call '.setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING)' to disable waterlogging
    private static final StructurePlaceSettings SETTINGS = new StructurePlaceSettings()
            .setRotation(Rotation.NONE)
            .setMirror(Mirror.NONE)
            .setIgnoreEntities(false)
            .setKnownShape(true);

    public static @Nullable BlockPos getSpawnPoint(final Player player) {
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return null;
        }

        if (handler.species().is(DSDragonSpeciesTags.CAVE)) {
            return CAVE_SPAWN;
        } else if (handler.species().is(DSDragonSpeciesTags.SEA)) {
            return SEA_SPAWN;
        } else if (handler.species().is(DSDragonSpeciesTags.FOREST)) {
            return FOREST_SPAWN;
        }

        return null;
    }

    public static boolean placePlatform(final Player player, final ServerLevel level, final BlockPos position) {
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return false;
        }

        ResourceLocation platform;

        if (handler.species().is(DSDragonSpeciesTags.CAVE)) {
            platform = CAVE_PLATFORM;
        } else if (handler.species().is(DSDragonSpeciesTags.SEA)) {
            platform = SEA_PLATFORM;
        } else if (handler.species().is(DSDragonSpeciesTags.FOREST)) {
            platform = FOREST_PLATFORM;
        } else {
            return false;
        }

        PlacedEndPlatforms platforms = level.getData(DSDataAttachments.PLACED_END_PLATFORMS);

        if (platforms.wasPlaced(platform)) {
            createNormalPlatform(level, position.below());
            return true;
        } else {
            platforms.addPlatform(platform);
        }

        StructureTemplate template = level.getStructureManager().getOrCreate(platform);
        // Offset so that the obsidian-platform of the structure lines up with the intended position
        BlockPos spawnPosition = position.offset(-template.getSize().getX() / 2, -template.getSize().getY() / 2, -template.getSize().getZ() / 2);

        // Realistically there should be nothing to clear out anyway
        BlockPos.betweenClosedStream(template.getBoundingBox(SETTINGS, spawnPosition)).forEach(toClear -> level.setBlock(toClear, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
        return template.placeInWorld(level, spawnPosition, spawnPosition, SETTINGS, level.getRandom(), /* Same as 'SinglePoolElement', avoids updating neighbour shapes */ 18);
    }

    /** Places a 5 x 5 x 1 obsidian platform and clears out the 5 x 5 x 2 area above it (replacing it with air) */
    private static void createNormalPlatform(final ServerLevel level, final BlockPos position) {
        BlockPos.MutableBlockPos mutablePosition = position.mutable();

        // Platform is 5 x 5 (position acts as center)
        for (int z = -2; z <= 2; z++) {
            for (int x = -2; x <= 2; x++) {
                for (int y = -1; y < 3; y++) {
                    BlockPos currentPosition = mutablePosition.set(position).move(x, y, z);
                    BlockState state = level.getBlockState(currentPosition);

                    if (state.is(DSBlockTags.END_PLATFORM_NON_REPLACEABLE)) {
                        // Mostly just here because the sea dragon platform has snow above the obsidian part
                        // Custom platforms may also have such types of decorative blocks
                        continue;
                    }

                    // Only place obsidian at the bottom (the other height is just cleared out)
                    Block block = y == -1 ? Blocks.OBSIDIAN : Blocks.AIR;

                    if (block == Blocks.AIR || !state.blocksMotion()) {
                        level.setBlock(currentPosition, block.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }
}