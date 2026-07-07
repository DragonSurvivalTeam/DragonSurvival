package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.item.trading.VillagerTrades;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.storage.loot.functions.DiscardItem;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.FilteredFunction;
import net.minecraft.world.level.storage.loot.functions.SetNameFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DSTrades {
    public static final TagKey<Structure> ON_DRAGON_HUNTERS_CASTLE_MAPS = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("on_dragon_hunter_maps"));
    private static final ResourceKey<PoiType> DRAGON_RIDER_POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, DragonSurvival.res("dragon_rider_poi"));

    public static final DeferredRegister<PoiType> POI_REGISTRY = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, DragonSurvival.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSION_REGISTRY = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, DragonSurvival.MODID);

    public static final Holder<PoiType> DRAGON_RIDER_POI = POI_REGISTRY.register(
        "dragon_rider_poi",
        () -> new PoiType(ImmutableSet.copyOf(DSBlocks.DRAGON_RIDER_WORKBENCH.value().getStateDefinition().getPossibleStates()), 1, 1)
    );

    private static final ResourceKey<TradeSet> DRAGON_RIDER_LEVEL_1 = tradeSetKey("dragon_rider/level_1");
    private static final ResourceKey<TradeSet> DRAGON_RIDER_LEVEL_2 = tradeSetKey("dragon_rider/level_2");
    private static final ResourceKey<TradeSet> DRAGON_RIDER_LEVEL_3 = tradeSetKey("dragon_rider/level_3");
    private static final ResourceKey<TradeSet> DRAGON_RIDER_LEVEL_4 = tradeSetKey("dragon_rider/level_4");
    private static final ResourceKey<TradeSet> DRAGON_RIDER_LEVEL_5 = tradeSetKey("dragon_rider/level_5");

    private static final ResourceKey<TradeSet> LEADER_LEVEL_1 = tradeSetKey("leader/level_1");
    private static final ResourceKey<TradeSet> LEADER_LEVEL_2 = tradeSetKey("leader/level_2");
    private static final ResourceKey<TradeSet> LEADER_LEVEL_3 = tradeSetKey("leader/level_3");
    private static final ResourceKey<TradeSet> LEADER_LEVEL_4 = tradeSetKey("leader/level_4");
    private static final ResourceKey<TradeSet> LEADER_LEVEL_5 = tradeSetKey("leader/level_5");

    private static final ResourceKey<VillagerTrade> DRAGON_RIDER_UNBREAKABLE_SPIRIT = villagerTradeKey("dragon_rider/unbreakable_spirit");
    private static final ResourceKey<VillagerTrade> DRAGON_RIDER_COMBAT_RECOVERY = villagerTradeKey("dragon_rider/combat_recovery");
    private static final ResourceKey<VillagerTrade> DRAGON_RIDER_AERODYNAMIC_MASTERY = villagerTradeKey("dragon_rider/aerodynamic_mastery");
    private static final ResourceKey<VillagerTrade> DRAGON_RIDER_SACRED_SCALES = villagerTradeKey("dragon_rider/sacred_scales");
    private static final ResourceKey<VillagerTrade> DRAGON_RIDER_LIGHT_KEY = villagerTradeKey("dragon_rider/light_key");

    private static final ResourceKey<VillagerTrade> LEADER_DRAGON_HEART_SHARD = villagerTradeKey("leader/dragon_heart_shard");
    private static final ResourceKey<VillagerTrade> LEADER_PARTISAN = villagerTradeKey("leader/partisan");
    private static final ResourceKey<VillagerTrade> LEADER_WEAK_DRAGON_HEART = villagerTradeKey("leader/weak_dragon_heart");
    private static final ResourceKey<VillagerTrade> LEADER_HUNTER_KEY = villagerTradeKey("leader/hunter_key");
    private static final ResourceKey<VillagerTrade> LEADER_ELDER_DRAGON_HEART = villagerTradeKey("leader/elder_dragon_heart");
    private static final ResourceKey<VillagerTrade> LEADER_DRAGONSBANE = villagerTradeKey("leader/dragonsbane");
    private static final ResourceKey<VillagerTrade> LEADER_BOLAS = villagerTradeKey("leader/bolas");

    public static final ResourceKey<VillagerTrade> CARTOGRAPHER_DRAGON_HUNTER_MAP = villagerTradeKey("cartographer/dragon_hunter_map");

    private static final Int2ObjectMap<ResourceKey<TradeSet>> DRAGON_RIDER_TRADE_SETS = Int2ObjectMap.ofEntries(
        Int2ObjectMap.entry(1, DRAGON_RIDER_LEVEL_1),
        Int2ObjectMap.entry(2, DRAGON_RIDER_LEVEL_2),
        Int2ObjectMap.entry(3, DRAGON_RIDER_LEVEL_3),
        Int2ObjectMap.entry(4, DRAGON_RIDER_LEVEL_4),
        Int2ObjectMap.entry(5, DRAGON_RIDER_LEVEL_5)
    );

    private static final Int2ObjectMap<ResourceKey<TradeSet>> LEADER_TRADE_SETS = Int2ObjectMap.ofEntries(
        Int2ObjectMap.entry(1, LEADER_LEVEL_1),
        Int2ObjectMap.entry(2, LEADER_LEVEL_2),
        Int2ObjectMap.entry(3, LEADER_LEVEL_3),
        Int2ObjectMap.entry(4, LEADER_LEVEL_4),
        Int2ObjectMap.entry(5, LEADER_LEVEL_5)
    );

    @Translation(type = Translation.Type.VILLAGER_PROFESSION, comments = {"Dragon Rider"})
    public static final Holder<VillagerProfession> DRAGON_RIDER_PROFESSION = PROFESSION_REGISTRY.register(
        "dragon_rider",
        () -> new VillagerProfession(
            Component.translatable(Translation.Type.VILLAGER_PROFESSION.wrap("dragon_rider")),
            poi -> poi.is(DRAGON_RIDER_POI_KEY),
            poi -> poi.is(DRAGON_RIDER_POI_KEY),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_WORK_ARMORER,
            DRAGON_RIDER_TRADE_SETS
        )
    );

    public static void registerVillagerTrades(final BootstrapContext<VillagerTrade> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        Optional<HolderSet<Enchantment>> doubleTradePrice = enchantments.get(EnchantmentTags.DOUBLE_TRADE_PRICE).map(named -> (HolderSet<Enchantment>) named);

        registerEnchantedBookTrade(context, items, enchantments, DRAGON_RIDER_UNBREAKABLE_SPIRIT, DSEnchantments.UNBREAKABLE_SPIRIT, 10, doubleTradePrice);
        registerEnchantedBookTrade(context, items, enchantments, DRAGON_RIDER_COMBAT_RECOVERY, DSEnchantments.COMBAT_RECOVERY, 20, doubleTradePrice);
        registerEnchantedBookTrade(context, items, enchantments, DRAGON_RIDER_AERODYNAMIC_MASTERY, DSEnchantments.AERODYNAMIC_MASTERY, 20, doubleTradePrice);
        registerEnchantedBookTrade(context, items, enchantments, DRAGON_RIDER_SACRED_SCALES, DSEnchantments.SACRED_SCALES, 20, doubleTradePrice);

        registerTrade(context, DRAGON_RIDER_LIGHT_KEY, Items.EMERALD, 32, DSItems.LIGHT_KEY, 1, 12, 35, 1.0F);

        registerTrade(context, LEADER_DRAGON_HEART_SHARD, DSItems.DRAGON_HEART_SHARD.value(), 1, Items.EMERALD, 1, 16, 5, 1.0F);
        registerTrade(context, LEADER_PARTISAN, Items.EMERALD, 12, DSItems.PARTISAN, 1, 1, 5, 1.0F);
        registerTrade(context, LEADER_WEAK_DRAGON_HEART, DSItems.WEAK_DRAGON_HEART.value(), 1, Items.EMERALD, 1, 16, 10, 1.0F);
        registerTrade(context, LEADER_HUNTER_KEY, Items.EMERALD, 32, DSItems.HUNTER_KEY, 1, 16, 35, 1.0F);
        registerTrade(context, LEADER_ELDER_DRAGON_HEART, DSItems.ELDER_DRAGON_HEART.value(), 1, Items.EMERALD, 12, 12, 25, 1.0F);
        registerEnchantedBookTrade(context, items, enchantments, LEADER_DRAGONSBANE, DSEnchantments.DRAGONSBANE, 15, doubleTradePrice);
        registerEnchantedBookTrade(context, items, enchantments, LEADER_BOLAS, DSEnchantments.BOLAS, 15, doubleTradePrice);

        registerExplorerMapTrade(context, items, CARTOGRAPHER_DRAGON_HUNTER_MAP, ON_DRAGON_HUNTERS_CASTLE_MAPS, DSMapDecorationTypes.DRAGON_HUNTER);
    }

    public static void registerTradeSets(final BootstrapContext<TradeSet> context) {
        registerTradeSet(context, DRAGON_RIDER_LEVEL_1, DRAGON_RIDER_UNBREAKABLE_SPIRIT);
        registerTradeSet(context, DRAGON_RIDER_LEVEL_2, DRAGON_RIDER_COMBAT_RECOVERY);
        registerTradeSet(context, DRAGON_RIDER_LEVEL_3, DRAGON_RIDER_AERODYNAMIC_MASTERY);
        registerTradeSet(context, DRAGON_RIDER_LEVEL_4, DRAGON_RIDER_SACRED_SCALES);
        registerTradeSet(context, DRAGON_RIDER_LEVEL_5, DRAGON_RIDER_LIGHT_KEY);

        registerTradeSet(context, LEADER_LEVEL_1, LEADER_DRAGON_HEART_SHARD, LEADER_PARTISAN);
        registerTradeSet(context, LEADER_LEVEL_2, LEADER_WEAK_DRAGON_HEART);
        registerTradeSet(context, LEADER_LEVEL_3, LEADER_HUNTER_KEY);
        registerTradeSet(context, LEADER_LEVEL_4, LEADER_ELDER_DRAGON_HEART);
        registerTradeSet(context, LEADER_LEVEL_5, LEADER_DRAGONSBANE, LEADER_BOLAS);
    }

    @Nullable public static ResourceKey<TradeSet> getLeaderTradeSet(final int level) {
        return LEADER_TRADE_SETS.get(level);
    }

    private static void registerEnchantedBookTrade(
        final BootstrapContext<VillagerTrade> context,
        final HolderGetter<Item> items,
        final HolderGetter<Enchantment> enchantments,
        final ResourceKey<VillagerTrade> key,
        final ResourceKey<Enchantment> enchantment,
        final int villagerXp,
        final Optional<HolderSet<Enchantment>> doubleTradePrice
    ) {
        context.register(
            key,
            new VillagerTrade(
                new TradeCost(Items.EMERALD, 0),
                Optional.of(new TradeCost(Items.BOOK, 1)),
                new ItemStackTemplate(Items.ENCHANTED_BOOK),
                12,
                villagerXp,
                0.2F,
                Optional.empty(),
                VillagerTrades.enchantedBook(items, Optional.of(HolderSet.direct(enchantments.getOrThrow(enchantment)))),
                doubleTradePrice
            )
        );
    }

    private static void registerTrade(
        final BootstrapContext<VillagerTrade> context,
        final ResourceKey<VillagerTrade> key,
        final Item costItem,
        final int costCount,
        final Holder<Item> resultItem,
        final int resultCount,
        final int maxUses,
        final int villagerXp,
        final float priceMultiplier
    ) {
        context.register(
            key,
            new VillagerTrade(
                new TradeCost(costItem, costCount),
                new ItemStackTemplate(resultItem, resultCount),
                maxUses,
                villagerXp,
                priceMultiplier,
                Optional.empty(),
                List.of()
            )
        );
    }

    private static void registerTrade(
        final BootstrapContext<VillagerTrade> context,
        final ResourceKey<VillagerTrade> key,
        final Item costItem,
        final int costCount,
        final Item resultItem,
        final int resultCount,
        final int maxUses,
        final int villagerXp,
        final float priceMultiplier
    ) {
        context.register(
            key,
            new VillagerTrade(
                new TradeCost(costItem, costCount),
                new ItemStackTemplate(resultItem, resultCount),
                maxUses,
                villagerXp,
                priceMultiplier,
                Optional.empty(),
                List.of()
            )
        );
    }

    private static void registerExplorerMapTrade(
        final BootstrapContext<VillagerTrade> context,
        final HolderGetter<Item> items,
        final ResourceKey<VillagerTrade> key,
        final TagKey<Structure> destination,
        final Holder<MapDecorationType> decoration
    ) {
        context.register(
            key,
            new VillagerTrade(
                new TradeCost(Items.EMERALD, 15),
                Optional.of(new TradeCost(Items.COMPASS, 1)),
                new ItemStackTemplate(Items.MAP),
                16,
                30,
                0.2F,
                Optional.empty(),
                List.of(
                    ExplorationMapFunction.makeExplorationMap()
                        .setDestination(destination)
                        .setMapDecoration(decoration)
                        .setSearchRadius(100)
                        .setSkipKnownStructures(true)
                        .build(),
                    SetNameFunction.setName(Component.translatable(LangKey.ITEM_KINGDOM_EXPLORER_MAP), SetNameFunction.Target.ITEM_NAME).build(),
                    FilteredFunction.filtered(
                            new ItemPredicate.Builder()
                                .of(items, Items.FILLED_MAP)
                                .withComponents(DataComponentMatchers.Builder.components().any(DataComponents.MAP_ID).build())
                                .build()
                        )
                        .onFail(Optional.of(DiscardItem.discardItem().build()))
                        .build()
                )
            )
        );
    }

    @SafeVarargs
    private static void registerTradeSet(final BootstrapContext<TradeSet> context, final ResourceKey<TradeSet> key, final ResourceKey<VillagerTrade>... trades) {
        HolderGetter<VillagerTrade> tradeLookup = context.lookup(Registries.VILLAGER_TRADE);
        List<Holder<VillagerTrade>> tradeHolders = new ArrayList<>(trades.length);

        for (ResourceKey<VillagerTrade> trade : trades) {
            tradeHolders.add(tradeLookup.getOrThrow(trade));
        }

        context.register(
            key,
            new TradeSet(
                HolderSet.direct(tradeHolders),
                ConstantValue.exactly(Math.min(2, tradeHolders.size())),
                false,
                Optional.of(key.identifier().withPrefix("trade_set/"))
            )
        );
    }

    private static ResourceKey<TradeSet> tradeSetKey(final String path) {
        return ResourceKey.create(Registries.TRADE_SET, DragonSurvival.res(path));
    }

    private static ResourceKey<VillagerTrade> villagerTradeKey(final String path) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, DragonSurvival.res(path));
    }
}
