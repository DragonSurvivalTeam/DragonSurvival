package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.entity.npc.villager.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

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
            () -> new VillagerProfession(Component.literal("dragon_rider"),
                    holder -> holder.value() == DRAGON_RIDER_POI.value(),
                    poiTypeHolder -> poiTypeHolder.value() == DRAGON_RIDER_POI.value(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_ARMORER));

    public static final Int2ObjectMap<VillagerTrades.ItemListing[]> LEADER_TRADES = new Int2ObjectOpenHashMap<>();

    // Required for map trades
    public static final TagKey<Structure> ON_DRAGON_HUNTERS_CASTLE_MAPS = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("on_dragon_hunter_maps"));

    @SubscribeEvent
    public static void addCustomTrades(final VillagerTradesEvent event) {
        if (event.getType() == DSTrades.DRAGON_RIDER_PROFESSION) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            trades.get(1).add(new VillagerTrades.EnchantBookForEmeralds(10, TagKey.create(Registries.ENCHANTMENT, DSEnchantments.UNBREAKABLE_SPIRIT.identifier())));
            trades.get(2).add(new VillagerTrades.EnchantBookForEmeralds(20, TagKey.create(Registries.ENCHANTMENT, DSEnchantments.COMBAT_RECOVERY.identifier())));
            trades.get(3).add(new VillagerTrades.EnchantBookForEmeralds(20, TagKey.create(Registries.ENCHANTMENT, DSEnchantments.AERODYNAMIC_MASTERY.identifier())));
            trades.get(4).add(new VillagerTrades.EnchantBookForEmeralds(20, TagKey.create(Registries.ENCHANTMENT, DSEnchantments.SACRED_SCALES.identifier())));
            trades.get(5).add(new VillagerTrades.ItemsForEmeralds(new ItemStack(DSItems.LIGHT_KEY, 1), 32 /* Cost */, 1 /* Item count */, 12 /* Max Uses */, 35 /* XP */, 1 /* Price multiplier */, Optional.empty()));

            // Declare the leader trades in here, since this event only fires once and if we do it statically it might try to initialize in cases where we don't actually have a minecraft instance yet.
            final List<ItemListing> LEADER_TRADES_LEVEL_1 = Lists.newArrayList(
                    new VillagerTrades.EmeraldForItems(DSItems.DRAGON_HEART_SHARD.value(), 1 /* Amount */, 16 /* Max uses */, 5 /* XP */, 1 /* Emerald amount */),
                    new VillagerTrades.ItemsForEmeralds(new ItemStack(DSItems.PARTISAN, 1), 12 /* Cost */, 1 /* Item count */, 1 /* Max Uses */, 5 /* XP */, 1 /* Price multiplier */, Optional.empty())
            );

            final List<ItemListing> LEADER_TRADES_LEVEL_2 = Lists.newArrayList(
                    new VillagerTrades.EmeraldForItems(DSItems.WEAK_DRAGON_HEART.value(), 1 /* Amount */, 16 /* Max uses */, 10 /* XP */, 1 /* Emerald amount */)
            );

            final List<ItemListing> LEADER_TRADES_LEVEL_3 = Lists.newArrayList(
                    new VillagerTrades.ItemsForEmeralds(new ItemStack(DSItems.HUNTER_KEY, 1), 32 /* Cost */, 1 /* Item count */, 16 /* Max Uses */, 35 /* XP */, 1 /* Price multiplier */, Optional.empty())
            );

            final List<ItemListing> LEADER_TRADES_LEVEL_4 = Lists.newArrayList(
                    new VillagerTrades.EmeraldForItems(DSItems.ELDER_DRAGON_HEART.value(), 1 /* Amount */, 12 /* Max uses */, 35 /* XP */, 12 /* Emerald amount */)
            );

            final List<ItemListing> LEADER_TRADES_LEVEL_5 = Lists.newArrayList(
                    new VillagerTrades.EnchantBookForEmeralds(15, TagKey.create(Registries.ENCHANTMENT, DSEnchantments.DRAGONSBANE.identifier())),
                    new VillagerTrades.EnchantBookForEmeralds(15, TagKey.create(Registries.ENCHANTMENT, DSEnchantments.BOLAS.identifier()))
            );

            LEADER_TRADES.put(1, LEADER_TRADES_LEVEL_1.toArray(new VillagerTrades.ItemListing[0]));
            LEADER_TRADES.put(2, LEADER_TRADES_LEVEL_2.toArray(new VillagerTrades.ItemListing[0]));
            LEADER_TRADES.put(3, LEADER_TRADES_LEVEL_3.toArray(new VillagerTrades.ItemListing[0]));
            LEADER_TRADES.put(4, LEADER_TRADES_LEVEL_4.toArray(new VillagerTrades.ItemListing[0]));
            LEADER_TRADES.put(5, LEADER_TRADES_LEVEL_5.toArray(new VillagerTrades.ItemListing[0]));
        }

        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            trades.get(2).add(new VillagerTrades.TreasureMapForEmeralds(15, ON_DRAGON_HUNTERS_CASTLE_MAPS, LangKey.ITEM_KINGDOM_EXPLORER_MAP, DSMapDecorationTypes.DRAGON_HUNTER, 16, 30));
        }
    }
}