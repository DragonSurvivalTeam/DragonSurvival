package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSwimDataEntry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class SwimData {
    public static final int UNLIMITED_OXYGEN = -1;

    public FluidType previousFluid;

    private final Map<ResourceKey<FluidType>, Integer> swimData = new HashMap<>();

    public Integer add(int maxOxygen, final Holder<FluidType> fluid) {
        return swimData.put(fluid.getKey(), maxOxygen);
    }

    public void remove(final Holder<FluidType> fluid) {
        swimData.remove(fluid.getKey());
    }

    public int getMaxOxygen(final Player player, final FluidType fluid) {
        ResourceKey<FluidType> key = key(fluid);
        float base;

        if (key == NeoForgeMod.EMPTY_TYPE.getKey()) {
            base = getMaxOxygen(player, previousFluid);
        } else {
            base = swimData.getOrDefault(key, Entity.TOTAL_AIR_SUPPLY);
        }

        if (base == UNLIMITED_OXYGEN) {
            return UNLIMITED_OXYGEN;
        }

        float bonus = player.getExistingData(DSDataAttachments.OXYGEN_BONUSES).map(data -> data.getBonus(key)).orElse(OxygenBonus.NONE);

        if (bonus == SwimData.UNLIMITED_OXYGEN) {
            return UNLIMITED_OXYGEN;
        }

        return Math.max(0, (int) (base + bonus));
    }

    public boolean canSwimIn(final FluidType fluid) {
        return canSwimIn(key(fluid));
    }

    public boolean canSwimIn(final ResourceKey<FluidType> fluid) {
        return swimData.containsKey(fluid);
    }

    public void sync(final ServerPlayer player) {
        swimData.forEach((fluid, oxygen) -> {
            PacketDistributor.sendToPlayer(player, new SyncSwimDataEntry(oxygen, player.registryAccess().holderOrThrow(fluid), false));
        });
    }

    // TODO :: remove?
    public static SwimData getData(final Player player) {
        return player.getData(DSDataAttachments.SWIM);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleOxygen(final LivingBreatheEvent event) {
        if (event.getEntity() instanceof Player player) {
            FluidType currentFluid = player.getEyeInFluidType();
            SwimData data = getData(player);

            if (event.canBreathe()) {
                if (data.canSwimIn(currentFluid)) {
                    event.setRefillAirAmount(data.getMaxOxygen(player, currentFluid));
                } else if (isAir(currentFluid) && data.previousFluid != null) {
                    // Vanilla: max. of 300, refill 4 -> ~ 1.5%
                    // TODO :: make the rate configurable?
                    event.setRefillAirAmount((int) (data.getMaxOxygen(player, data.previousFluid) * 0.015));
                }
            }

            if (!isAir(currentFluid) && data.previousFluid != currentFluid) {
                int maxAirSupply = data.getMaxOxygen(player, data.previousFluid);
                int newMaxAirSupply = data.getMaxOxygen(player, player.getEyeInFluidType());

                float airSupplyRatio = (float) newMaxAirSupply / (float) maxAirSupply;
                player.setAirSupply((int) Math.min(newMaxAirSupply, Math.ceil(player.getAirSupply() * airSupplyRatio)));

                data.previousFluid = currentFluid;
            }
        }
    }

    public static boolean isAir(final FluidType fluid) {
        return key(fluid) == NeoForgeMod.EMPTY_TYPE.getKey();
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
