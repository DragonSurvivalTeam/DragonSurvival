package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncEffectModification;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import javax.annotation.Nullable;

public record EffectModification(ResourceLocation id, HolderSet<MobEffect> effects, Modification durationModification, Modification amplifierModification, LevelBasedValue duration) {
    @Translation(comments = {
            "§6■ Effect modifications:§r",
            "- Duration %s",
            "- Amplifier %s",
            "\nAffected effects:\n%s"
    })
    private static final String EFFECT_MODIFICATIONS = Translation.Type.ABILITY.wrap("general.effect_modifications");

    @Translation(comments = "increased by %s")
    private static final String INCREASED = Translation.Type.ABILITY.wrap("general.effect_modifications.increased");

    @Translation(comments = "reduced by %s")
    private static final String REDUCED = Translation.Type.ABILITY.wrap("general.effect_modifications.reduced");

    @Translation(comments = "is unmodified")
    private static final String UNMODIFIED = Translation.Type.ABILITY.wrap("general.effect_modifications.unmodified");

    @Translation(comments = " %s seconds") // TODO :: make it generic (for global use)?
    private static final String SECONDS = Translation.Type.ABILITY.wrap("general.effect_modifications.seconds");

    public static final Codec<EffectModification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(EffectModification::id),
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(EffectModification::effects),
            Modification.CODEC.fieldOf("duration_modification").forGetter(EffectModification::durationModification),
            Modification.CODEC.fieldOf("amplifier_modification").forGetter(EffectModification::amplifierModification),
            LevelBasedValue.CODEC.optionalFieldOf("duration", LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)).forGetter(EffectModification::duration)
    ).apply(instance, EffectModification::new));

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final LivingEntity target) {
        int newDuration = (int) duration.calculate(ability.level());

        EffectModifications data = target.getData(DSDataAttachments.EFFECT_MODIFICATIONS);
        Instance instance = data.get(id);

        if (instance != null && instance.appliedAbilityLevel() == ability.level() && instance.currentDuration() == newDuration) {
            return;
        }

        data.remove(target, instance);
        data.add(target, new Instance(this, ClientEffectProvider.ClientData.from(dragon, ability, id, Component.empty()), ability.level(), newDuration));
    }

    public void remove(final LivingEntity target) {
        EffectModifications data = target.getData(DSDataAttachments.EFFECT_MODIFICATIONS);
        data.remove(target, data.get(id));
    }

    public @NotNull MutableComponent getDescription(int abilityLevel) {
        Component duration = getModificationDescription(durationModification, abilityLevel, true);
        Component amplifier = getModificationDescription(amplifierModification, abilityLevel, false);

        MutableComponent effects = null;

        for (Holder<MobEffect> effect : this.effects) {
            Component effectDescription = DSColors.dynamicValue(Component.translatable(effect.value().getDescriptionId()));

            if (effects == null) {
                effects = Component.literal("- ").append(effectDescription);
            } else {
                effects.append(Component.literal("\n- ").append(effectDescription));
            }
        }

        if (effects == null) {
            effects = Component.empty();
        } else {
            effects.append(Component.literal("\n"));
        }

        return Component.translatable(EFFECT_MODIFICATIONS, duration, amplifier, effects);
    }

    private Component getModificationDescription(final Modification modification, final int abilityLevel, boolean isTime) {
        float calculated = modification.amount().calculate(abilityLevel);

        if (modification.type() == Modification.ModificationType.MULTIPLICATIVE) {
            String difference = NumberFormat.getPercentInstance().format(Math.abs(calculated - 1));

            if (calculated == 1) {
                return Component.translatable(UNMODIFIED);
            } else if (calculated < 1) {
                return Component.translatable(REDUCED, DSColors.dynamicValue(difference));
            } else {
                return Component.translatable(INCREASED, DSColors.dynamicValue(difference));
            }
        }

        int value = Math.abs((int) calculated);

        if (calculated == 0) {
            return Component.translatable(UNMODIFIED);
        }

        Component component = isTime ? Component.translatable(SECONDS, DSColors.dynamicValue(Functions.ticksToSeconds(value))) : DSColors.dynamicValue(value);

        if (calculated < 0) {
            return Component.translatable(REDUCED, component);
        } else {
            return Component.translatable(INCREASED, component);
        }
    }

    public static class Instance extends DurationInstance<EffectModification> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(instance, () -> EffectModification.CODEC).apply(instance, Instance::new));

        public Instance(final EffectModification baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration) {
            super(baseData, clientData, appliedAbilityLevel, currentDuration);
        }

        public int calculateDuration(final int duration) {
            return switch (baseData().durationModification().type()) {
                case ADDITIVE -> duration + (int) baseData().durationModification().amount().calculate(appliedAbilityLevel());
                case MULTIPLICATIVE -> (int) (duration * baseData().durationModification().amount().calculate(appliedAbilityLevel()));
            };
        }

        public int calculateAmplifier(final int amplifier) {
            return switch (baseData().amplifierModification().type()) {
                case ADDITIVE -> amplifier + (int) baseData().amplifierModification().amount().calculate(appliedAbilityLevel());
                case MULTIPLICATIVE -> (int) (amplifier * baseData().amplifierModification().amount().calculate(appliedAbilityLevel()));
            };
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncEffectModification(player.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncEffectModification(player.getId(), this, true));
            }
        }

        @Override
        public int getDuration() {
            return (int) baseData().duration().calculate(appliedAbilityLevel());
        }

        @Override
        public ResourceLocation id() {
            return baseData().id();
        }
    }
}
