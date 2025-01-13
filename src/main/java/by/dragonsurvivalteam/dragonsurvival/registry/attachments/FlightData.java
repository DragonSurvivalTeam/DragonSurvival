package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import net.minecraft.client.Minecraft;
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

import java.util.Optional;

public class FlightData implements INBTSerializable<CompoundTag> {
    private static final String COOLDOWN = "cooldown";
    private static final String DURATION = "duration";
    private static final String FLIGHT = "flight";
    private static final String SPIN = "spin";
    private static final String ARE_WINGS_SPREAD = "are_wings_spread";
    private static final String SWIM_SPIN_FLUID = "swim_spin_fluid";


    // Data that actually needs to be saved
    public boolean areWingsSpread;
    public Optional<Holder<FluidType>> swimSpinFluid = Optional.empty();
    public int cooldown;
    public int duration;
    public boolean hasFlight;
    public boolean hasSpin;

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
        swimSpinFluid.ifPresent(fluidTypeHolder -> tag.putString(SWIM_SPIN_FLUID, fluidTypeHolder.getKey().location().toString()));
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        areWingsSpread = tag.getBoolean(ARE_WINGS_SPREAD);
        hasFlight = tag.getBoolean(FLIGHT);
        hasSpin = tag.getBoolean(SPIN);
        cooldown = tag.getInt(COOLDOWN);
        duration = tag.getInt(DURATION);
        if (tag.contains(SWIM_SPIN_FLUID)) {
            Optional<Holder.Reference<FluidType>> fluidType = provider.holder(ResourceKey.create(NeoForgeRegistries.FLUID_TYPES.key(), ResourceLocation.parse(tag.getString(SWIM_SPIN_FLUID))));

            if (fluidType.isPresent()) {
                swimSpinFluid = Optional.of(fluidType.get());
            } else {
                DragonSurvival.LOGGER.warn("Fluid type not found for key: [{}] in FlightData", tag.getString(SWIM_SPIN_FLUID));
            }
        }
    }

    public AttachmentType<?> type() {
        return DSDataAttachments.SPIN.get();
    }

    public void sync(final ServerPlayer player) {
        player.getExistingData(type()).ifPresent(data -> PacketDistributor.sendToPlayer(player, new SyncData(player.getId(), NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type()), serializeNBT(player.registryAccess()))));
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
            DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
            ResourceLocation icon = handler.isDragon() && handler.species().value().miscResources().wingIcon().isPresent() ? handler.species().value().miscResources().wingIcon().get() : DragonSurvival.res("textures/ability_effect/dragon_wings.png");
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
}
