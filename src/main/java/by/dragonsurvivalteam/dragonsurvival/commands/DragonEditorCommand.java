package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonEditorPacket;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import static net.minecraft.commands.Commands.literal;


public class DragonEditorCommand{
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher){
		RootCommandNode<CommandSourceStack> rootCommandNode = commandDispatcher.getRoot();
		LiteralCommandNode<CommandSourceStack> dragon = literal("dragon-editor").requires(commandSource -> commandSource.hasPermission(2)).executes(context -> {
			return runCommand(context.getSource());
		}).build();

		rootCommandNode.addChild(dragon);
	}

	private static int runCommand(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();

		if (DragonUtils.isDragon(player)) {
			NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenDragonEditorPacket());
			return 1;
		}

		source.sendFailure(Component.translatable("command.dragonsurvival.failure.open_editor_as_non_dragon"));
		return 0;
	}
}