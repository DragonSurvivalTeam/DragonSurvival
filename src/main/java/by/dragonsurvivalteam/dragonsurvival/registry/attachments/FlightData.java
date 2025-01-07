package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FlightData implements INBTSerializable<CompoundTag> {
    public static final String COOLDOWN = "cooldown";
    public static final String DURATION = "duration";

    public Optional<Holder<FluidType>> swimSpinFluid = Optional.empty();
    public boolean hasFlight;
    public boolean hasSpin;
    public boolean areWingsSpread;

    // Data that actually needs to be saved
    public int cooldown;
    public int duration;

    public static FlightData getData(final Player player) {
        return player.getData(DSDataAttachments.SPIN);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(COOLDOWN, cooldown);
        tag.putInt(DURATION, duration);
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        cooldown = tag.getInt(COOLDOWN);
        duration = tag.getInt(DURATION);
    }

    public boolean hasFlight() {
        return hasFlight;
    }

    public boolean isWingsSpread() {
        return hasFlight && areWingsSpread;
    }

    public static final ClientEffectProvider FLIGHT_EFFECT = new ClientEffectProvider() {
        @Translation(type = Translation.Type.GUI, comments = "Wings")
        private static final ResourceLocation MODIFIER = DragonSurvival.res("wings");

        private static final ClientData DATA = new ClientData(DragonSurvival.res("textures/modifiers/cave_dragon_wings.png"), Component.translatable(Translation.Type.GUI.wrap(MODIFIER)), Component.empty());

        @Override
        public Component getDescription() {
            return Component.empty();
        }

        @Override
        public ClientData clientData() {
            return DATA;
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
