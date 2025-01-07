package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.server.tileentity.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DSBlockEntities {
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HelmetBlockEntity>> HELMET = DS_TILE_ENTITIES.register(
            "knight_helmet", () -> BlockEntityType.Builder.of(
                            HelmetBlockEntity::new,
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrimordialAnchorBlockEntity>> PRIMORDIAL_ANCHOR = DS_TILE_ENTITIES.register(
            "primordial_anchor", () -> BlockEntityType.Builder.of(
                            PrimordialAnchorBlockEntity::new,
                            DSBlocks.PRIMORDIAL_ANCHOR.get())
                    .build(null)
    );
}