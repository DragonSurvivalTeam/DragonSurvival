package by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A loot modifier that adds loot from a table to the current loot table if the current loot table is in a list of tables to apply to.
 * <p>
 * Supports regex for table names, and can blacklist or whitelist tables.
 * <p>
 * This is used currently to add the dragon loot to various loot tables automatically.
 */
public class AddTableLootExtendedLootModifier extends LootModifier {

    public static final Codec<AddTableLootExtendedLootModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance).and(
            instance.group(
                    ResourceLocation.CODEC.fieldOf("table").forGetter(AddTableLootExtendedLootModifier::table),
                    Codec.STRING.listOf().fieldOf("tables_to_apply").forGetter(AddTableLootExtendedLootModifier::tablesToApply),
                    Codec.BOOL.optionalFieldOf("blacklist", false).forGetter(AddTableLootExtendedLootModifier::blacklist)
            ))
            .apply(instance, AddTableLootExtendedLootModifier::new));

    private final ResourceLocation table;
    private final List<String> tablesToApply;
    private final boolean blacklist;

    public AddTableLootExtendedLootModifier(LootItemCondition[] conditionsIn, ResourceLocation table, List<String> lootTables, boolean blacklist) {
        super(conditionsIn);
        this.table = table;
        this.tablesToApply = lootTables;
        this.blacklist = blacklist;
    }

    public ResourceLocation table() {
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
        ResourceLocation queriedId = context.getQueriedLootTableId();
        boolean shouldApply = !queriedId.equals(table) && tablesToApply.stream().anyMatch(candidate -> matches(candidate, queriedId));

        if (shouldApply == blacklist) {
            return generatedLoot;
        }

        LootTable extraTable = context.getResolver().getLootTable(table);
        // Don't run loot modifiers for subtables; downstream modifiers will process the target table's output.
        extraTable.getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        return generatedLoot;
    }

    private static boolean matches(final String table, final ResourceLocation queriedId) {
        ResourceLocation exact = ResourceLocation.tryParse(table);
        return exact != null ? exact.equals(queriedId) : queriedId.toString().matches(table);
    }

    @Override
    public @NotNull Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
