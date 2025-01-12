package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncDamageModification;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.Optional;

public record DamageModification(ResourceLocation id, HolderSet<DamageType> damageTypes, LevelBasedValue multiplier, LevelBasedValue duration, Optional<ResourceLocation> customIcon, boolean isHidden) {
    @Translation(comments = "§6■ Immune§r to ")
    private static final String ABILITY_IMMUNITY = Translation.Type.GUI.wrap("damage_modification.immunity");

    @Translation(comments = "§6■ %s% §r reduced damage taken from ")
    private static final String ABILITY_DAMAGE_REDUCTION = Translation.Type.GUI.wrap("damage_modification.damage_reduction");

    @Translation(comments = "§6■ %s% §r increased damage taken from ")
    private static final String ABILITY_DAMAGE_INCREASE = Translation.Type.GUI.wrap("damage_modification.damage_increase");

    public static final Codec<DamageModification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(DamageModification::id),
            RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).fieldOf("types").forGetter(DamageModification::damageTypes),
            LevelBasedValue.CODEC.fieldOf("multiplier").forGetter(DamageModification::multiplier),
            LevelBasedValue.CODEC.optionalFieldOf("duration", LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)).forGetter(DamageModification::duration),
            ResourceLocation.CODEC.optionalFieldOf("custom_icon").forGetter(DamageModification::customIcon),
            Codec.BOOL.optionalFieldOf("is_hidden", false).forGetter(DamageModification::isHidden)
    ).apply(instance, DamageModification::new));

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        int newDuration = (int) duration.calculate(ability.level());

        DamageModifications data = entity.getData(DSDataAttachments.DAMAGE_MODIFICATIONS);
        Instance instance = data.get(id);

        if (instance != null && instance.appliedAbilityLevel() == ability.level() && instance.currentDuration() == newDuration) {
            return;
        }

        data.remove(entity, instance);
        data.add(entity, new Instance(this, ClientEffectProvider.ClientData.from(dragon, ability, customIcon), ability.level(), newDuration));
    }

    public void remove(final Entity target) {
        DamageModifications data = target.getData(DSDataAttachments.DAMAGE_MODIFICATIONS);
        data.remove(target, data.get(id));
    }

    public boolean isFireImmune(int appliedAbilityLevel) {
        if (multiplier.calculate(appliedAbilityLevel) != 0) {
            return false;
        }

        if (damageTypes instanceof HolderSet.Named<DamageType> named && named.key() == DamageTypeTags.IS_FIRE) {
            return true;
        }

        for (Holder<DamageType> damageType : damageTypes) {
            if (damageType.is(DamageTypes.ON_FIRE) || damageType.is(DamageTypes.IN_FIRE) || damageType.is(DamageTypes.LAVA)) {
                return true;
            }
        }

        return false;
    }

    public MutableComponent getDescription(int abilityLevel) {
        float amount = multiplier.calculate(abilityLevel);
        String difference = NumberFormat.getPercentInstance().format(Math.abs(amount - 1));

        MutableComponent name;

        if (amount == 0) {
            name = Component.translatable(ABILITY_IMMUNITY);
        } else if (amount < 1) {
            name = Component.translatable(ABILITY_DAMAGE_REDUCTION, DSColors.dynamicValue(difference));
        } else {
            name = Component.translatable(ABILITY_DAMAGE_INCREASE, DSColors.dynamicValue(difference));
        }

        if (damageTypes instanceof HolderSet.Named<DamageType> named) {
            name.append(DSColors.dynamicValue(Component.translatable(Tags.getTagTranslationKey(named.key()))));
        } else {
            int count = 0;

            for (Holder<DamageType> damageType : damageTypes) {
                //noinspection DataFlowIssue -> key is present
                name.append(DSColors.dynamicValue(Component.translatable(Translation.Type.DAMAGE_TYPE.wrap(damageType.getKey().location()))));

                if (count < damageTypes.size() - 1) {
                    name.append(", ");
                }

                count++;
            }
        }

        float duration = duration().calculate(abilityLevel);

        if (duration != DurationInstance.INFINITE_DURATION) {
            name.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.dynamicValue(Functions.ticksToSeconds((int) duration))));
        }

        return name;
    }

    public static class Instance extends DurationInstance<DamageModification> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(instance, () -> DamageModification.CODEC).apply(instance, Instance::new));

        public Instance(final DamageModification baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration) {
            super(baseData, clientData, appliedAbilityLevel, currentDuration);
        }

        public float calculate(final Holder<DamageType> damageType, float damageAmount) {
            float modification = 1;

            if (baseData().damageTypes().contains(damageType)) {
                modification = Math.max(0, baseData().multiplier().calculate(appliedAbilityLevel()));
            }

            return damageAmount * modification;
        }

        public boolean isFireImmune() {
            return baseData().isFireImmune(appliedAbilityLevel());
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
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

        @Override
        public ResourceLocation id() {
            return baseData().id();
        }

        @Override
        public int getDuration() {
            return (int) baseData().duration().calculate(appliedAbilityLevel());
        }

        @Override
        public boolean isInvisible() {
            return baseData().isHidden();
        }
    }
}
