package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncAbilityEnabled(ResourceKey<DragonAbility> ability, boolean isEnabled, boolean wasDoneManually) implements CustomPacketPayload {
    public static final Type<SyncAbilityEnabled> TYPE = new Type<>(DragonSurvival.res("sync_ability_enabled"));

    public static final StreamCodec<FriendlyByteBuf, SyncAbilityEnabled> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(DragonAbility.REGISTRY), SyncAbilityEnabled::ability,
            ByteBufCodecs.BOOL, SyncAbilityEnabled::isEnabled,
            ByteBufCodecs.BOOL, SyncAbilityEnabled::wasDoneManually,
            SyncAbilityEnabled::new
    );

    public static void handleServer(final SyncAbilityEnabled packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            MagicData data = MagicData.getData(context.player());
            DragonAbilityInstance ability = data.getAbilities().get(packet.ability());

            if (context.player() instanceof ServerPlayer serverPlayer && !packet.isEnabled() && ability.isApplyingEffects()) {
                ability.setActive(false, serverPlayer);
            }

            ability.setEnabled(packet.isEnabled(), packet.wasDoneManually());
        });
    }

    public static void handleClient(final SyncAbilityEnabled packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            MagicData data = MagicData.getData(context.player());
            DragonAbilityInstance ability = data.getAbilities().get(packet.ability());
            ability.setEnabled(packet.isEnabled(), packet.wasDoneManually());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
