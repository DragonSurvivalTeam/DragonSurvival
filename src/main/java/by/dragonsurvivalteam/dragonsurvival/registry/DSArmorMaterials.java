package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonBoots;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonChestplate;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonHelmet;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonLeggings;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(Dist.CLIENT)
public class DSArmorMaterials
{
    public static ArmorMaterial DRAGON_ARMOR_MATERIAL = new ArmorMaterial(
        37,
        ArmorMaterials.makeDefense(3, 6, 8, 3, 11),
        15,
        new Holder.Direct<>(SoundEvents.IRON_GOLEM_STEP),
        3.0F,
        0.1F,
        ItemTags.REPAIRS_NETHERITE_ARMOR,
        EquipmentAssets.IRON
    );

    // TODO :: What is the purpose of this in 1.21.11?
    /*public static final Tier DRAGON_HUNTER = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2031,
            9,
            5,
            15,
            () -> Ingredient.of(Items.NETHERITE_INGOT)
    );*/

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DragonChestplate.LAYER_LOCATION, DragonChestplate::createBodyLayer);
        event.registerLayerDefinition(DragonLeggings.LAYER_LOCATION, DragonLeggings::createBodyLayer);
        event.registerLayerDefinition(DragonHelmet.LAYER_LOCATION, DragonHelmet::createBodyLayer);
        event.registerLayerDefinition(DragonBoots.LAYER_LOCATION, DragonBoots::createBodyLayer);
    }

}
