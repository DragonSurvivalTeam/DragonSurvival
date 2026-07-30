package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDisableAbility(ResourceKey<DragonAbility> ability, boolean isDisabled, boolean isManual) implements CustomPacketPayload {
    public static final Type<SyncDisableAbility> TYPE = new Type<>(DragonSurvival.res("sync_ability_enabled"));

    public static final StreamCodec<FriendlyByteBuf, SyncDisableAbility> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.resourceKey(DragonAbility.REGISTRY), SyncDisableAbility::ability,
            ByteBufCodecs.BOOL, SyncDisableAbility::isDisabled,
            ByteBufCodecs.BOOL, SyncDisableAbility::isManual,
            SyncDisableAbility::new
    );

    @Translation(comments = "You do not have enough mana for the reservation cost of this ability")
    private static final String NOT_ENOUGH_MANA = Translation.Type.GUI.wrap("message.not_enough_mana_for_reserve");

    public static void handleServer(final SyncDisableAbility packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            MagicData magic = MagicData.getData(context.player());
            DragonAbilityInstance ability = magic.getAbility(packet.ability());

            if (ability == null) {
                return;
            }

            if (packet.isDisabled() && ability.isApplyingEffects() && ability == magic.getCurrentlyCasting()) {
                magic.stopCasting(context.player(), ability, true);
            }

            if (!packet.isDisabled() && packet.isManual() && ability.cannotReserve(context.player())) {
                // Automatic disable is already handled in the 'tick' method of the ability instance
                context.reply(new SyncDisableAbility(packet.ability(), true, true));
                context.player().sendSystemMessage(Component.translatable(NOT_ENOUGH_MANA).withColor(DSColors.RED));
                return;
            }

            ability.setDisabled(context.player(), packet.isDisabled(), packet.isManual());
        });
    }

    public static void handleClient(final SyncDisableAbility packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            MagicData data = MagicData.getData(context.player());
            DragonAbilityInstance ability = data.getAbility(packet.ability());

            if (ability == null) {
                return;
            }

            if (data.getCurrentlyCasting() != null && data.getCurrentlyCasting() == ability) {
                if (ability.isApplyingEffects()) {
                    data.stopCasting(context.player(), ability, true);
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
