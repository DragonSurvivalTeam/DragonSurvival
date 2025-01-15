package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncEffectModification;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;

public class EffectModification extends DurationInstanceBase<EffectModifications, EffectModification.Instance> {
    @Translation(comments = {
            "§6■ Effect modifications:§r",
            " - Duration %s",
            " - Amplifier %s",
            "\nAffected effects:"
    })
    private static final String EFFECT_MODIFICATIONS = Translation.Type.GUI.wrap("effect_modification");

    @Translation(comments = "increased by %s")
    private static final String INCREASED = Translation.Type.GUI.wrap("effect_modification.increased");

    @Translation(comments = "reduced by %s")
    private static final String REDUCED = Translation.Type.GUI.wrap("effect_modification.reduced");

    @Translation(comments = "is unmodified")
    private static final String UNMODIFIED = Translation.Type.GUI.wrap("effect_modification.unmodified");

    public static final Codec<EffectModification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(EffectModification::effects),
            Modification.CODEC.fieldOf("duration_modification").forGetter(EffectModification::durationModification),
            Modification.CODEC.fieldOf("amplifier_modification").forGetter(EffectModification::amplifierModification)
    ).apply(instance, EffectModification::new));

    private final HolderSet<MobEffect> effects;
    private final Modification durationModification;
    private final Modification amplifierModification;

    public EffectModification(final DurationInstanceBase<?, ?> base, final HolderSet<MobEffect> effects, final Modification durationModification, final Modification amplifierModification) {
        super(base);
        this.effects = effects;
        this.durationModification = durationModification;
        this.amplifierModification = amplifierModification;
    }

    public MutableComponent getDescription(int abilityLevel) {
        Component duration = getModificationDescription(durationModification, abilityLevel, true);
        Component amplifier = getModificationDescription(amplifierModification, abilityLevel, false);
        MutableComponent description = Component.translatable(EFFECT_MODIFICATIONS, duration, amplifier);

        for (Holder<MobEffect> effect : this.effects) {
            description.append(Component.literal("\n- ").append(DSColors.dynamicValue(Component.translatable(effect.value().getDescriptionId()))));
        }

        return description;
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

        Component component = isTime ? Component.translatable(LangKey.SECONDS, DSColors.dynamicValue(Functions.ticksToSeconds(value))) : DSColors.dynamicValue(value);

        if (calculated < 0) {
            return Component.translatable(REDUCED, component);
        } else {
            return Component.translatable(INCREASED, component);
        }
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration);
    }

    @Override
    public AttachmentType<EffectModifications> type() {
        return DSDataAttachments.EFFECT_MODIFICATIONS.value();
    }

    public HolderSet<MobEffect> effects() {
        return effects;
    }

    public Modification durationModification() {
        return durationModification;
    }

    public Modification amplifierModification() {
        return amplifierModification;
    }

    public static class Instance extends DurationInstance<EffectModification> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(
                instance, () -> EffectModification.CODEC).apply(instance, Instance::new)
        );

        public Instance(final EffectModification baseData, final CommonData commonData, int currentDuration) {
            super(baseData, commonData, currentDuration);
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

        @Override
        public Component getDescription() {
            return baseData().getDescription(appliedAbilityLevel());
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

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }
}
