package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public record SyncCooldown(ResourceKey<DragonAbility> ability, int cooldown) implements CustomPacketPayload {
    public static final Type<SyncCooldown> TYPE = new Type<>(DragonSurvival.res("sync_cooldown"));

    public static final StreamCodec<FriendlyByteBuf, SyncCooldown> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.resourceKey(DragonAbility.REGISTRY), SyncCooldown::ability,
            ByteBufCodecs.INT, SyncCooldown::cooldown,
            SyncCooldown::new
    );

    public static void handleClient(final SyncCooldown packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            MagicData magic = MagicData.getData(context.player());
            DragonAbilityInstance instance = magic.getAbility(packet.ability());

            if (instance != null) {
                instance.setCooldown(packet.cooldown());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}