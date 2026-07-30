package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncDamageModification;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
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
import net.minecraft.resources.RegistryOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedValue;
import by.dragonsurvivalteam.dragonsurvival.common.compat.attachments.AttachmentType;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Optional;

// We store the damage type as optional instead considering an empty holder set as "all damage types"
// Since the holderset may be empty due to specified damage types / tags not being present (optional ones)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DamageModification extends DurationInstanceBase<DamageModifications, DamageModification.Instance> {
    @Translation(comments = "§6■ Immune§r to %s")
    private static final String ABILITY_IMMUNITY = Translation.Type.GUI.wrap("damage_modification.immunity");

    @Translation(comments = "§6■ %s reduced damage taken§r from %s")
    private static final String ABILITY_DAMAGE_REDUCTION = Translation.Type.GUI.wrap("damage_modification.damage_reduction");

    @Translation(comments = "§6■ %s increased damage taken§r from %s")
    private static final String ABILITY_DAMAGE_INCREASE = Translation.Type.GUI.wrap("damage_modification.damage_increase");

    @Translation(comments = "All Sources")
    public static final String ALL = Translation.Type.GUI.wrap("damage_modification.all");

    public static final Codec<DamageModification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("damage_types").forGetter(DamageModification::damageTypes),
            LevelBasedValue.CODEC.fieldOf("multiplier").forGetter(DamageModification::multiplier)
    ).apply(instance, DamageModification::new));

    private final Optional<HolderSet<DamageType>> damageTypes;
    private final LevelBasedValue multiplier;

    public DamageModification(final DurationInstanceBase<?, ?> base, final HolderSet<DamageType> damageTypes, final LevelBasedValue multiplier) {
        this(base, Optional.ofNullable(damageTypes), multiplier);
    }

    public DamageModification(final DurationInstanceBase<?, ?> base, final Optional<HolderSet<DamageType>> damageTypes, final LevelBasedValue multiplier) {
        super(base);
        this.damageTypes = damageTypes;
        this.multiplier = multiplier;
    }

    public boolean isFireImmune(int appliedAbilityLevel) {
        if (multiplier.calculate(appliedAbilityLevel) != 0) {
            return false;
        }

        if (damageTypes.map(types -> types instanceof HolderSet.Named<DamageType> named && named.key() == DamageTypeTags.IS_FIRE).orElse(true)) {
            return true;
        }

        for (Holder<DamageType> damageType : damageTypes.orElse(HolderSet.empty())) {
            if (damageType.is(DamageTypes.ON_FIRE) || damageType.is(DamageTypes.IN_FIRE) || damageType.is(DamageTypes.LAVA)) {
                return true;
            }
        }

        return false;
    }

    public MutableComponent getDescription(final int abilityLevel) {
        float amount = multiplier.calculate(abilityLevel);
        String difference = NumberFormat.getPercentInstance().format(Math.abs(amount - 1));

        MutableComponent damageType = damageTypes.map(
                types -> Functions.translateHolderSet(types, Translation.Type.DAMAGE_TYPE)
        ).orElse(/* 'translateHolderSet' handles the coloring for the other component */ DSColors.dynamicValue(Component.translatable(ALL)));

        MutableComponent description;

        if (amount == 0) {
            description = Component.translatable(ABILITY_IMMUNITY, damageType);
        } else if (amount < 1) {
            description = Component.translatable(ABILITY_DAMAGE_REDUCTION, DSColors.dynamicValue(difference), damageType);
        } else {
            description = Component.translatable(ABILITY_DAMAGE_INCREASE, DSColors.dynamicValue(difference), damageType);
        }

        float duration = duration().calculate(abilityLevel);

        if (duration != DurationInstance.INFINITE_DURATION) {
            description.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.dynamicValue(Functions.ticksToSeconds((int) duration))));
        }

        return description;
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration);
    }

    @Override
    public AttachmentType<DamageModifications> type() {
        return DSDataAttachments.DAMAGE_MODIFICATIONS.get();
    }

    public Optional<HolderSet<DamageType>> damageTypes() {
        return damageTypes;
    }

    public LevelBasedValue multiplier() {
        return multiplier;
    }

    public static class Instance extends DurationInstance<DamageModification> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(
                instance, () -> DamageModification.CODEC).apply(instance, Instance::new)
        );

        public Instance(final DamageModification baseData, final CommonData commonData, int currentDuration) {
            super(baseData, commonData, currentDuration);
        }

        public float calculate(final Holder<DamageType> damageType, float damageAmount) {
            float modification = 1;

            if (baseData().damageTypes().map(types -> types.contains(damageType)).orElse(true)) {
                modification = Math.max(0, baseData().multiplier().calculate(appliedAbilityLevel()));
            }

            return damageAmount * modification;
        }

        public boolean isFireImmune() {
            return baseData().isFireImmune(appliedAbilityLevel());
        }

        @Override
        public Component getDescription() {
            return baseData().getDescription(appliedAbilityLevel());
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncDamageModification(player.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncDamageModification(player.getId(), this, true));
            }
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, provider), this).getOrThrow(false, DragonSurvival.LOGGER::error);
        }

        public static @Nullable Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, provider), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }
}
