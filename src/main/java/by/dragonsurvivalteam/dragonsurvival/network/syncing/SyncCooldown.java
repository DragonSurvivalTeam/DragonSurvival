package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncCooldown(ResourceKey<DragonAbility> ability, int cooldown) implements CustomPacketPayload {
    public static final Type<SyncCooldown> TYPE = new Type<>(DragonSurvival.res("sync_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCooldown> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(DragonAbility.REGISTRY), SyncCooldown::ability,
            ByteBufCodecs.INT, SyncCooldown::cooldown,
            SyncCooldown::new
    );

    public static void handleClient(final SyncCooldown packet, final IPayloadContext context) {
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