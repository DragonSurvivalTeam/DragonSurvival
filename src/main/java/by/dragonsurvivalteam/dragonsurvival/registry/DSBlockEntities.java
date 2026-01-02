package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonBeaconBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
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
            "dragon_nest", () -> new BlockEntityType<>(
                            SourceOfMagicBlockEntity::new,
                            DSBlocks.CAVE_SOURCE_OF_MAGIC.get(),
                            DSBlocks.SEA_SOURCE_OF_MAGIC.get(),
                            DSBlocks.FOREST_SOURCE_OF_MAGIC.get())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SourceOfMagicPlaceholder>> SOURCE_OF_MAGIC_PLACEHOLDER = REGISTRY.register(
            "placeholder", () -> new BlockEntityType<>(
                            SourceOfMagicPlaceholder::new,
                            DSBlocks.FOREST_SOURCE_OF_MAGIC.get(),
                            DSBlocks.SEA_SOURCE_OF_MAGIC.get(),
                            DSBlocks.CAVE_SOURCE_OF_MAGIC.get())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HelmetBlockEntity>> HELMET = REGISTRY.register(
            "knight_helmet", () -> new BlockEntityType<>(
                            HelmetBlockEntity::new,
                            DSBlocks.GRAY_KNIGHT_HELMET.get(),
                            DSBlocks.GOLDEN_KNIGHT_HELMET.get(),
                            DSBlocks.BLACK_KNIGHT_HELMET.get())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DragonBeaconBlockEntity>> DRAGON_BEACON = REGISTRY.register(
            "dragon_beacon", () -> new BlockEntityType<>(
                            DragonBeaconBlockEntity::new,
                            DSBlocks.DRAGON_BEACON.get())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DragonSoulBlockEntity>> DRAGON_SOUL = REGISTRY.register(
            "dragon_soul", () -> new BlockEntityType<>(
                            DragonSoulBlockEntity::new,
                            DSBlocks.DRAGON_SOUL.get())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrimordialAnchorBlockEntity>> PRIMORDIAL_ANCHOR = REGISTRY.register(
            "primordial_anchor", () -> new BlockEntityType<>(
                            PrimordialAnchorBlockEntity::new,
                            DSBlocks.PRIMORDIAL_ANCHOR.get())
    );
}