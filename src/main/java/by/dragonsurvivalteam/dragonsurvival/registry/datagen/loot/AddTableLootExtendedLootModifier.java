package by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    private final List<TablePattern> resolvedPatterns = new ArrayList<>();
    private boolean hasResolvedTables = false;

    public AddTableLootExtendedLootModifier(LootItemCondition[] conditionsIn, ResourceKey<LootTable> table, List<String> lootTables, boolean blacklist) {
        super(conditionsIn, 0);
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

    private void resolveTables() {
        for (String table : this.tablesToApply) {
            Identifier parsedTable = Identifier.tryParse(table);

            if (parsedTable != null) {
                ResourceKey<LootTable> resolvedKey = ResourceKey.create(Registries.LOOT_TABLE, parsedTable);

                if (!resolvedKey.equals(this.table)) {
                    resolvedTables.add(resolvedKey);
                }

                continue;
            }

            String[] splitLocation = table.split(":", 2);

            if (splitLocation.length < 2) {
                continue;
            }

            Pattern pattern;

            try {
                pattern = Pattern.compile(splitLocation[1]);
            } catch (PatternSyntaxException ignored) {
                continue;
            }

            resolvedPatterns.add(new TablePattern(splitLocation[0], pattern));
        }

        hasResolvedTables = true;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        if (!hasResolvedTables) {
            resolveTables();
        }

        ResourceKey<LootTable> queriedKey = ResourceKey.create(Registries.LOOT_TABLE, context.getQueriedLootTableId());
        Identifier queriedId = queriedKey.identifier();
        boolean shouldApply = resolvedTables.contains(queriedKey) || resolvedPatterns.stream().anyMatch(pattern -> pattern.matches(queriedId));

        if (shouldApply == blacklist) {
            return generatedLoot;
        }

        context.getResolver().lookupOrThrow(Registries.LOOT_TABLE).get(this.table).ifPresent(extraTable -> {
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

    private record TablePattern(String namespace, Pattern pattern) {
        private boolean matches(final Identifier identifier) {
            return identifier.getNamespace().equals(namespace) && pattern.matcher(identifier.getPath()).matches();
        }
    }
}
