package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class SwimData {
    public static final int UNLIMITED_OXYGEN = -1;
    public static final int NO_MODIFICATION = 0;

    public FluidType previousEyeInFluidType;
    public FluidType eyeInFluidTypeLastTick;

    private final Map<ResourceKey<FluidType>, Integer> swimData = new HashMap<>();

    public void add(int maxOxygen, final Holder<FluidType> fluid) {
        swimData.put(fluid.getKey(), maxOxygen);
    }

    public void remove(final Holder<FluidType> fluid) {
        swimData.remove(fluid.getKey());
    }

    public int getMaxOxygen(final FluidType fluid) {
        return swimData.getOrDefault(key(fluid), Entity.TOTAL_AIR_SUPPLY);
    }

    public boolean canSwimIn(final FluidType fluid) {
        return swimData.containsKey(key(fluid));
    }

    public static SwimData getData(final Player player) {
        return player.getData(DSDataAttachments.SWIM);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void allowForInfiniteOxygen(final LivingBreatheEvent event) {
        if (event.getEntity() instanceof Player player) {
            SwimData data = getData(player);
            boolean canBreathe = data.getMaxOxygen(event.getEntity().getEyeInFluidType()) == UNLIMITED_OXYGEN;

            if (canBreathe) {
                event.setConsumeAirAmount(0);
                event.setRefillAirAmount(data.getMaxOxygen(event.getEntity().getEyeInFluidType()));
            }

            if (data.canSwimIn(event.getEntity().getEyeInFluidType())) {
                event.setCanBreathe(canBreathe);
            }

            FluidType currentFluidType = player.getEyeInFluidType();

            if (data.previousEyeInFluidType != currentFluidType) {
                int maxAirSupply = data.getMaxOxygen(data.previousEyeInFluidType);
                int newMaxAirSupply = data.getMaxOxygen(player.getEyeInFluidType());

                float airSupplyRatio = (float) newMaxAirSupply / (float) maxAirSupply;

                player.setAirSupply((int) Math.min(newMaxAirSupply, Math.ceil(player.getAirSupply() * airSupplyRatio)));
                data.previousEyeInFluidType = currentFluidType;
                data.eyeInFluidTypeLastTick = player.getEyeInFluidType();
            }
        }
    }

    public static @Nullable ResourceLocation getAirSprite(final FluidType fluid) {
        ResourceLocation resource = NeoForgeRegistries.FLUID_TYPES.getKey(fluid);

        if (resource == null) {
            return null;
        }

        // TODO :: should this always use the ds namespace?
        return DragonSurvival.res("air_meters/" + resource.getPath());
    }

    public static ResourceLocation getAirBurstSprite(final FluidType fluid) {
        ResourceLocation resource = NeoForgeRegistries.FLUID_TYPES.getKey(fluid);

        if (resource == null) {
            return null;
        }

        // TODO :: should this always use the ds namespace?
        return DragonSurvival.res("air_meters/" + resource.getPath() + "_burst");
    }

    public static @Nullable ResourceKey<FluidType> key(final FluidType fluid) {
        return NeoForgeRegistries.FLUID_TYPES.getResourceKey(fluid).orElse(null);
    }
}
