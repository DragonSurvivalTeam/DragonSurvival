package by.dragonsurvivalteam.dragonsurvival.network.magic;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncMagicCap implements IMessage<SyncMagicCap.Data> {
	public static void handleClient(final SyncMagicCap.Data message, final IPayloadContext context) {
		context.enqueueWork(() -> ClientProxy.handleSyncMagicCap(message, context.player().registryAccess()));
	}

	public static void handleServer(final SyncMagicCap.Data message, final IPayloadContext context) {
		Player sender = context.player();
		context.enqueueWork(()-> {
			DragonStateProvider.getCap(sender).ifPresent(handler -> {
				handler.getMagicData().deserializeNBT(context.player().registryAccess(), message.nbt);
			});
		});
	}

	public record Data(int playerId, CompoundTag nbt) implements CustomPacketPayload {
		public static final Type<Data> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "magic_cap"));

		public static final StreamCodec<FriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT,
			Data::playerId,
			ByteBufCodecs.COMPOUND_TAG,
			Data::nbt,
			Data::new
		);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}