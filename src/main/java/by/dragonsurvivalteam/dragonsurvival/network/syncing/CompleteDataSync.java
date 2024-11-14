package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.network.ISidedMessage;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonModifiers;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class CompleteDataSync extends ISidedMessage<CompleteDataSync> {
	private CompoundTag nbt;

	public CompleteDataSync() {
		super(-1);
	}

	public CompleteDataSync(final Player player) {
		super(player.getId());
		DragonStateProvider.getCap(player).ifPresent(cap -> nbt = cap.writeNBT());
	}

	public CompleteDataSync(int playerId, final CompoundTag nbt) {
		super(playerId);
		this.nbt = nbt;
	}

	@Override
	public void encode(final CompleteDataSync message, final FriendlyByteBuf buffer) {
		buffer.writeInt(message.playerId);
		buffer.writeNbt(message.nbt);
	}

	@Override
	public CompleteDataSync decode(final FriendlyByteBuf buffer) {
		return new CompleteDataSync(buffer.readInt(), buffer.readNbt());
	}

	@Override
	public CompleteDataSync create(final CompleteDataSync message) {
		return new CompleteDataSync(message.playerId, message.nbt);
	}

	@Override
	public void runCommon(final CompleteDataSync message, final NetworkEvent.Context context) { /* Nothing to do */ }

	@Override // Currently only relevant for DragonEditorScreen#confirm
	public void runServer(final CompleteDataSync message, final NetworkEvent.Context context, final ServerPlayer player) {
		context.enqueueWork(() -> {
			DragonStateProvider.getCap(player).ifPresent(handler -> {
				SimpleContainer container = handler.getClawToolData().getClawsInventory();

				AbstractDragonType oldType = handler.getType();
				AbstractDragonBody oldBody = handler.getBody();
				double oldSize = handler.getSize();

				handler.readNBT(message.nbt);

				if (!DragonUtils.isDragonType(oldType, handler.getType())) {
					DragonModifiers.updateTypeModifiers(player);
				}

				if (oldBody != handler.getBody()) {
					DragonModifiers.updateBodyModifiers(player);
				}

				if (oldSize != handler.getSize()) {
					DragonModifiers.updateSizeModifiers(player);
				}

				handler.getClawToolData().setClawsInventory(container); // TODO :: Why is the old state restored?
			});

			player.refreshDimensions();
		});
	}

	@Override
	public void runClient(final CompleteDataSync message, final NetworkEvent.Context context, final Player player) {
		DragonStateProvider.getCap(player).ifPresent(handler -> handler.readNBT(message.nbt));
		player.refreshDimensions();
	}
}