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
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDisableAbility(ResourceKey<DragonAbility> ability, boolean isDisabled, boolean isManual) implements CustomPacketPayload {
    public static final Type<SyncDisableAbility> TYPE = new Type<>(DragonSurvival.res("sync_ability_enabled"));

    public static final StreamCodec<FriendlyByteBuf, SyncDisableAbility> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(DragonAbility.REGISTRY), SyncDisableAbility::ability,
            ByteBufCodecs.BOOL, SyncDisableAbility::isDisabled,
            ByteBufCodecs.BOOL, SyncDisableAbility::isManual,
            SyncDisableAbility::new
    );

    public static void handleServer(final SyncDisableAbility packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            MagicData data = MagicData.getData(context.player());
            DragonAbilityInstance ability = data.getAbilities().get(packet.ability());

            if (packet.isDisabled() && ability.isApplyingEffects() && ability == data.getCurrentlyCasting()) {
                data.stopCasting(context.player(), true);
            }

            ability.setDisabled(context.player(), packet.isDisabled(), packet.isManual());
        });
    }

    public static void handleClient(final SyncDisableAbility packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            MagicData data = MagicData.getData(context.player());
            DragonAbilityInstance ability = data.getAbilities().get(packet.ability());

            if (data.getCurrentlyCasting() != null && data.getCurrentlyCasting() == ability) {
                if (ability.isApplyingEffects()) {
                    data.stopCasting(context.player(), true);
                } else {
                    data.stopCasting(context.player());
                }
            }

            ability.setDisabled(context.player(), packet.isDisabled(), packet.isManual());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
