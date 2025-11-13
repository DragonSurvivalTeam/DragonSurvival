package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonBoots;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonChestplate;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonHelmet;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonLeggings;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;
import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.res;

@EventBusSubscriber(Dist.CLIENT)
public class DSEquipment {
    public static final DeferredRegister<ArmorMaterial> REGISTRY = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, MODID);

    public static final Holder<ArmorMaterial> LIGHT_DRAGON_ARMOR_MATERIAL =
            REGISTRY.register("light_dragon", () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 11);
                    }),
                    30,
                    Holder.direct(SoundEvents.IRON_GOLEM_STEP),
                    () -> Ingredient.of(DSItems.ELDER_DRAGON_HEART.value()),
                    List.of(new ArmorMaterial.Layer(res("light_dragon"))),
                    3,
                    0.1f
            ));

    public static final Holder<ArmorMaterial> DARK_DRAGON_ARMOR_MATERIAL =
            REGISTRY.register("dark_dragon", () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 11);
                    }),
                    30,
                    Holder.direct(SoundEvents.IRON_GOLEM_STEP),
                    () -> Ingredient.of(DSItems.ELDER_DRAGON_HEART.value()),
                    List.of(new ArmorMaterial.Layer(res("dark_dragon"))),
                    3,
                    0.1f
            ));

    public static final Tier DRAGON_HUNTER = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2031,
            9,
            5,
            15,
            () -> Ingredient.of(Items.NETHERITE_INGOT)
    );

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DragonChestplate.LAYER_LOCATION, DragonChestplate::createBodyLayer);
        event.registerLayerDefinition(DragonLeggings.LAYER_LOCATION, DragonLeggings::createBodyLayer);
        event.registerLayerDefinition(DragonHelmet.LAYER_LOCATION, DragonHelmet::createBodyLayer);
        event.registerLayerDefinition(DragonBoots.LAYER_LOCATION, DragonBoots::createBodyLayer);
    }

}
