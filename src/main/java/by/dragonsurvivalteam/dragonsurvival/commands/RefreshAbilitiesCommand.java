package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class RefreshAbilitiesCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        RootCommandNode<CommandSourceStack> rootCommandNode = commandDispatcher.getRoot();
        LiteralCommandNode<CommandSourceStack> dragon = literal("refresh-abilities").requires(commandSource -> commandSource.hasPermission(2)).executes(context -> runCommand(context.getSource().getPlayerOrException())).build();
        rootCommandNode.addChild(dragon);
    }

    private static int runCommand(ServerPlayer serverPlayer) {
        DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);
        MagicData magic = MagicData.getData(serverPlayer);
        magic.refresh(serverPlayer, handler.species());
        return 1;
    }
}
