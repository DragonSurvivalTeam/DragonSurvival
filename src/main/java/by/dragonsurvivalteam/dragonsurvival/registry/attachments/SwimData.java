package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import by.dragonsurvivalteam.dragonsurvival.common.serialization.INBTSerializable;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSwimDataEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SwimEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class SwimData implements INBTSerializable<CompoundTag> {
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

        if (key == ForgeMod.EMPTY_TYPE.getKey()) {
            base = getMaxOxygen(player, previousFluid);
        } else {
            base = swimData.getOrDefault(key, Entry.VANILLA).maxOxygen();
        }

        if (base == UNLIMITED_OXYGEN) {
            return UNLIMITED_OXYGEN;
        }

        float bonus = AttachmentManager.getExistingData(player, DSDataAttachments.OXYGEN_BONUSES).map(data -> data.getBonus(key)).orElse(OxygenBonus.NONE);

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
        return AttachmentManager.getData(player, DSDataAttachments.SWIM);
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
        return key(fluid) == ForgeMod.EMPTY_TYPE.getKey();
    }

    public static @Nullable ResourceLocation getAirSprite(final FluidType fluid) {
        ResourceLocation resource = ForgeRegistries.FLUID_TYPES.get().getKey(fluid);

        if (resource == null) {
            return null;
        }

        // TODO :: should this always use the ds namespace?
        return DragonSurvival.res("air_meters/" + resource.getPath());
    }

    public static ResourceLocation getAirBurstSprite(final FluidType fluid) {
        ResourceLocation resource = ForgeRegistries.FLUID_TYPES.get().getKey(fluid);

        if (resource == null) {
            return null;
        }

        // TODO :: should this always use the ds namespace?
        return DragonSurvival.res("air_meters/" + resource.getPath() + "_burst");
    }

    public static @Nullable ResourceKey<FluidType> key(final FluidType fluid) {
        return ForgeRegistries.FLUID_TYPES.get().getResourceKey(fluid).orElse(null);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        if (!swimData.isEmpty()) {
            CompoundTag entries = new CompoundTag();

            swimData.forEach((fluid, entry) -> Entry.CODEC.encodeStart(NbtOps.INSTANCE, entry)
                    .resultOrPartial(DragonSurvival.LOGGER::error)
                    .ifPresent(entryTag -> entries.put(fluid.location().toString(), entryTag)));

            tag.put(SWIM_DATA, entries);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        swimData.clear();
        CompoundTag entries = tag.getCompound(SWIM_DATA);

        entries.getAllKeys().forEach(key -> {
            ResourceLocation fluidIdentifier = ResourceLocation.tryParse(key);

            if (fluidIdentifier != null && ForgeRegistries.FLUID_TYPES.get().containsKey(fluidIdentifier)) {
                Entry.CODEC.decode(NbtOps.INSTANCE, entries.get(key)).resultOrPartial(DragonSurvival.LOGGER::error).ifPresent(entry -> {
                    swimData.put(ResourceKey.create(ForgeRegistries.Keys.FLUID_TYPES, fluidIdentifier), entry.getFirst());
                });
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
