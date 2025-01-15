package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncGlowInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
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
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Glow extends DurationInstanceBase<GlowData, Glow.Instance> {
    public static final Codec<Glow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            TextColor.CODEC.fieldOf("color").forGetter(Glow::color)
    ).apply(instance, Glow::new));

    private final TextColor color;

    public Glow(final DurationInstanceBase<?, ?> base, TextColor color) {
        super(base);
        this.color = color;
    }

    public static Glow create(final ResourceLocation id, final TextColor color) {
        return new Glow(DurationInstanceBase.create(id).infinite().removeAutomatically().hidden().build(), color);
    }

    public static Glow create(final ResourceLocation id, final LevelBasedValue duration, final TextColor color) {
        return new Glow(DurationInstanceBase.create(id).duration(duration).hidden().build(), color);
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration);
    }

    @Override
    public AttachmentType<GlowData> type() {
        return DSDataAttachments.GLOW.value();
    }

    public TextColor color() {
        return color;
    }

    public static class Instance extends DurationInstance<Glow> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(
                instance, () -> Glow.CODEC).apply(instance, Instance::new)
        );

        public Instance(final Glow baseData, final CommonData commonData, int currentDuration) {
            super(baseData, commonData, currentDuration);
        }

        public int getColor() {
            return baseData().color().getValue();
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

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Glow.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }
}
