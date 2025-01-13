package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncVisualEffectAdded(int entityId, MobEffectInstance effect) implements CustomPacketPayload {
    public static final Type<SyncVisualEffectAdded> TYPE = new Type<>(DragonSurvival.res("sync_visual_effect_added"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncVisualEffectAdded> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncVisualEffectAdded::entityId,
            ByteBufCodecs.fromCodecWithRegistries(MobEffectInstance.CODEC), SyncVisualEffectAdded::effect,
            SyncVisualEffectAdded::new
    );

    public static void handleClient(final SyncVisualEffectAdded packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.entityId()) instanceof LivingEntity entity) {
                entity.addEffect(packet.effect());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}