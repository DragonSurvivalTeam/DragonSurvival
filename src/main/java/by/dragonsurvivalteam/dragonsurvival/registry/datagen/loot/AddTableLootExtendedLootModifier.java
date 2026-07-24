package by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

/**
 * A loot modifier that adds loot from a table to the current loot table if the current loot table is in a list of tables to apply to.
 * <p>
 * Supports regex for table names, and can blacklist or whitelist tables.
 * <p>
 * This is used currently to add the dragon loot to various loot tables automatically.
 */
public class AddTableLootExtendedLootModifier extends LootModifier {

    public static final MapCodec<AddTableLootExtendedLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(glm -> glm.conditions),
                    ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(AddTableLootExtendedLootModifier::table),
                    Codec.STRING.listOf().fieldOf("tables_to_apply").forGetter(AddTableLootExtendedLootModifier::tablesToApply),
                    Codec.BOOL.optionalFieldOf("blacklist", false).forGetter(AddTableLootExtendedLootModifier::blacklist))
            .apply(instance, AddTableLootExtendedLootModifier::new));

    private final ResourceKey<LootTable> table;
    private final List<String> tablesToApply;
    private final boolean blacklist;
    private final HashSet<ResourceKey<LootTable>> resolvedTables = new HashSet<>();
    private boolean hasResolvedTables = false;

    public AddTableLootExtendedLootModifier(LootItemCondition[] conditionsIn, ResourceKey<LootTable> table, List<String> lootTables, boolean blacklist) {
        super(conditionsIn);
        this.table = table;
        this.tablesToApply = lootTables;
        this.blacklist = blacklist;
    }

    public ResourceKey<LootTable> table() {
        return this.table;
    }

    public List<String> tablesToApply() {
        return this.tablesToApply;
    }

    public boolean blacklist() {
        return this.blacklist;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        // Generate the resolved tables list if we haven't already
        if (!hasResolvedTables) {
            for (String table : this.tablesToApply) {
                ResourceLocation parsedTable = ResourceLocation.tryParse(table);
                if (parsedTable != null) {
                    resolvedTables.add(ResourceKey.create(Registries.LOOT_TABLE, parsedTable));
                } else {
                    // Try regex if we don't have a valid key
                    context.getLevel().getServer().reloadableRegistries().get().registryOrThrow(Registries.LOOT_TABLE).registryKeySet().forEach(
                            key -> {
                                String path = key.location().toString();
                                if (path.matches(table) && !path.equals(this.table.location().toString())) {
                                    resolvedTables.add(key);
                                }
                            }
                    );
                }
            }
            hasResolvedTables = true;
        }

        ResourceKey<LootTable> queriedKey = ResourceKey.create(Registries.LOOT_TABLE, context.getQueriedLootTableId());
        boolean shouldApply = resolvedTables.contains(queriedKey);

        if (shouldApply == blacklist) {
            return generatedLoot;
        }

        context.getResolver().get(Registries.LOOT_TABLE, this.table).ifPresent(extraTable -> {
            // Don't run loot modifiers for subtables;
            // the added loot will be modifiable by downstream loot modifiers modifying the target table,
            // so if we modify it here then it could get modified twice.
            extraTable.value().getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        });
        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
