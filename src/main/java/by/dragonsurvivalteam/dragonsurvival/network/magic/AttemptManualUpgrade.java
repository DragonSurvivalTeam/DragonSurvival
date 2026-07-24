package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperiencePointsUpgrade;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record AttemptManualUpgrade(ResourceKey<DragonAbility> ability, ExperiencePointsUpgrade.Type upgradeType) implements CustomPacketPayload {
    public static final Type<AttemptManualUpgrade> TYPE = new Type<>(DragonSurvival.res("attempt_manual_upgrade"));

    public static final StreamCodec<FriendlyByteBuf, AttemptManualUpgrade> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.resourceKey(DragonAbility.REGISTRY), AttemptManualUpgrade::ability,
            ByteBufCodecs.enumCodec(ExperiencePointsUpgrade.Type.class), AttemptManualUpgrade::upgradeType,
            AttemptManualUpgrade::new
    );

    public static void handleServer(final AttemptManualUpgrade packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            MagicData data = MagicData.getData(serverPlayer);
            DragonAbilityInstance ability = data.getAbility(packet.ability());

            if (ability != null) {
                ability.value().upgrade().ifPresent(upgrade -> upgrade.attempt(serverPlayer, ability, packet.upgradeType()));
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
