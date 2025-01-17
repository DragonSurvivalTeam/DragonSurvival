package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonBeaconBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.HelmetBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.PrimordialAnchorBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicPlaceholder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@SuppressWarnings("DataFlowIssue") // null parameter in 'build' does not cause issues
public class DSBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SourceOfMagicBlockEntity>> SOURCE_OF_MAGIC_TILE_ENTITY = REGISTRY.register(
            "dragon_nest", () -> BlockEntityType.Builder.of(
                            SourceOfMagicBlockEntity::new,
                            DSBlocks.CAVE_SOURCE_OF_MAGIC.get(),
                            DSBlocks.SEA_SOURCE_OF_MAGIC.get(),
                            DSBlocks.FOREST_SOURCE_OF_MAGIC.get())
                    .build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SourceOfMagicPlaceholder>> SOURCE_OF_MAGIC_PLACEHOLDER = REGISTRY.register(
            "placeholder", () -> BlockEntityType.Builder.of(
                            SourceOfMagicPlaceholder::new,
                            DSBlocks.FOREST_SOURCE_OF_MAGIC.get(),
                            DSBlocks.SEA_SOURCE_OF_MAGIC.get(),
                            DSBlocks.CAVE_SOURCE_OF_MAGIC.get())
                    .build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HelmetBlockEntity>> HELMET = REGISTRY.register(
            "knight_helmet", () -> BlockEntityType.Builder.of(
                            HelmetBlockEntity::new,
                            DSBlocks.GRAY_KNIGHT_HELMET.get(),
                            DSBlocks.GOLDEN_KNIGHT_HELMET.get(),
                            DSBlocks.BLACK_KNIGHT_HELMET.get())
                    .build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DragonBeaconBlockEntity>> DRAGON_BEACON = REGISTRY.register(
            "dragon_beacon", () -> BlockEntityType.Builder.of(
                            DragonBeaconBlockEntity::new,
                            DSBlocks.EMPTY_DRAGON_BEACON.get(),
                            DSBlocks.FOREST_DRAGON_BEACON.get(),
                            DSBlocks.SEA_DRAGON_BEACON.get(),
                            DSBlocks.CAVE_DRAGON_BEACON.get())
                    .build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrimordialAnchorBlockEntity>> PRIMORDIAL_ANCHOR = REGISTRY.register(
            "primordial_anchor", () -> BlockEntityType.Builder.of(
                            PrimordialAnchorBlockEntity::new,
                            DSBlocks.PRIMORDIAL_ANCHOR.get())
                    .build(null)
    );
}