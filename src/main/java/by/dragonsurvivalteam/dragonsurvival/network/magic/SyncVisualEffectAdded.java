package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncVisualEffectAdded(int entityId, MobEffectInstance effect) implements CustomPacketPayload {
    public static final Type<SyncVisualEffectAdded> TYPE = new Type<>(DragonSurvival.res("sync_visual_effect_added"));

    public static final StreamCodec<FriendlyByteBuf, SyncVisualEffectAdded> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncVisualEffectAdded::entityId,
            ByteBufCodecs.MOB_EFFECT_INSTANCE, SyncVisualEffectAdded::effect,
            SyncVisualEffectAdded::new
    );

    public static void handleClient(final SyncVisualEffectAdded packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.entityId()) instanceof LivingEntity entity) {
                MobEffectInstance current = entity.getEffect(packet.effect().getEffect());

                if (current == null || current.getDuration() != packet.effect().getDuration() || current.getAmplifier() != packet.effect().getAmplifier()) {
                    entity.addEffect(packet.effect());

                    // Client does not trigger the "effect added" event
                    if (packet.effect().getEffect() == DSEffects.HUNTER.get()) {
                        HunterHandler.informUser(entity);
                    }
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
