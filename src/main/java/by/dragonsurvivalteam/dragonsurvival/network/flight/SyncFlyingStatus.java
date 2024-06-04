package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;
import static by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler.DRAGON_HANDLER;

public class SyncFlyingStatus implements IMessage<SyncFlyingStatus.Data> {

	public static void handleClient(final SyncFlyingStatus.Data message, final IPayloadContext context) {
		context.enqueueWork(() -> ClientProxy.handleSyncFlyingStatus(message));
	}

	public static void handleServer(final SyncFlyingStatus.Data message, final IPayloadContext context) {
		Player sender = context.player();
		DragonStateHandler handler = sender.getData(DRAGON_HANDLER);
		handler.setWingsSpread(message.state);
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(sender, message);
	}

	public record Data(int playerId, boolean state) implements CustomPacketPayload  {
		public static final Type<Data> TYPE = new Type<>(new ResourceLocation(MODID, "flying_status"));

		public static final StreamCodec<FriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				Data::playerId,
				ByteBufCodecs.BOOL,
				Data::state,
				Data::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}