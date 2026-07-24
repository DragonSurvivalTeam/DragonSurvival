package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncAbilityLevel(ResourceKey<DragonAbility> ability, int level) implements CustomPacketPayload {
    public static final Type<SyncAbilityLevel> TYPE = new Type<>(DragonSurvival.res("sync_ability_level"));

    public static final StreamCodec<FriendlyByteBuf, SyncAbilityLevel> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.resourceKey(DragonAbility.REGISTRY), SyncAbilityLevel::ability,
            ByteBufCodecs.VAR_INT, SyncAbilityLevel::level,
            SyncAbilityLevel::new
    );

    public static void handleClient(final SyncAbilityLevel packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            MagicData data = MagicData.getData(context.player());
            DragonAbilityInstance ability = data.getAbility(packet.ability());

            if (ability != null) {
                ability.setLevel(packet.level());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
