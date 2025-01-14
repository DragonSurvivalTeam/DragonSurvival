package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncGlowInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public record Glow(ResourceLocation id, TextColor color, LevelBasedValue duration) {
    public static final Codec<Glow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Glow::id),
            TextColor.CODEC.fieldOf("color").forGetter(Glow::color),
            LevelBasedValue.CODEC.optionalFieldOf("duration", DragonAbilities.INFINITE_DURATION).forGetter(Glow::duration)
    ).apply(instance, Glow::new));

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        int newDuration = (int) duration.calculate(ability.level());

        GlowData data = target.getData(DSDataAttachments.GLOW);
        Glow.Instance instance = data.get(id);

        if (instance != null && instance.appliedAbilityLevel() == ability.level() && instance.currentDuration() == newDuration) {
            return;
        }

        data.remove(target, instance);
        data.add(target, new Glow.Instance(this, ClientEffectProvider.ClientData.from(dragon, ability), ability.level(), newDuration));
    }

    public void remove(final Entity target) {
        GlowData data = target.getData(DSDataAttachments.GLOW);
        data.remove(target, data.get(id));
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
            return baseData().id();
        }

        @Override
        public int getDuration() {
            return (int) baseData().duration().calculate(appliedAbilityLevel());
        }

        @Override
        public boolean isHidden() {
            // There is nothing interesting to show
            return true;
        }
    }
}
