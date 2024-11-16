package by.dragonsurvivalteam.dragonsurvival.network.flight;

import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class SyncDeltaMovement implements IMessage<SyncDeltaMovement> {
    public int playerId;
    public double speedX;
    public double speedY;
    public double speedZ;

    public SyncDeltaMovement() {}

    public SyncDeltaMovement(int playerId, double speedX, double speedY, double speedZ) {
        this.playerId = playerId;
        this.speedX = speedX;
        this.speedY = speedY;
        this.speedZ = speedZ;
    }

    @Override
    public void encode(SyncDeltaMovement message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.playerId);
        buffer.writeDouble(message.speedX);
        buffer.writeDouble(message.speedY);
        buffer.writeDouble(message.speedZ);
    }

    @Override
    public SyncDeltaMovement decode(FriendlyByteBuf buffer) {
        return new SyncDeltaMovement(buffer.readInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public void handle(SyncDeltaMovement message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.enqueueWork(() -> ClientProxy.handleSyncDeltaMovement(message));
        } else if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            Player sender = context.getSender();

            if (sender != null) {
                // This needs to be set so that it can be read back in some server side logic that uses deltamovement (e.g. DragonDestructionHandler)
                sender.setDeltaMovement(message.speedX, message.speedY, message.speedZ);
                NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> sender), new SyncDeltaMovement(sender.getId(), message.speedX, message.speedY, message.speedZ));
            }
        }

        context.setPacketHandled(true);
    }
}
