package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncGlowInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record Glow(DurationInstanceBase base, TextColor color) {
    public static final Codec<Glow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(Glow::base),
            TextColor.CODEC.fieldOf("color").forGetter(Glow::color)
    ).apply(instance, Glow::new));

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        int newDuration = (int) base.duration().calculate(ability.level());

        GlowData data = target.getData(DSDataAttachments.GLOW);
        Glow.Instance instance = data.get(base.id());

        if (instance != null && instance.appliedAbilityLevel() == ability.level() && instance.currentDuration() == newDuration) {
            return;
        }

        data.remove(target, instance);
        data.add(target, new Glow.Instance(this, ClientEffectProvider.ClientData.from(dragon, ability), ability.level(), newDuration));
    }

    public void remove(final Entity target) {
        GlowData data = target.getData(DSDataAttachments.GLOW);
        data.remove(target, data.get(base.id()));
    }

    public static Glow create(final ResourceLocation id, final TextColor color) {
        return new Glow(DurationInstanceBase.create(id).infinite().hidden().build(), color);
    }

    public static Glow create(final ResourceLocation id, final LevelBasedValue duration, final TextColor color) {
        return new Glow(DurationInstanceBase.create(id).duration(duration).hidden().build(), color);
    }

    public static class Instance extends DurationInstance<Glow> {
        public static final Codec<Glow.Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(instance, () -> Glow.CODEC).apply(instance, Glow.Instance::new));

        public Instance(final Glow baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration) {
            super(baseData, clientData, appliedAbilityLevel, currentDuration);
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Glow.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }

        @Override
        public Component getDescription() {
            return Component.empty();
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (!storageHolder.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(storageHolder, new SyncGlowInstance(storageHolder.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (!storageHolder.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(storageHolder, new SyncGlowInstance(storageHolder.getId(), this, true));
            }
        }

        public int getColor() {
            return baseData().color().getValue();
        }

        @Override
        public ResourceLocation id() {
            return baseData().base().id();
        }

        @Override
        public int getDuration() {
            return (int) baseData().base().duration().calculate(appliedAbilityLevel());
        }

        @Override
        public Optional<LootItemCondition> earlyRemovalCondition() {
            return baseData().base().earlyRemovalCondition();
        }

        @Override
        public boolean isHidden() {
            return baseData().base().isHidden();
        }
    }
}
