package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.flight.FlightStatus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public record FlightEffect(int flightLevel) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Can use flight")
    private static final String FLIGHT = Translation.Type.GUI.wrap("flight_effect.flight");

    public static final MapCodec<FlightEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("flight_level").forGetter(FlightEffect::flightLevel)
    ).apply(instance, FlightEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ServerPlayer serverTarget) || !DragonStateProvider.isDragon(serverTarget)) {
            return;
        }

        FlightData data = FlightData.getData(serverTarget);
        boolean hadFlight = data.hasFlight;
        data.hasFlight = ability.level() >= flightLevel;

        if (hadFlight != data.hasFlight) {
             PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverTarget, new FlightStatus(serverTarget.getId(), data.hasFlight));
        }
    }

    @Override
    @SuppressWarnings("ConstantValue") // ignore
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ServerPlayer serverTarget) || !DragonStateProvider.isDragon(serverTarget)) {
            return;
        }

        FlightData data = FlightData.getData(serverTarget);
        boolean hadFlight = data.hasFlight;
        data.hasFlight = false;

        if (hadFlight != data.hasFlight) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverTarget, new FlightStatus(serverTarget.getId(), data.hasFlight));
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        if (ability.level() >= flightLevel) {
            components.add(Component.translatable(FLIGHT));
        }

        return components;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
