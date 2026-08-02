package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonBoots;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonChestplate;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonHelmet;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonLeggings;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@EventBusSubscriber(Dist.CLIENT)
public class DSEquipment {
    public static final ArmorMaterial LIGHT_DRAGON_ARMOR_MATERIAL = new DragonArmorMaterial("light_dragon");
    public static final ArmorMaterial DARK_DRAGON_ARMOR_MATERIAL = new DragonArmorMaterial("dark_dragon");

    public static final Tier DRAGON_HUNTER = new ForgeTier(
            4,
            2031,
            9,
            5,
            15,
            BlockTags.NEEDS_DIAMOND_TOOL,
            () -> Ingredient.of(Items.NETHERITE_INGOT)
    );

    private record DragonArmorMaterial(String path) implements ArmorMaterial {
        private static final int DURABILITY_MULTIPLIER = 100;

        @Override
        public int getDurabilityForType(final ArmorItem.Type type) {
            return switch (type) {
                case BOOTS -> 13;
                case LEGGINGS -> 15;
                case CHESTPLATE -> 16;
                case HELMET -> 11;
            } * DURABILITY_MULTIPLIER;
        }

        @Override
        public int getDefenseForType(final ArmorItem.Type type) {
            return switch (type) {
                case BOOTS, HELMET -> 3;
                case LEGGINGS -> 6;
                case CHESTPLATE -> 8;
            };
        }

        @Override
        public int getEnchantmentValue() {
            return 30;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.IRON_GOLEM_STEP;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(DSItems.ELDER_DRAGON_HEART.get());
        }

        @Override
        public String getName() {
            return MODID + ":" + path;
        }

        @Override
        public float getToughness() {
            return 3;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.1f;
        }
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DragonChestplate.LAYER_LOCATION, DragonChestplate::createBodyLayer);
        event.registerLayerDefinition(DragonLeggings.LAYER_LOCATION, DragonLeggings::createBodyLayer);
        event.registerLayerDefinition(DragonHelmet.LAYER_LOCATION, DragonHelmet::createBodyLayer);
        event.registerLayerDefinition(DragonBoots.LAYER_LOCATION, DragonBoots::createBodyLayer);
    }

}
