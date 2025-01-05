package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class ClearMarkCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.getRoot().addChild(literal("dragon-clear-mark")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> runCommand(context.getSource().getPlayerOrException()))
                .build()
        );
    }

    private static int runCommand(final ServerPlayer player) {
        DragonStateProvider.getData(player).markedByEnderDragon = false;
        return 1;
    }
}
