package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonBeacon;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonDoor;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonSoulBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonVaultBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.ModCompat;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.PrimordialAnchorBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SkeletonPieceBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockLootTableSubProvider extends BlockLootSubProvider {
    public BlockLootTableSubProvider(HolderLookup.Provider provider) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        DSBlocks.REGISTRY.getEntries().forEach((key) -> {
            if (key.get() instanceof ModCompat compat && compat.getCompatId() != null) {
                // Added as separate datapack to avoid errors
                // Since there is no current support to conditionally load loot tables at the moment
                return;
            }

            Function<Block, LootTable.Builder> builder = block -> {
                if (block instanceof DragonDoor) {
                    return createSinglePropConditionTable(block, DragonDoor.PART, DragonDoor.Part.BOTTOM);
                } else if (block instanceof SourceOfMagicBlock) {
                    return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SourceOfMagicBlock.PRIMARY_BLOCK, true))))));
                } else if (block instanceof TreasureBlock) {
                    return createTreasureBlockLoot(block);
                } else if (block instanceof SkeletonPieceBlock skeleton) {
                    return switch (skeleton.type()) {
                        case CHEST -> createSingleItemTable(DSItems.STAR_BONE.get(), UniformGenerator.between(3, 6));
                        case LEG_1, NECK_2 -> createSingleItemTable(DSItems.STAR_BONE.get(), UniformGenerator.between(1, 3));
                        case LEG_2, NECK_1 -> createSingleItemTable(DSItems.STAR_BONE.get(), UniformGenerator.between(1, 2));
                        case LEG_3 -> createSingleItemTable(DSItems.STAR_BONE.get(), ConstantValue.exactly(1));
                        case NECK_3 -> createSingleItemTable(DSItems.STAR_BONE.get(), UniformGenerator.between(1, 4));
                        case PELVIS -> createSingleItemTable(DSItems.STAR_BONE.get(), UniformGenerator.between(2, 4));
                        case SKULL_1, SKULL_2 -> createSingleItemTable(DSItems.STAR_BONE.get(), UniformGenerator.between(1, 5));
                        default -> createSingleItemTable(DSItems.STAR_BONE.get());
                    };
                } else if (block instanceof DragonBeacon) {
                    // We want all dragon beacons to drop empty dragon beacons instead
                    return createSingleItemTable(DSBlocks.DRAGON_BEACON.get());
                } else if (block instanceof DragonVaultBlock || block instanceof PrimordialAnchorBlock) {
                    // Vaults and Primordial Anchors should not drop anything
                    return LootTable.lootTable();
                } else if (block instanceof DragonSoulBlock) {
                    return LootTable.lootTable().withPool(
                            applyExplosionCondition(block, LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    // Make sure it copies the soul data back into the dropped item
                                    .add(LootItem.lootTableItem(block).apply(
                                            CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("components", "components")
                                    ))
                            ));
                }

                return createSingleItemTable(key.get().asItem());
            };

            add(key.get(), builder);
        });
    }

    public static LootTable.Builder createTreasureBlockLoot(final Block block) {
        ArrayList<LootPoolSingletonContainer.Builder<?>> list = new ArrayList<>();

        for (Integer possibleValue : TreasureBlock.LAYERS.getPossibleValues()) {
            LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(block)
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TreasureBlock.LAYERS, possibleValue)))
                    .apply(SetItemCountFunction.setCount(ConstantValue.exactly(possibleValue)));

            list.add(entry);
        }

        LootPoolSingletonContainer.Builder<?>[] arr = list.toArray(new LootPoolSingletonContainer.Builder[0]);

        return LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))
                        .add(AlternativesEntry.alternatives(AlternativesEntry.alternatives(arr))
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block))));
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return DSBlocks.REGISTRY.getEntries().stream().map(RegistryObject::get)
                .filter(block -> !(block instanceof ModCompat compat) || compat.getCompatId() == null)
                .collect(Collectors.toList());
    }
}
