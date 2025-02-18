package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncVisualEffectRemoval(int entityId, Holder<MobEffect> effect) implements CustomPacketPayload {
    public static final Type<SyncVisualEffectRemoval> TYPE = new Type<>(DragonSurvival.res("sync_visual_effect_removal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncVisualEffectRemoval> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncVisualEffectRemoval::entityId,
            ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), SyncVisualEffectRemoval::effect,
            SyncVisualEffectRemoval::new
    );

    public static void handleClient(final SyncVisualEffectRemoval packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.entityId()) instanceof LivingEntity entity) {
                if (entity.hasEffect(packet.effect())) {
                    entity.removeEffect(packet.effect());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}