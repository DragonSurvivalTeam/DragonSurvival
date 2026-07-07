package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements AdditionalEffectData {
    @Unique private static final String dragonSurvival$APPLIER = "applier";

    @Mutable @Shadow @Final public static Codec<MobEffectInstance> CODEC;
    @Mutable @Shadow @Final public static StreamCodec<RegistryFriendlyByteBuf, MobEffectInstance> STREAM_CODEC;

    @Shadow @Final private Holder<MobEffect> effect;
    @Shadow private int duration;
    @Shadow private int amplifier;
    @Shadow private boolean ambient;
    @Shadow private boolean visible;
    @Shadow private boolean showIcon;
    @Shadow @Nullable private MobEffectInstance hiddenEffect;

    @Unique @Nullable private Entity dragonSurvival$applier;
    @Unique @Nullable private UUID dragonSurvival$applierUUID;
    @Unique @Nullable private ThreadLocal<Player> dragonSurvival$entity;

    @Override
    public void dragonSurvival$setApplier(@Nullable final Entity applier) {
        dragonSurvival$applier = applier;
        dragonSurvival$applierUUID = applier != null ? applier.getUUID() : null;
    }

    @Override
    public @Nullable Entity dragonSurvival$getApplier(final ServerLevel level) {
        if (dragonSurvival$applier == null && dragonSurvival$applierUUID != null) {
            dragonSurvival$applier = level.getEntity(dragonSurvival$applierUUID);

            if (dragonSurvival$applier == null) {
                dragonSurvival$applier = level.getServer().getPlayerList().getPlayer(dragonSurvival$applierUUID);
            }
        }

        return dragonSurvival$applier;
    }

    @Override
    public void dragonSurvival$setApplierUUID(final UUID applierUUID) {
        dragonSurvival$applierUUID = applierUUID;
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void dragonSurvival$extendSerialization(final CallbackInfo callback) {
        CODEC = Codec.recursive(
                "DragonSurvivalMobEffectInstance",
                codec -> RecordCodecBuilder.create(instance -> instance.group(
                        MobEffect.CODEC.fieldOf("id").forGetter(MobEffectInstance::getEffect),
                        ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("amplifier", 0).forGetter(MobEffectInstance::getAmplifier),
                        Codec.INT.optionalFieldOf("duration", 0).forGetter(MobEffectInstance::getDuration),
                        Codec.BOOL.optionalFieldOf("ambient", false).forGetter(MobEffectInstance::isAmbient),
                        Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(MobEffectInstance::isVisible),
                        Codec.BOOL.optionalFieldOf("show_icon").forGetter(effect -> Optional.of(effect.showIcon())),
                        codec.optionalFieldOf("hidden_effect").forGetter(effect -> Optional.ofNullable(((MobEffectInstanceMixin) (Object) effect).hiddenEffect)),
                        UUIDUtil.CODEC.optionalFieldOf(dragonSurvival$APPLIER).forGetter(effect -> Optional.ofNullable(((MobEffectInstanceMixin) (Object) effect).dragonSurvival$applierUUID))
                ).apply(instance, MobEffectInstanceMixin::dragonSurvival$create))
        );
        STREAM_CODEC = dragonSurvival$createStreamCodec();
    }

    @Inject(method = "setDetailsFrom", at = @At("RETURN"))
    private void dragonSurvival$copyAdditionalData(final MobEffectInstance copy, final CallbackInfo callback) {
        MobEffectInstanceMixin instance = (MobEffectInstanceMixin) (Object) copy;
        dragonSurvival$applier = instance.dragonSurvival$applier;
        dragonSurvival$applierUUID = instance.dragonSurvival$applierUUID;
    }

    @Inject(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;tickDownDuration()V"))
    private void dragonSurvival$storeEntity(final ServerLevel serverLevel, final LivingEntity entity, final Runnable onEffectUpdate, final org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> callback) {
        if (entity instanceof Player player && effect.is(DSEffects.SOURCE_OF_MAGIC)) {
            if (dragonSurvival$entity == null) {
                dragonSurvival$entity = new ThreadLocal<>();
            }

            dragonSurvival$entity.set(player);
        }
    }

    @Inject(method = "tickDownDuration", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$retainDuration(final CallbackInfo callback) {
        if (dragonSurvival$entity != null && effect.is(DSEffects.SOURCE_OF_MAGIC)) {
            Player player = dragonSurvival$entity.get();

            if (player != null && DragonStateProvider.getData(player).isOnMagicSource) {
                callback.cancel();
            }

            dragonSurvival$entity.remove();
        }
    }

    @Unique private static MobEffectInstance dragonSurvival$create(
            final Holder<MobEffect> effect,
            final int amplifier,
            final int duration,
            final boolean ambient,
            final boolean showParticles,
            final Optional<Boolean> showIcon,
            final Optional<MobEffectInstance> hiddenEffect,
            final Optional<UUID> applierUUID
    ) {
        MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier, ambient, showParticles, showIcon.orElse(showParticles), hiddenEffect.orElse(null));
        applierUUID.ifPresent(((AdditionalEffectData) instance)::dragonSurvival$setApplierUUID);
        return instance;
    }

    @Unique private static StreamCodec<RegistryFriendlyByteBuf, MobEffectInstance> dragonSurvival$createStreamCodec() {
        return new StreamCodec<>() {
            @Override
            public MobEffectInstance decode(final RegistryFriendlyByteBuf buffer) {
                Holder<MobEffect> effect = MobEffect.STREAM_CODEC.decode(buffer);
                int amplifier = ByteBufCodecs.VAR_INT.decode(buffer);
                int duration = ByteBufCodecs.VAR_INT.decode(buffer);
                boolean ambient = ByteBufCodecs.BOOL.decode(buffer);
                boolean showParticles = ByteBufCodecs.BOOL.decode(buffer);
                boolean showIcon = ByteBufCodecs.BOOL.decode(buffer);
                Optional<MobEffectInstance> hiddenEffect = ByteBufCodecs.optional(this).decode(buffer);
                Optional<UUID> applierUUID = ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).decode(buffer);
                return dragonSurvival$create(effect, amplifier, duration, ambient, showParticles, Optional.of(showIcon), hiddenEffect, applierUUID);
            }

            @Override
            public void encode(final RegistryFriendlyByteBuf buffer, final MobEffectInstance input) {
                MobEffectInstanceMixin instance = (MobEffectInstanceMixin) (Object) input;
                MobEffect.STREAM_CODEC.encode(buffer, input.getEffect());
                ByteBufCodecs.VAR_INT.encode(buffer, input.getAmplifier());
                ByteBufCodecs.VAR_INT.encode(buffer, input.getDuration());
                ByteBufCodecs.BOOL.encode(buffer, input.isAmbient());
                ByteBufCodecs.BOOL.encode(buffer, input.isVisible());
                ByteBufCodecs.BOOL.encode(buffer, input.showIcon());
                ByteBufCodecs.optional(this).encode(buffer, Optional.ofNullable(instance.hiddenEffect));
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buffer, Optional.ofNullable(instance.dragonSurvival$applierUUID));
            }
        };
    }
}
