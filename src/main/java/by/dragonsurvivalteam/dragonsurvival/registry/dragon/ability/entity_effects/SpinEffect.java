package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// TODO :: does this need some 'remove_automatically' handling as well?
public record SpinEffect(int spinLevel, Optional<HolderSet<FluidType>> inFluid) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Can use spin")
    private static final String SPIN = Translation.Type.GUI.wrap("spin_effect.spin");

    public static final MapCodec<SpinEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("spin_level").forGetter(SpinEffect::spinLevel),
            RegistryCodecs.homogeneousList(NeoForgeRegistries.Keys.FLUID_TYPES).optionalFieldOf("in_fluid").forGetter(SpinEffect::inFluid)
    ).apply(instance, SpinEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ServerPlayer serverTarget) || !DragonStateProvider.isDragon(serverTarget)) {
            return;
        }

        FlightData data = FlightData.getData(serverTarget);
        boolean hadSpin = data.hasSpin;

        if (ability.level() >= spinLevel) {
            data.hasSpin = true;
            data.inFluid = inFluid.orElse(null);
        } else {
            data.hasSpin = false;
        }

        if (hadSpin != data.hasSpin) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverTarget, new SyncSpinStatus(serverTarget.getId(), data.hasSpin, Optional.ofNullable(data.inFluid)));
        }
    }

    @Override
    @SuppressWarnings("ConstantValue") // ignore
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target, final boolean isAutoRemoval) {
        if (isAutoRemoval) {
            return;
        }

        if (!(target instanceof ServerPlayer serverTarget) || !DragonStateProvider.isDragon(serverTarget)) {
            return;
        }

        FlightData data = FlightData.getData(serverTarget);
        boolean hadSpin = data.hasSpin;
        data.hasSpin = false;

        if (hadSpin != data.hasSpin) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverTarget, new SyncSpinStatus(serverTarget.getId(), data.hasSpin, Optional.ofNullable(data.inFluid)));
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();
        
        if (ability.level() >= spinLevel) {
            components.add(Component.translatable(SPIN));
        }

        return components;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
