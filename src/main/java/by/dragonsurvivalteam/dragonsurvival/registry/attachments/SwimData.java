package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSwimDataEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SwimEffect;
import by.dragonsurvivalteam.dragonsurvival.util.FluidTypeUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class SwimData implements ValueIOSerializable {
    public static final int UNLIMITED_OXYGEN = -1;

    /** To adjust the air supply when switching to air or other fluids */
    public FluidType previousFluid;

    private final Map<ResourceKey<FluidType>, Entry> swimData = new HashMap<>();

    public Entry add(final SwimEffect.Entry entry) {
        return swimData.put(entry.fluidType(), new Entry(entry.maxOxygen(), entry.hasStableSwim()));
    }

    public @Nullable Entry remove(final ResourceKey<FluidType> fluid) {
        return swimData.remove(fluid);
    }

    public int getMaxOxygen(final Player player, final FluidType fluid) {
        ResourceKey<FluidType> key = key(fluid);
        float base;

        if (key == NeoForgeMod.EMPTY_TYPE.getKey()) {
            base = getMaxOxygen(player, previousFluid);
        } else {
            base = swimData.getOrDefault(key, Entry.VANILLA).maxOxygen();
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

    public boolean hasStableSwim(final FluidType fluid) {
        ResourceKey<FluidType> key = key(fluid);
        Entry entry = swimData.get(key);
        return entry != null && entry.hasStableSwim();
    }

    public boolean canSwimIn(final FluidType fluid) {
        return canSwimIn(key(fluid));
    }

    public boolean canSwimIn(final ResourceKey<FluidType> fluid) {
        return swimData.containsKey(fluid);
    }

    public void sync(final ServerPlayer player) {
        swimData.forEach((fluid, entry) -> PacketDistributor.sendToPlayer(player, new SyncSwimDataEntry(entry.convert(fluid), false)));
    }

    // TODO :: remove?
    public static SwimData getData(final Player player) {
        return player.getData(DSDataAttachments.SWIM);
    }

    // FIXME :: Neo has yet to implement this event again for 26.1, waiting on them
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleOxygen(final LivingBreatheEvent event) {
        if (event.getEntity() instanceof Player player) {
            FluidType currentFluid = FluidTypeUtil.getEyeFluidType(player);
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
                int newMaxAirSupply = data.getMaxOxygen(player, FluidTypeUtil.getEyeFluidType(player));

                float airSupplyRatio = (float) newMaxAirSupply / (float) maxAirSupply;
                player.setAirSupply((int) Math.min(newMaxAirSupply, Math.ceil(player.getAirSupply() * airSupplyRatio)));

                data.previousFluid = currentFluid;
            }
        }
    }

    public static boolean isAir(final FluidType fluid) {
        return key(fluid) == NeoForgeMod.EMPTY_TYPE.getKey();
    }

    public static @Nullable Identifier getAirSprite(final FluidType fluid) {
        Identifier resource = NeoForgeRegistries.FLUID_TYPES.getKey(fluid);

        if (resource == null) {
            return null;
        }

        // TODO :: should this always use the ds namespace?
        return DragonSurvival.res("air_meters/" + resource.getPath());
    }

    public static Identifier getAirBurstSprite(final FluidType fluid) {
        Identifier resource = NeoForgeRegistries.FLUID_TYPES.getKey(fluid);

        if (resource == null) {
            return null;
        }

        // TODO :: should this always use the ds namespace?
        return DragonSurvival.res("air_meters/" + resource.getPath() + "_burst");
    }

    public static @Nullable ResourceKey<FluidType> key(final FluidType fluid) {
        return NeoForgeRegistries.FLUID_TYPES.getResourceKey(fluid).orElse(null);
    }

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {
        ValueOutput entries = valueOutput.child(SWIM_DATA);
        swimData.forEach((fluid, entry) -> entries.store(fluid.identifier().toString(), Entry.CODEC, entry));
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        swimData.clear();
        ValueInput entries = valueInput.childOrEmpty(SWIM_DATA);

        entries.keySet().forEach(key -> {
            Identifier fluidIdentifier = Identifier.tryParse(key);

            if (fluidIdentifier != null && NeoForgeRegistries.FLUID_TYPES.containsKey(fluidIdentifier)) {
                entries.read(key, Entry.CODEC).ifPresent(entry -> swimData.put(ResourceKey.create(NeoForgeRegistries.Keys.FLUID_TYPES, fluidIdentifier), entry));
            }
        });
    }

    private static final String SWIM_DATA = "swim_data";

    public record Entry(int maxOxygen, boolean hasStableSwim) {
        public static final Entry VANILLA = new Entry(Entity.TOTAL_AIR_SUPPLY, false);

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("max_oxygen").forGetter(Entry::maxOxygen),
                Codec.BOOL.fieldOf("has_stable_swim").forGetter(Entry::hasStableSwim)
        ).apply(instance, Entry::new));

        public SwimEffect.Entry convert(final ResourceKey<FluidType> fluidType) {
            return new SwimEffect.Entry(maxOxygen, hasStableSwim, fluidType);
        }
    }
}
