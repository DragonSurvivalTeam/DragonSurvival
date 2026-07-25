package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncVisualEffectRemoval(int entityId, Holder<MobEffect> effect) implements CustomPacketPayload {
    public static final Type<SyncVisualEffectRemoval> TYPE = new Type<>(DragonSurvival.res("sync_visual_effect_removal"));

    public static final StreamCodec<FriendlyByteBuf, SyncVisualEffectRemoval> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncVisualEffectRemoval::entityId,
            ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), SyncVisualEffectRemoval::effect,
            SyncVisualEffectRemoval::new
    );

    public static void handleClient(final SyncVisualEffectRemoval packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.entityId()) instanceof LivingEntity entity) {
                if (entity.hasEffect(packet.effect().value())) {
                    entity.removeEffect(packet.effect().value());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
