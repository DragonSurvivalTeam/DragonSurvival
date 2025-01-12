package by.dragonsurvivalteam.dragonsurvival.network.animation;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationType;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.CompoundAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public record SyncAbilityAnimation(int playerId, AnimationType animationType, Either<CompoundAbilityAnimation, SimpleAbilityAnimation> animation) implements CustomPacketPayload {

    public static final Type<SyncAbilityAnimation> TYPE = new Type<>(DragonSurvival.res("sync_ability_animation"));

    public static final StreamCodec<FriendlyByteBuf, SyncAbilityAnimation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncAbilityAnimation::playerId,
            ByteBufCodecs.STRING_UTF8.map(AnimationType::valueOf, AnimationType::name), SyncAbilityAnimation::animationType,
            ByteBufCodecs.fromCodec(Codec.either(CompoundAbilityAnimation.CODEC, SimpleAbilityAnimation.CODEC)), SyncAbilityAnimation::animation,
            SyncAbilityAnimation::new
    );

    public static void handleClient(final SyncAbilityAnimation packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.playerId());

            if (!(entity instanceof Player player)) {
                return;
            }

            AtomicReference<DragonEntity> dragonEntity = ClientDragonRenderer.PLAYER_DRAGON_MAP.get(player.getId());
            if(dragonEntity == null) {
                return;
            }

            AbilityAnimation abilityAnimation = packet.animation.map(
                    simple -> simple,
                    compound -> compound
            );

            dragonEntity.get().setCurrentAbilityAnimation(new Pair<>(abilityAnimation, packet.animationType));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
