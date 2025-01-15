package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncOxygenBonus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.OxygenBonuses;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
public class OxygenBonus extends DurationInstanceBase<OxygenBonuses, OxygenBonus.Instance> {
    @Translation(comments = "Can breathe in %s for %s additional seconds")
    private static final String BONUS = Translation.Type.GUI.wrap("oxygen_bonus.bonus");

    @Translation(comments = "Can breathe in %s indefinitely")
    private static final String UNLIMITED = Translation.Type.GUI.wrap("oxygen_bonus.unlimited");

    @Translation(comments = "all fluids")
    private static final String ALL_FLUIDS = Translation.Type.GUI.wrap("oxygen_bonus.all_fluids");

    @Translation(comments = "No fluid exists to which a bonus can be applied")
    private static final String NO_FLUID = Translation.Type.GUI.wrap("oxygen_bonus.no_fluid");

    public static float NONE;

    public static final Codec<OxygenBonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            RegistryCodecs.homogeneousList(NeoForgeRegistries.Keys.FLUID_TYPES).optionalFieldOf("fluids").forGetter(OxygenBonus::fluids),
            LevelBasedValue.CODEC.fieldOf("oxygen_bonus").forGetter(OxygenBonus::oxygenBonus)
    ).apply(instance, OxygenBonus::new));

    private final Optional<HolderSet<FluidType>> fluids;
    private final LevelBasedValue oxygenBonus;

    public OxygenBonus(final DurationInstanceBase<?, ?> base, final Optional<HolderSet<FluidType>> fluids, final LevelBasedValue oxygenBonus) {
        super(base);
        this.fluids = fluids;
        this.oxygenBonus = oxygenBonus;
    }

    public MutableComponent getDescription(final int abilityLevel) {
        float bonus = oxygenBonus.calculate(abilityLevel);
        MutableComponent description;

        MutableComponent fluids = null;

        if (fluids().isEmpty()) {
            fluids = DSColors.dynamicValue(Component.translatable(ALL_FLUIDS));
        } else {
            for (Holder<FluidType> fluid : fluids().get()) {
                MutableComponent name = DSColors.dynamicValue(Component.translatable(fluid.value().getDescriptionId()));

                if (fluids == null) {
                    fluids = name;
                } else {
                    fluids.append(Component.literal(", ").append(name));
                }
            }
        }

        if (fluids == null) {
            return Component.translatable(NO_FLUID);
        }

        if (bonus == SwimData.UNLIMITED_OXYGEN) {
            description = Component.translatable(UNLIMITED, fluids);
        } else {
            description = Component.translatable(BONUS, fluids, (int) bonus);
        }

        if (duration().calculate(abilityLevel) != DurationInstance.INFINITE_DURATION) {
            description.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, (int) duration().calculate(abilityLevel)));
        }

        return description;
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(dragon, ability, customIcon()), currentDuration);
    }

    @Override
    public AttachmentType<OxygenBonuses> type() {
        return DSDataAttachments.OXYGEN_BONUSES.value();
    }

    public Optional<HolderSet<FluidType>> fluids() {
        return fluids;
    }

    public LevelBasedValue oxygenBonus() {
        return oxygenBonus;
    }

    public static class Instance extends DurationInstance<OxygenBonus> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(
                instance, () -> OxygenBonus.CODEC).apply(instance, Instance::new)
        );

        public Instance(final OxygenBonus baseData, final CommonData commonData, int currentDuration) {
            super(baseData, commonData, currentDuration);
        }

        public float getOxygenBonus(final Holder<FluidType> fluid) {
            if (!baseData().fluids().map(set -> set.contains(fluid)).orElse(true)) {
                return OxygenBonus.NONE;
            }

            return baseData().oxygenBonus().calculate(appliedAbilityLevel());
        }

        @Override
        public Component getDescription() {
            return baseData().getDescription(appliedAbilityLevel());
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncOxygenBonus(player.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncOxygenBonus(player.getId(), this, true));
            }
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable OxygenBonus.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }
}
