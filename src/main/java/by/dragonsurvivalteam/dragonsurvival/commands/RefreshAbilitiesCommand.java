package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class RefreshAbilitiesCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.getRoot().addChild(literal("dragon-refresh-abilities")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> runCommand(context.getSource().getPlayerOrException()))
                .build()
        );
    }

    private static int runCommand(final ServerPlayer player) {
        MagicData.getData(player).refresh(player, DragonStateProvider.getData(player).species());
        return 1;
    }
}
