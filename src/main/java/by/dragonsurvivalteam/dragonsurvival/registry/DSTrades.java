package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public class DSTrades {
    public static final DeferredRegister<PoiType> POI_REGISTRY = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, DragonSurvival.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSION_REGISTRY = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, DragonSurvival.MODID);

    public static final Holder<PoiType> DRAGON_RIDER_POI = POI_REGISTRY.register(
            "dragon_rider_poi",
            () -> new PoiType(ImmutableSet.copyOf(DSBlocks.DRAGON_RIDER_WORKBENCH.get().getStateDefinition().getPossibleStates()), 1, 1));

    @Translation(type = Translation.Type.VILLAGER_PROFESSION, comments = {"Dragon Rider"})
    public static final Holder<VillagerProfession> DRAGON_RIDER_PROFESSION = PROFESSION_REGISTRY.register(
            "dragon_rider",
            () -> new VillagerProfession("dragon_rider",
                    holder -> holder.value() == DRAGON_RIDER_POI.value(),
                    poiTypeHolder -> poiTypeHolder.value() == DRAGON_RIDER_POI.value(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_ARMORER));

    public static class ItemTrade implements VillagerTrades.ItemListing {
        private final ItemCost item;
        private final ItemStack result;
        private final int maxUses;
        private final float priceMultiplier;
        private final int xp;

        public ItemTrade(final ItemStack item, final ItemStack result, int maxUses, int xp) {
            this.item = new ItemCost(item.getItem(), item.getCount());
            this.result = result;
            this.maxUses = maxUses;
            this.priceMultiplier = 0;
            this.xp = xp;
        }

        public ItemTrade(final ItemStack item, final ItemStack result, int maxUses, float priceMultiplier, int xp) {
            this.item = new ItemCost(item.getItem(), item.getCount());
            this.result = result;
            this.maxUses = maxUses;
            this.priceMultiplier = priceMultiplier;
            this.xp = xp;
        }

        @Nullable @Override
        public MerchantOffer getOffer(@NotNull final Entity entity, @NotNull final RandomSource random) {
            return new MerchantOffer(item, Optional.empty(), result, maxUses, xp, priceMultiplier);
        }
    }

    /** Copied from {@link net.minecraft.world.entity.npc.VillagerTrades.TreasureMapForEmeralds} */
    public static class TreasureMapForEmeralds implements VillagerTrades.ItemListing {
        private final int emeraldCost;
        private final TagKey<Structure> destination;
        private final String displayName;
        private final Holder<MapDecorationType> destinationType;
        private final int maxUses;
        private final int villagerXp;

        public TreasureMapForEmeralds(int pEmeraldCost, TagKey<Structure> pDestination, String pDisplayName, Holder<MapDecorationType> pDestinationType, int pMaxUses, int pVillagerXp) {
            this.emeraldCost = pEmeraldCost;
            this.destination = pDestination;
            this.displayName = pDisplayName;
            this.destinationType = pDestinationType;
            this.maxUses = pMaxUses;
            this.villagerXp = pVillagerXp;
        }

        @Nullable @Override
        public MerchantOffer getOffer(final Entity trader, @NotNull final RandomSource random) {
            if (!(trader.level() instanceof ServerLevel serverlevel)) {
                return null;
            } else {
                BlockPos blockpos = serverlevel.findNearestMapStructure(this.destination, trader.blockPosition(), 100, true);

                if (blockpos != null) {
                    ItemStack itemstack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte) 2, true, true);
                    MapItem.renderBiomePreviewMap(serverlevel, itemstack);
                    MapItemSavedData.addTargetDecoration(itemstack, blockpos, "+", this.destinationType);
                    itemstack.set(DataComponents.ITEM_NAME, Component.translatable(this.displayName));

                    return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(new ItemCost(Items.COMPASS)), itemstack, this.maxUses, this.villagerXp, 0.2F);
                } else {
                    return null;
                }
            }
        }
    }

    /** Copied from {@link net.minecraft.world.entity.npc.VillagerTrades.EnchantBookForEmeralds} */
    static class EnchantBookForEmeralds implements VillagerTrades.ItemListing {
        private final ResourceKey<Enchantment> enchantment;
        private final int villagerXp;
        private final int minLevel;
        private final int maxLevel;

        public EnchantBookForEmeralds(final ResourceKey<Enchantment> enchantment, int villagerExperience) {
            this(enchantment, villagerExperience, 0, Integer.MAX_VALUE);
        }

        public EnchantBookForEmeralds(final ResourceKey<Enchantment> enchantment, int villagerExperience, int minLevel, int maxLevel) {
            this.enchantment = enchantment;
            this.villagerXp = villagerExperience;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
        }

        @Override
        public MerchantOffer getOffer(@NotNull final Entity trader, @NotNull final RandomSource random) {
            Holder<Enchantment> enchantment = EnchantmentUtils.getHolder(this.enchantment);

            if (enchantment == null) {
                DragonSurvival.LOGGER.warn("Enchantment [{}] is not present - cannot create proper trade offer", this.enchantment.location());
                return new MerchantOffer(new ItemCost(Items.EMERALD, 1), Optional.empty(), Items.BOOK.getDefaultInstance(), 1, 0, 1);
            }

            int minLevel = Math.max(enchantment.value().getMinLevel(), this.minLevel);
            int maxLevel = Math.min(enchantment.value().getMaxLevel(), this.maxLevel);
            int level = Mth.nextInt(random, minLevel, maxLevel);

            ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
            int cost = 2 + random.nextInt(5 + level * 10) + 3 * level;

            if (enchantment.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                cost *= 2;
            }

            if (cost > 64) {
                cost = 64;
            }

            return new MerchantOffer(new ItemCost(Items.EMERALD, cost), Optional.of(new ItemCost(Items.BOOK)), book, 12, this.villagerXp, 0.2f);
        }
    }

    public static final Int2ObjectMap<VillagerTrades.ItemListing[]> LEADER_TRADES = new Int2ObjectOpenHashMap<>();

    // Required for map trades
    public static final TagKey<Structure> ON_DRAGON_HUNTERS_CASTLE_MAPS = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("on_dragon_hunter_maps"));

    @SubscribeEvent
    public static void addCustomTrades(final VillagerTradesEvent event) {
        if (event.getType() == DSTrades.DRAGON_RIDER_PROFESSION.value()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            trades.get(1).add(new EnchantBookForEmeralds(DSEnchantments.UNBREAKABLE_SPIRIT, 10));
            trades.get(2).add(new EnchantBookForEmeralds(DSEnchantments.COMBAT_RECOVERY, 20));
            trades.get(3).add(new EnchantBookForEmeralds(DSEnchantments.AERODYNAMIC_MASTERY, 20));
            trades.get(4).add(new EnchantBookForEmeralds(DSEnchantments.SACRED_SCALES, 20));
            trades.get(5).add(new ItemTrade(new ItemStack(Items.EMERALD, 32), new ItemStack(DSItems.LIGHT_KEY, 1), 12, 35));

            // Declare the leader trades in here, since this event only fires once and if we do it statically it might try to initialize in cases where we don't actually have a minecraft instance yet.
            final List<ItemListing> LEADER_TRADES_LEVEL_1 = Lists.newArrayList(
                    new ItemTrade(new ItemStack(DSItems.DRAGON_HEART_SHARD, 1), new ItemStack(Items.EMERALD, 1), 16, 1, 5),
                    new ItemTrade(new ItemStack(Items.EMERALD, 12), new ItemStack(DSItems.PARTISAN, 1), 1, 1, 5)
            );

            final List<ItemListing> LEADER_TRADES_LEVEL_2 = Lists.newArrayList(
                    new ItemTrade(new ItemStack(DSItems.WEAK_DRAGON_HEART, 1), new ItemStack(Items.EMERALD, 1), 16, 1, 10)
            );

            final List<ItemListing> LEADER_TRADES_LEVEL_3 = Lists.newArrayList(
                    new ItemTrade(new ItemStack(Items.EMERALD, 32), new ItemStack(DSItems.HUNTER_KEY, 1), 16, 1, 35)
            );

            final List<ItemListing> LEADER_TRADES_LEVEL_4 = Lists.newArrayList(
                    new ItemTrade(new ItemStack(DSItems.ELDER_DRAGON_HEART, 1), new ItemStack(Items.EMERALD, 12), 12, 1, 25)
            );

            final List<ItemListing> LEADER_TRADES_LEVEL_5 = Lists.newArrayList(
                    new EnchantBookForEmeralds(DSEnchantments.DRAGONSBANE, 15),
                    new EnchantBookForEmeralds(DSEnchantments.BOLAS, 15)
            );

            LEADER_TRADES.put(1, LEADER_TRADES_LEVEL_1.toArray(new VillagerTrades.ItemListing[0]));
            LEADER_TRADES.put(2, LEADER_TRADES_LEVEL_2.toArray(new VillagerTrades.ItemListing[0]));
            LEADER_TRADES.put(3, LEADER_TRADES_LEVEL_3.toArray(new VillagerTrades.ItemListing[0]));
            LEADER_TRADES.put(4, LEADER_TRADES_LEVEL_4.toArray(new VillagerTrades.ItemListing[0]));
            LEADER_TRADES.put(5, LEADER_TRADES_LEVEL_5.toArray(new VillagerTrades.ItemListing[0]));
        }

        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            trades.get(2).add(new TreasureMapForEmeralds(15, ON_DRAGON_HUNTERS_CASTLE_MAPS, LangKey.ITEM_KINGDOM_EXPLORER_MAP, DSMapDecorationTypes.DRAGON_HUNTER, 16, 30));
        }
    }
}