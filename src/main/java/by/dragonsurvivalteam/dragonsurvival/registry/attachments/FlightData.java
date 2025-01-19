package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FlightData implements INBTSerializable<CompoundTag> {
    public static final ResourceLocation DEFAULT_ICON = DragonSurvival.res("textures/ability_effect/generic_icons/dragon_wings.png");

    public ResourceLocation icon = DEFAULT_ICON;
    public @Nullable HolderSet<FluidType> inFluid;

    public boolean areWingsSpread;
    public boolean hasFlight;
    public boolean hasSpin;

    public int cooldown;
    public int duration;

    public static FlightData getData(final Player player) {
        return player.getData(DSDataAttachments.FLIGHT);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(ARE_WINGS_SPREAD, areWingsSpread);
        tag.putBoolean(FLIGHT, hasFlight);
        tag.putBoolean(SPIN, hasSpin);
        tag.putInt(COOLDOWN, cooldown);
        tag.putInt(DURATION, duration);

        if (inFluid != null) {
            RegistryCodecs.homogeneousList(NeoForgeRegistries.Keys.FLUID_TYPES).encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), inFluid)
                    .resultOrPartial(DragonSurvival.LOGGER::error).ifPresent(list -> tag.put(IN_FLUID, list));
        }

        tag.putString(ICON, icon.toString());
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        areWingsSpread = tag.getBoolean(ARE_WINGS_SPREAD);
        hasFlight = tag.getBoolean(FLIGHT);
        hasSpin = tag.getBoolean(SPIN);
        cooldown = tag.getInt(COOLDOWN);
        duration = tag.getInt(DURATION);

        if (tag.contains(IN_FLUID)) {
            inFluid = RegistryCodecs.homogeneousList(NeoForgeRegistries.Keys.FLUID_TYPES).decode(provider.createSerializationContext(NbtOps.INSTANCE), tag.get(IN_FLUID))
                    .resultOrPartial(DragonSurvival.LOGGER::error).map(Pair::getFirst).orElse(null);
        } else {
            inFluid = null;
        }

        icon = Objects.requireNonNullElse(ResourceLocation.tryParse(tag.getString(ICON)), DEFAULT_ICON);
    }

    public void sync(final ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncData(player.getId(), DSDataAttachments.FLIGHT.getId(), serializeNBT(player.registryAccess())));
    }

    // Needed for when a player enters tracking range, as the flight data has a visual impact (whether the wings are spread or not)
    public void sync(final ServerPlayer source, final ServerPlayer target) {
        PacketDistributor.sendToPlayer(target, new SyncData(source.getId(), DSDataAttachments.FLIGHT.getId(), serializeNBT(source.registryAccess())));
    }

    public boolean hasFlight() {
        return hasFlight;
    }

    public boolean isWingsSpread() {
        return hasFlight && areWingsSpread;
    }

    public static final ClientEffectProvider FLIGHT_EFFECT = new ClientEffectProvider() {
        @Translation(type = Translation.Type.GUI, comments = "Dragon Wings")
        private static final ResourceLocation NAME = DragonSurvival.res("dragon_wings");

        private static final ResourceLocation ID = DragonSurvival.res("dragon_wings");
        private static final Component TRANSLATED_NAME = Component.translatable(Translation.Type.GUI.wrap(NAME));

        @Override
        public Component getDescription() {
            return Component.empty();
        }

        @Override
        public ClientData clientData() {
            Player player = DragonSurvival.PROXY.getLocalPlayer();
            ResourceLocation icon = player != null ? FlightData.getData(player).icon : DEFAULT_ICON;
            return new ClientData(ID, icon, TRANSLATED_NAME, CommonComponents.EMPTY);
        }

        @Override
        public int getDuration() {
            return DurationInstance.INFINITE_DURATION;
        }

        @Override
        public int currentDuration() {
            return 0;
        }
    };

    private static final String COOLDOWN = "cooldown";
    private static final String DURATION = "duration";
    private static final String FLIGHT = "flight";
    private static final String SPIN = "spin";
    private static final String ARE_WINGS_SPREAD = "are_wings_spread";
    private static final String IN_FLUID = "in_fluid";
    private static final String ICON = "icon";
}
