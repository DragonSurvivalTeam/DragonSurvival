package by.dragonsurvivalteam.dragonsurvival.common.structures;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.EndPlatform;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PlacedEndPlatforms;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
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

        EndPlatform data = handler.species().getData(DSDataMaps.END_PLATFORMS);

        if (data != null) {
            return data.spawnPosition();
        }

        return null;
    }

    public static boolean placePlatform(final Player player, final ServerLevel level, final BlockPos position) {
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return false;
        }

        EndPlatform data = handler.species().getData(DSDataMaps.END_PLATFORMS);

        if (data == null) {
            return false;
        }

        ResourceLocation platform = data.structure();
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

                    // Only place obsidian at the bottom (the other height is just cleared out)
                    Block block = y == -1 ? Blocks.OBSIDIAN : Blocks.AIR;

                    // If we're checking for placing obsidian and the block does not block motion (i.e. player can fall through it)
                    // Then we replace it anyway to make sure that the player does not fall in the void
                    if (state.is(DSBlockTags.END_PLATFORM_NON_REPLACEABLE) && (block == Blocks.AIR || state.blocksMotion())) {
                        // Mostly just here because the sea dragon platform has snow above the obsidian part
                        // Custom platforms may also have such types of decorative blocks
                        continue;
                    }

                    if (block == Blocks.AIR || !state.blocksMotion()) {
                        level.setBlock(currentPosition, block.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }
}