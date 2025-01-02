package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperienceUpgrade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record AttemptManualUpgrade(ResourceKey<DragonAbility> ability, ExperienceUpgrade.Type upgradeType) implements CustomPacketPayload {
    public static final Type<AttemptManualUpgrade> TYPE = new Type<>(DragonSurvival.res("attempt_manual_upgrade"));

    public static final StreamCodec<FriendlyByteBuf, AttemptManualUpgrade> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(DragonAbility.REGISTRY), AttemptManualUpgrade::ability,
            NeoForgeStreamCodecs.enumCodec(ExperienceUpgrade.Type.class), AttemptManualUpgrade::upgradeType,
            AttemptManualUpgrade::new
    );

    public static void handleServer(final AttemptManualUpgrade packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }

            MagicData data = MagicData.getData(serverPlayer);
            DragonAbilityInstance ability = data.getAbilities().get(packet.ability());
            ability.value().upgrade().ifPresent(upgrade -> upgrade.attempt(serverPlayer, ability, packet.upgradeType()));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
