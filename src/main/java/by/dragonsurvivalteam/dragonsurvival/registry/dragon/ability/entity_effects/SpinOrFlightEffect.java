package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.network.flight.SpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.FlightStatus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
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

public record SpinOrFlightEffect(int flightLevel, int spinLevel, Holder<FluidType> swimSpinFluid) implements AbilityEntityEffect {
    public static final MapCodec<SpinOrFlightEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("flight_level").forGetter(SpinOrFlightEffect::flightLevel),
            Codec.INT.fieldOf("spin_level").forGetter(SpinOrFlightEffect::spinLevel),
            NeoForgeRegistries.FLUID_TYPES.holderByNameCodec().fieldOf("swim_spin_fluid").forGetter(SpinOrFlightEffect::swimSpinFluid)
    ).apply(instance, SpinOrFlightEffect::new));

    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, Entity entity) {
        if(dragon != entity) {
            throw new IllegalArgumentException("The entity must be the same as the dragon for SpinOrFlightEffect.");
        }

        FlightData data = FlightData.getData(dragon);
        boolean prevFlight = data.hasFlight;
        data.hasFlight = ability.level() >= flightLevel;
        if(prevFlight != data.hasFlight) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(dragon, new FlightStatus(dragon.getId(), data.hasFlight));
        }

        boolean prevSpin = data.hasSpin;
        if(ability.level() >= spinLevel) {
            data.hasSpin = true;
            data.swimSpinFluid = swimSpinFluid;
        } else {
            data.hasSpin = false;
            data.swimSpinFluid = null;
        }

        if(prevSpin != data.hasSpin) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(dragon, new SpinStatus(dragon.getId(), data.hasSpin, data.swimSpinFluid.getKey()));
        }
    }

    @Override
    public boolean shouldAppendSelfTargetingToDescription() { return false; }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();
        if(ability.level() >= flightLevel) {
            components.add(Component.translatable(LangKey.ABILITY_FLIGHT));
        }

        if(ability.level() >= spinLevel) {
            components.add(Component.translatable(LangKey.ABILITY_SPIN));
        }

        return components;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
