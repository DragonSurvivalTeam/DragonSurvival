package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.mixins.EntityAccessor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Map;

@EventBusSubscriber
public class SwimData {
    private final Map<FluidType, Integer> swimData = new java.util.HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void allowForInfiniteOxygen(LivingBreatheEvent event) {
        if (event.getEntity() instanceof Player player) {
            SwimData data = getData(player);
            if (data.getMaxOxygen(event.getEntity().getEyeInFluidType()) == -1) {
                event.setConsumeAirAmount(0);
            }

            if (data.canSwimIn(event.getEntity().getEyeInFluidType())) {
                event.setCanBreathe(false);
            }

            FluidType previousFluidType = ((EntityAccessor)player).getEyeInFluidTypeLastTick();
            FluidType currentFluidType = player.getEyeInFluidType();
            if(previousFluidType != currentFluidType) {
                int maxAirSupply = data.getMaxOxygen(previousFluidType);
                int newMaxAirSupply = data.getMaxOxygen(player.getEyeInFluidType());
                float airSupplyRatio = (float) newMaxAirSupply / (float) maxAirSupply;
                player.setAirSupply((int) Math.min(newMaxAirSupply, Math.ceil(player.getAirSupply() * airSupplyRatio)));
                ((EntityAccessor)player).setPreviousEyeInFluidType(previousFluidType);
                ((EntityAccessor)player).setEyeInFluidTypeLastTick(player.getEyeInFluidType());
            }
        }
    }

    public static ResourceLocation getAirSprite(FluidType fluidType) {
        ResourceLocation fluidResourceLocation = NeoForgeRegistries.FLUID_TYPES.getKey(fluidType);
        if(fluidResourceLocation == null) {
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "air_meters/" + fluidResourceLocation.getPath());
    }

    public static ResourceLocation getAirBurstSprite(FluidType fluidType) {
        ResourceLocation fluidResourceLocation = NeoForgeRegistries.FLUID_TYPES.getKey(fluidType);
        if(fluidResourceLocation == null) {
            return null;
        }

        return ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "air_meters/" + fluidResourceLocation.getPath() + "_burst");
    }

    public static SwimData getData(final Player player) {
        return player.getData(DSDataAttachments.SWIM);
    }

    public void addEntry(int maxOxygen, Holder<FluidType> fluidType) {
        swimData.put(fluidType.value(), maxOxygen);
    }

    public void removeEntry(Holder<FluidType> fluidType) {
        swimData.remove(fluidType.value());
    }

    public int getMaxOxygen(FluidType fluidType) {
        for (Map.Entry<FluidType, Integer> entry : swimData.entrySet()) {
            if (entry.getKey().equals(fluidType)) {
                return entry.getValue();
            }
        }

        // Vanilla max oxygen value
        return 300;
    }

    public boolean canSwimIn(FluidType fluidType) {
        return swimData.containsKey(fluidType);
    }
}
