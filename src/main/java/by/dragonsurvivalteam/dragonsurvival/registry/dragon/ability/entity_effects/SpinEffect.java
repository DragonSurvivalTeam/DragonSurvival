package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SpinStatus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.IHolderExtension;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record SpinEffect(int spinLevel, Optional<Holder<FluidType>> swimSpinFluid) implements AbilityEntityEffect {
    public static final MapCodec<SpinEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("spin_level").forGetter(SpinEffect::spinLevel),
            NeoForgeRegistries.FLUID_TYPES.holderByNameCodec().optionalFieldOf("swim_spin_fluid").forGetter(SpinEffect::swimSpinFluid)
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
            data.swimSpinFluid = swimSpinFluid;
        } else {
            data.hasSpin = false;
        }

        if (hadSpin != data.hasSpin) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverTarget, new SpinStatus(serverTarget.getId(), data.hasSpin, data.swimSpinFluid.map(IHolderExtension::getKey)));
        }
    }

    @Override
    @SuppressWarnings("ConstantValue") // ignore
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ServerPlayer serverTarget) || !DragonStateProvider.isDragon(serverTarget)) {
            return;
        }

        FlightData data = FlightData.getData(serverTarget);
        boolean hadSpin = data.hasSpin;
        data.hasSpin = false;

        if (hadSpin != data.hasSpin) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverTarget, new SpinStatus(serverTarget.getId(), data.hasSpin, data.swimSpinFluid.map(IHolderExtension::getKey)));
        }
    }

    @Override
    public boolean shouldAppendSelfTargetingToDescription() {
        return false;
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();
        
        if (ability.level() >= spinLevel) {
            components.add(Component.translatable(LangKey.ABILITY_SPIN));
        }

        return components;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
