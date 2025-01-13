package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
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
    public @Nullable Holder<FluidType> swimSpinFluid;

    public boolean areWingsSpread;
    public boolean hasFlight;
    public boolean hasSpin;

    public int cooldown;
    public int duration;

    public static FlightData getData(final Player player) {
        return player.getData(DSDataAttachments.SPIN);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(ARE_WINGS_SPREAD, areWingsSpread);
        tag.putBoolean(FLIGHT, hasFlight);
        tag.putBoolean(SPIN, hasSpin);
        tag.putInt(COOLDOWN, cooldown);
        tag.putInt(DURATION, duration);

        if (swimSpinFluid != null) {
            tag.putString(SWIM_SPIN_FLUID, swimSpinFluid.getRegisteredName());
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

        ResourceKey<FluidType> fluid = ResourceHelper.decodeKey(provider, NeoForgeRegistries.Keys.FLUID_TYPES, tag, SWIM_SPIN_FLUID);

        if (fluid != null) {
            swimSpinFluid = provider.holderOrThrow(fluid);
        }

        icon = Objects.requireNonNullElse(ResourceLocation.tryParse(tag.getString(ICON)), DEFAULT_ICON);
    }

    public AttachmentType<?> type() {
        return DSDataAttachments.SPIN.get();
    }

    public void sync(final ServerPlayer player) {
        player.getExistingData(type()).ifPresent(data ->
                PacketDistributor.sendToPlayer(player, new SyncData(player.getId(), NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type()), serializeNBT(player.registryAccess())))
        );
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

        @Override
        public Component getDescription() {
            return Component.empty();
        }

        @Override
        public ClientData clientData() {
            Player player = DragonSurvival.PROXY.getLocalPlayer();
            ResourceLocation icon = player != null ? FlightData.getData(player).icon : DEFAULT_ICON;
            return new ClientData(icon, Component.translatable(Translation.Type.GUI.wrap(NAME)), Component.empty());
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
    private static final String SWIM_SPIN_FLUID = "swim_spin_fluid";
    private static final String ICON = "icon";
}
