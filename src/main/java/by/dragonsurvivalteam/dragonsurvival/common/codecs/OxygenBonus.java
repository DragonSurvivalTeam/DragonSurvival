package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncOxygenBonus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.OxygenBonuses;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

public record OxygenBonus(ResourceLocation id, Optional<ResourceKey<FluidType>> fluidType, LevelBasedValue oxygenBonus, LevelBasedValue duration, Optional<ResourceLocation> customIcon, boolean isHidden) {
    public static final Codec<OxygenBonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(OxygenBonus::id),
            ResourceKey.codec(NeoForgeRegistries.FLUID_TYPES.key()).optionalFieldOf("fluid_type").forGetter(OxygenBonus::fluidType),
            LevelBasedValue.CODEC.fieldOf("oxygen_bonus").forGetter(OxygenBonus::oxygenBonus),
            LevelBasedValue.CODEC.optionalFieldOf("duration", LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)).forGetter(OxygenBonus::duration),
            ResourceLocation.CODEC.optionalFieldOf("custom_icon").forGetter(OxygenBonus::customIcon),
            Codec.BOOL.optionalFieldOf("is_hidden", false).forGetter(OxygenBonus::isHidden)
    ).apply(instance, OxygenBonus::new));

    public static float NO_BONUS_VALUE = 0.f;
    public static float INFINITE_VALUE = -1.f;

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final LivingEntity target) {
        int newDuration = (int) duration.calculate(ability.level());

        OxygenBonuses data = target.getData(DSDataAttachments.OXYGEN_BONUSES);
        OxygenBonus.Instance instance = data.get(id);

        if (instance != null && instance.appliedAbilityLevel() == ability.level() && instance.currentDuration() == newDuration) {
            return;
        }

        data.remove(target, instance);
        data.add(target, new OxygenBonus.Instance(this, ClientEffectProvider.ClientData.from(dragon, ability, customIcon), ability.level(), newDuration));
    }

    public void remove(final LivingEntity target) {
        OxygenBonuses data = target.getData(DSDataAttachments.OXYGEN_BONUSES);
        data.remove(target, data.get(id));
    }

    public MutableComponent getDescription(final int abilityLevel) {
        MutableComponent description;
        MutableComponent fluids = fluidType().map(ResourceKey::location).map(Translation.Type.FLUID::wrap).map(Component::translatable).orElse(Component.translatable(LangKey.ABILITY_ALL_FLUIDS));
        float bonus = oxygenBonus.calculate(abilityLevel);

        if(bonus == INFINITE_VALUE) {
            description = Component.translatable(LangKey.ABILITY_BREATHE_INDEFINITELY, fluids);
        } else {
            description = Component.translatable(LangKey.ABILITY_BREATHE, fluids, (int) bonus);
        }

        if(duration().calculate(abilityLevel) != DurationInstance.INFINITE_DURATION) {
            description.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, (int) duration().calculate(abilityLevel)));
        }

        return description;
    }

    public static class Instance extends DurationInstance<OxygenBonus> {
        public static final Codec<OxygenBonus.Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(instance, () -> OxygenBonus.CODEC).apply(instance, OxygenBonus.Instance::new));

        public Instance(final OxygenBonus baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration) {
            super(baseData, clientData, appliedAbilityLevel, currentDuration);
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable OxygenBonus.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
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

        public float getOxygenBonus(final ResourceKey<FluidType> fluidTypeResourceKey) {
            if (baseData().fluidType().isPresent() && !baseData().fluidType().get().equals(fluidTypeResourceKey)) {
                return NO_BONUS_VALUE;
            }

            return baseData().oxygenBonus().calculate(appliedAbilityLevel());
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
        public boolean isHidden() {
            return baseData().isHidden();
        }
    }
}
