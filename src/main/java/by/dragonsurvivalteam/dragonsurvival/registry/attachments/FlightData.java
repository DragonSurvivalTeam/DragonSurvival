package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FlightData implements ValueIOSerializable {
    public static final Identifier DEFAULT_ICON = DragonSurvival.res("textures/ability_effect/generic_icons/dragon_wings.png");

    public Identifier icon = DEFAULT_ICON;
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
    public void serialize(@NotNull final ValueOutput valueOutput) {
        valueOutput.putBoolean(ARE_WINGS_SPREAD, areWingsSpread);
        valueOutput.putBoolean(FLIGHT, hasFlight);
        valueOutput.putBoolean(SPIN, hasSpin);
        valueOutput.putInt(COOLDOWN, cooldown);
        valueOutput.putInt(DURATION, duration);

        if (inFluid != null) {
            valueOutput.store(IN_FLUID, RegistryCodecs.homogeneousList(NeoForgeRegistries.Keys.FLUID_TYPES), inFluid);
        }

        valueOutput.putString(ICON, icon.toString());
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        areWingsSpread = valueInput.getBooleanOr(ARE_WINGS_SPREAD, false);
        hasFlight = valueInput.getBooleanOr(FLIGHT, false);
        hasSpin = valueInput.getBooleanOr(SPIN, false);
        cooldown = valueInput.getInt(COOLDOWN).orElseThrow();
        duration = valueInput.getInt(DURATION).orElseThrow();

        if (valueInput.keySet().contains(IN_FLUID)) {
            inFluid = valueInput.read(IN_FLUID, RegistryCodecs.homogeneousList(NeoForgeRegistries.Keys.FLUID_TYPES)).orElseThrow();
        } else {
            inFluid = null;
        }

        icon = Objects.requireNonNullElse(Identifier.tryParse(valueInput.getString(ICON).orElseThrow()), DEFAULT_ICON);
    }

    public void sync(final ServerPlayer player) {
        TagValueOutput tagValueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
        serialize(tagValueOutput);

        PacketDistributor.sendToPlayer(player, new SyncData(player.getId(), DSDataAttachments.FLIGHT.getId(), tagValueOutput.buildResult()));
    }

    // Needed for when a player enters tracking range, as the flight data has a visual impact (whether the wings are spread or not)
    public void sync(final ServerPlayer source, final ServerPlayer target) {
        TagValueOutput tagValueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, source.registryAccess());
        serialize(tagValueOutput);

        PacketDistributor.sendToPlayer(target, new SyncData(source.getId(), DSDataAttachments.FLIGHT.getId(), tagValueOutput.buildResult()));
    }

    public boolean hasFlight() {
        return hasFlight;
    }

    public boolean isWingsSpread() {
        return hasFlight && areWingsSpread;
    }

    public static final ClientEffectProvider FLIGHT_EFFECT = new ClientEffectProvider() {
        @Translation(type = Translation.Type.GUI, comments = "Wings")
        private static final Identifier NAME = DragonSurvival.res("dragon_wings");

        private static final Identifier ID = DragonSurvival.res("dragon_wings");
        private static final Component TRANSLATED_NAME = Component.translatable(Translation.Type.GUI.wrap(NAME));

        @Override
        public Component getDescription() {
            return Component.empty();
        }

        @Override
        public ClientData clientData() {
            Player player = DragonSurvival.PROXY.getLocalPlayer();
            Identifier icon = player != null ? FlightData.getData(player).icon : DEFAULT_ICON;
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
