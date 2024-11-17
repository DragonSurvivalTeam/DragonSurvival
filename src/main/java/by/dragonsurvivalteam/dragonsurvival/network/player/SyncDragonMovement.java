package by.dragonsurvivalteam.dragonsurvival.network.player;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class SyncDragonMovement implements IMessage<SyncDragonMovement> {
	public int playerId;
	public boolean isFirstPerson;
	public boolean bite;
	public boolean isFreeLook;
	public float desiredMoveVecX;
	public float desiredMoveVecY;

	public SyncDragonMovement() {}

	public SyncDragonMovement(int playerId, boolean isFirstPerson, boolean bite, boolean isFreeLook, float desiredMoveVecX, float desiredMoveVecY) {
		this.playerId = playerId;
		this.isFirstPerson = isFirstPerson;
		this.bite = bite;
		this.isFreeLook = isFreeLook;
		this.desiredMoveVecX = desiredMoveVecX;
		this.desiredMoveVecY = desiredMoveVecY;
	}

	@Override
	public void encode(final SyncDragonMovement message, final FriendlyByteBuf buffer) {
		buffer.writeInt(message.playerId);
		buffer.writeBoolean(message.isFirstPerson);
		buffer.writeBoolean(message.bite);
		buffer.writeBoolean(message.isFreeLook);
		buffer.writeFloat(message.desiredMoveVecX);
		buffer.writeFloat(message.desiredMoveVecY);
    }

	@Override
	public SyncDragonMovement decode(final FriendlyByteBuf buffer) {
		return new SyncDragonMovement(buffer.readInt(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readFloat(), buffer.readFloat());
	}

	@Override
	public void handle(final SyncDragonMovement message, final Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();

		if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
			context.enqueueWork(() -> ClientProxy.handlePacketSyncCapabilityMovement(message)).thenRun(() -> context.setPacketHandled(true));
		} else if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			Entity entity = context.getSender();
			context.enqueueWork(() -> {
				DragonStateProvider.getCap(entity).ifPresent(handler -> {
					handler.setFirstPerson(message.isFirstPerson);
					handler.setBite(message.bite);
					handler.setFreeLook(message.isFreeLook);
					handler.setDesiredMoveVec(new Vec2(message.desiredMoveVecX, message.desiredMoveVecY));
				});
			})
					.thenRun(() -> NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message))
					.thenRun(() -> context.setPacketHandled(true));
		}
	}
}