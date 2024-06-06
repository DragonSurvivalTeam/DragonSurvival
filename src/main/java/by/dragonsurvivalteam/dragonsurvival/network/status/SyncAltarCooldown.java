package by.dragonsurvivalteam.dragonsurvival.network.status;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;
import static by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler.DRAGON_HANDLER;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncAltarCooldown implements IMessage<SyncAltarCooldown.Data> {

	public static void handleClient(final Data message, final IPayloadContext context) {
		context.enqueueWork(() -> ClientProxy.handleSyncAltarCooldown(message));
	}

	public static void handleServer (final Data message, final IPayloadContext context) {
		DragonStateHandler handler = context.player().getData(DRAGON_HANDLER);
		handler.altarCooldown = message.cooldown;
		handler.hasUsedAltar = true;

		PacketDistributor.sendToPlayersTrackingEntityAndSelf(context.player(), message);
	}

	public record Data (int playerId, int cooldown) implements CustomPacketPayload {
		public static final Type<Data> TYPE = new Type<>(new ResourceLocation(MODID, "altar_cooldown"));

		public static final StreamCodec<FriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				Data::playerId,
				ByteBufCodecs.VAR_INT,
				Data::cooldown,
				Data::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}