package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DSCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonSurvivalMod.MODID);

    @SuppressWarnings("unused")
    public static RegistryObject<CreativeModeTab> DS_TAB = CREATIVE_MODE_TABS.register("dragon_survival", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(DSItems.elderDragonBone))
            .title(Component.translatable("itemGroup.dragon.survival.blocks"))
            .displayItems((parameters, output) -> {
                /* entries will be unordered with this approach
                List<Item> hidden = List.of(
                        DSItems.huntingNet,
                        DSItems.passiveMagicBeacon,
                        DSItems.passivePeaceBeacon,
                        DSItems.passiveFireBeacon,
                        DSItems.lightningTextureItem,
                        DSItems.inactivePeaceDragonBeacon,
                        DSItems.inactiveMagicDragonBeacon,
                        DSItems.inactiveFireDragonBeacon
                );

                DSItems.DS_ITEMS.forEach((key, value) -> {
                    if (!hidden.contains(value)) {
                        output.accept(value);
                    }
                });

                DSBlocks.DS_BLOCKS.forEach((key, value) -> output.accept(value));
                */

                List<ItemLike> items = List.of(
                        /* dragon altars */
                        DSBlocks.dragon_altar_stone,
                        DSBlocks.dragon_altar_sandstone,
                        DSBlocks.dragon_altar_red_sandstone,
                        DSBlocks.dragon_altar_purpur_block,
                        DSBlocks.dragon_altar_oak_log,
                        DSBlocks.dragon_altar_nether_bricks,
                        DSBlocks.dragon_altar_mossy_cobblestone,
                        DSBlocks.dragon_altar_blackstone,
                        DSBlocks.dragon_altar_birch_log,
                        /* dragon items */
                        DSItems.elderDragonDust,
                        DSItems.elderDragonBone,
                        DSItems.dragonHeartShard,
                        DSItems.weakDragonHeart,
                        DSItems.elderDragonHeart,
                        DSItems.starBone,
                        DSItems.starHeart,
                        /* special items */
                        DSItems.wingGrantItem,
                        DSItems.spinGrantItem,
                        /* food */
                        DSItems.seaDragonTreat,
                        DSItems.forestDragonTreat,
                        DSItems.caveDragonTreat,
                        DSItems.hotDragonRod,
                        DSItems.explosiveCopper,
                        DSItems.doubleQuartz,
                        DSItems.quartzExplosiveCopper,
                        DSItems.charredMeat,
                        DSItems.charredVegetable,
                        DSItems.charredMushroom,
                        DSItems.charredSeafood,
                        DSItems.chargedCoal,
                        DSItems.chargedSoup,
                        DSItems.meatWildBerries,
                        DSItems.smellyMeatPorridge,
                        DSItems.sweetSourRabbit,
                        DSItems.meatChorusMix,
                        DSItems.diamondChorus,
                        DSItems.luminousOintment,
                        DSItems.frozenRawFish,
                        DSItems.seasonedFish,
                        DSItems.goldenCoralPufferfish,
                        DSItems.goldenTurtleEgg,
                        /* beacons */
                        DSBlocks.dragonBeacon,
                        DSBlocks.peaceDragonBeacon,
                        DSBlocks.magicDragonBeacon,
                        DSBlocks.fireDragonBeacon,
                        /* source of magic */
                        DSBlocks.forestSourceOfMagic,
                        DSBlocks.caveSourceOfMagic,
                        DSBlocks.seaSourceOfMagic,
                        /* misc */
                        DSBlocks.dragonMemoryBlock,
                        /* dragon treasure */
                        DSBlocks.treasureDebris,
                        DSBlocks.treasureDiamond,
                        DSBlocks.treasureEmerald,
                        DSBlocks.treasureCopper,
                        DSBlocks.treasureGold,
                        DSBlocks.treasureIron,
                        /* decoration */
                        DSBlocks.helmet2,
                        DSBlocks.helmet1,
                        DSBlocks.helmet3,
                        /* summon items */
                        DSItems.princeSummon,
                        DSItems.princessSummon,
                        /* dragon doors */
                        DSBlocks.caveDoor,
                        DSBlocks.forestDoor,
                        DSBlocks.seaDoor,
                        DSBlocks.spruceDoor,
                        DSBlocks.legacyDoor,
                        DSBlocks.oakDoor,
                        DSBlocks.acaciaDoor,
                        DSBlocks.birchDoor,
                        DSBlocks.jungleDoor,
                        DSBlocks.darkOakDoor,
                        DSBlocks.crimsonDoor,
                        DSBlocks.warpedDoor,
                        DSBlocks.ironDoor,
                        DSBlocks.murdererDoor,
                        DSBlocks.sleeperDoor,
                        DSBlocks.stoneDoor,
                        /* small dragon doors*/
                        DSBlocks.caveSmallDoor,
                        DSBlocks.forestSmallDoor,
                        DSBlocks.seaSmallDoor,
                        DSBlocks.spruceSmallDoor,
                        DSBlocks.oakSmallDoor,
                        DSBlocks.acaciaSmallDoor,
                        DSBlocks.birchSmallDoor,
                        DSBlocks.jungleSmallDoor,
                        DSBlocks.darkOakSmallDoor,
                        DSBlocks.crimsonSmallDoor,
                        DSBlocks.warpedSmallDoor,
                        DSBlocks.ironSmallDoor,
                        DSBlocks.murdererSmallDoor,
                        DSBlocks.sleeperSmallDoor,
                        DSBlocks.stoneSmallDoor,
                        /* pressure plates */
                        DSBlocks.humanPressurePlate,
                        DSBlocks.dragonPressurePlate,
                        DSBlocks.cavePressurePlate,
                        DSBlocks.seaPressurePlate,
                        DSBlocks.forestPressurePlate
                );

                // debug
//                List<ResourceLocation> dragonSurvivalItems = new ArrayList<>();
//                DSItems.DS_ITEMS.forEach((key, value) -> dragonSurvivalItems.add(value.builtInRegistryHolder().key().location()));
//                DSBlocks.DS_BLOCKS.forEach((key, value) -> dragonSurvivalItems.add(value.asItem().builtInRegistryHolder().key().location()));

                items.forEach(item -> {
//                    dragonSurvivalItems.remove(item.asItem().builtInRegistryHolder().key().location());
                    output.accept(item);
                });

//                dragonSurvivalItems.forEach(location -> DragonSurvivalMod.LOGGER.warn("missing from creative tab: [{}]", location));
            })
            .build());

    @SubscribeEvent
    public static void buildCreativeModeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            DSEntities.SPAWN_EGGS.forEach(event::accept);
        }
    }
}
