package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonSizeArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DragonSizeCommand {
    public static void register(final RegisterCommandsEvent event) {
        LiteralCommandNode<CommandSourceStack> command = literal("dragon-set-size").requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS)).build();

        ArgumentCommandNode<CommandSourceStack, Double> sizeArgument = argument(DragonSizeArgument.ID, new DragonSizeArgument(event.getBuildContext())).executes(context -> {
            double size = DragonSizeArgument.get(context);
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);

            if (handler.isDragon()) {
                handler.setDesiredSize(serverPlayer, size);
            }

            return 1;
        }).build();

        command.addChild(sizeArgument);
        event.getDispatcher().getRoot().addChild(command);
    }
}
