package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonBeaconBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.HelmetTileEntity;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicPlaceholder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DSTileEntities {
    public static DeferredRegister<BlockEntityType<?>> DS_TILE_ENTITIES = DeferredRegister.create(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            MODID
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SourceOfMagicBlockEntity>> SOURCE_OF_MAGIC_TILE_ENTITY = DS_TILE_ENTITIES.register(
            "dragon_nest", () -> BlockEntityType.Builder.of(
                            SourceOfMagicBlockEntity::new,
                            DSBlocks.CAVE_SOURCE_OF_MAGIC.get(),
                            DSBlocks.SEA_SOURCE_OF_MAGIC.get(),
                            DSBlocks.FOREST_SOURCE_OF_MAGIC.get())
                    .build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SourceOfMagicPlaceholder>> SOURCE_OF_MAGIC_PLACEHOLDER = DS_TILE_ENTITIES.register(
            "placeholder", () -> BlockEntityType.Builder.of(
                            SourceOfMagicPlaceholder::new,
                            DSBlocks.FOREST_SOURCE_OF_MAGIC.get(),
                            DSBlocks.SEA_SOURCE_OF_MAGIC.get(),
                            DSBlocks.CAVE_SOURCE_OF_MAGIC.get())
                    .build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HelmetTileEntity>> HELMET_TILE = DS_TILE_ENTITIES.register(
            "knight_helmet", () -> BlockEntityType.Builder.of(
                            HelmetTileEntity::new,
                            DSBlocks.GRAY_KNIGHT_HELMET.get(),
                            DSBlocks.GOLDEN_KNIGHT_HELMET.get(),
                            DSBlocks.BLACK_KNIGHT_HELMET.get())
                    .build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DragonBeaconBlockEntity>> DRAGON_BEACON = DS_TILE_ENTITIES.register(
            "dragon_beacon", () -> BlockEntityType.Builder.of(
                            DragonBeaconBlockEntity::new,
                            DSBlocks.EMPTY_DRAGON_BEACON.get(),
                            DSBlocks.FOREST_DRAGON_BEACON.get(),
                            DSBlocks.SEA_DRAGON_BEACON.get(),
                            DSBlocks.CAVE_DRAGON_BEACON.get())
                    .build(null)
    );
}