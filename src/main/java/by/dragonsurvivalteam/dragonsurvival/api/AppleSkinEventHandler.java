package by.dragonsurvivalteam.dragonsurvival.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.food.FoodProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

@OnlyIn(Dist.CLIENT)
public class AppleSkinEventHandler {
    @SubscribeEvent
    public void onToolTipEvent(final FoodValuesEvent event) {
        FoodProperties properties = DragonFood.getEffectiveFoodProperties(event.itemStack.getItem(), Minecraft.getInstance().player);

        if (properties == null) {
            return;
        }

        event.modifiedFoodValues = new FoodValues(properties.getNutrition(), properties.getSaturationModifier());
    }
}
