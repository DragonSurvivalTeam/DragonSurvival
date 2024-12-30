package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncHarvestBonus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HarvestBonuses;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import javax.annotation.Nullable;

public record HarvestBonus(ResourceLocation id, Optional<HolderSet<Block>> blocks, LevelBasedValue harvestBonus, LevelBasedValue breakSpeedMultiplier, LevelBasedValue duration) {
    public static int NO_BONUS_VALUE = 0;
    public static final LevelBasedValue NO_BONUS = LevelBasedValue.constant(NO_BONUS_VALUE);

    public static final Codec<HarvestBonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(HarvestBonus::id),
            RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("blocks").forGetter(HarvestBonus::blocks),
            LevelBasedValue.CODEC.optionalFieldOf("harvest_bonus", NO_BONUS).forGetter(HarvestBonus::harvestBonus),
            LevelBasedValue.CODEC.optionalFieldOf("break_speed_multiplier", NO_BONUS).forGetter(HarvestBonus::breakSpeedMultiplier),
            LevelBasedValue.CODEC.optionalFieldOf("duration", LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)).forGetter(HarvestBonus::duration)
    ).apply(instance, HarvestBonus::new));

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final LivingEntity target) {
        int abilityLevel = ability.level();
        int newDuration = (int) duration().calculate(abilityLevel);

        HarvestBonuses data = target.getData(DSDataAttachments.HARVEST_BONUSES);
        Instance instance = data.get(id);

        if (instance != null && instance.currentDuration() == newDuration && instance.appliedAbilityLevel() == abilityLevel) {
            return;
        }

        if (instance != null) {
            data.remove(target, instance);
        }

        ClientEffectProvider.ClientData clientData = new ClientEffectProvider.ClientData(ability.getIcon(), /* TODO */ Component.empty(), Optional.of(dragon.getUUID()));
        instance = new Instance(this, clientData, abilityLevel, newDuration);
        data.add(target, instance);
    }

    public void remove(final LivingEntity target) {
        HarvestBonuses data = target.getData(DSDataAttachments.HARVEST_BONUSES);
        data.remove(target, data.get(id));
    }

    public static class Instance extends DurationInstance<HarvestBonus> {
        public static final Codec<HarvestBonus.Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(instance, () -> HarvestBonus.CODEC).apply(instance, Instance::new));

        public Instance(final HarvestBonus baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration) {
            super(baseData, clientData, appliedAbilityLevel, currentDuration);
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable HarvestBonus.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncHarvestBonus(player.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncHarvestBonus(player.getId(), this, true));
            }
        }

        public int getHarvestBonus(final BlockState state) {
            if (baseData().blocks().isPresent() && !baseData().blocks().get().contains(state.getBlockHolder())) {
                return NO_BONUS_VALUE;
            }

            return (int) baseData().harvestBonus().calculate(appliedAbilityLevel());
        }

        public float getSpeedMultiplier(final BlockState state) {
            if (baseData().blocks().isPresent() && !baseData().blocks().get().contains(state.getBlockHolder())) {
                return NO_BONUS_VALUE;
            }

            return baseData().breakSpeedMultiplier().calculate(appliedAbilityLevel());
        }

        @Override
        public ResourceLocation id() {
            return baseData().id();
        }

        @Override
        public int getDuration() {
            return (int) baseData().duration().calculate(appliedAbilityLevel());
        }
    }
}
