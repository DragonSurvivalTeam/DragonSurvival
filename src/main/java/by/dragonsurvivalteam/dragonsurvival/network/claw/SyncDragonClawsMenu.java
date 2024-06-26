package by.dragonsurvivalteam.dragonsurvival.network.claw;

import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.ClawInventory;
import by.dragonsurvivalteam.dragonsurvival.network.IMessage;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncDragonClawsMenu implements IMessage<SyncDragonClawsMenu> {
	public int playerId;
	public boolean state;

	public SimpleContainer clawInventory;

	public SyncDragonClawsMenu() { /* Nothing to do */ }

	public SyncDragonClawsMenu(int playerId, boolean state, final SimpleContainer clawInventory) {
		this.playerId = playerId;
		this.state = state;
		this.clawInventory = clawInventory;
	}

	@Override
	public void encode(final SyncDragonClawsMenu message, final FriendlyByteBuf buffer) {
		buffer.writeInt(message.playerId);
		buffer.writeBoolean(message.state);
		CompoundTag nbt = new CompoundTag();
		nbt.put("inv", ClawInventory.saveClawInventory(message.clawInventory));
		buffer.writeNbt(nbt);
	}

	@Override
	public SyncDragonClawsMenu decode(final FriendlyByteBuf buffer) {
		int playerId = buffer.readInt();
		boolean state = buffer.readBoolean();
		CompoundTag tag = buffer.readNbt();
		SimpleContainer inventory = ClawInventory.readClawInventory(tag.getList("inv", CompoundTag.TAG_COMPOUND));
		return new SyncDragonClawsMenu(playerId, state, inventory);
	}

	@Override
	public void handle(final SyncDragonClawsMenu message, final Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();

		if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
			context.enqueueWork(() -> ClientProxy.handleSyncDragonClawsMenu(message));
		}

		context.setPacketHandled(true);
	}
}