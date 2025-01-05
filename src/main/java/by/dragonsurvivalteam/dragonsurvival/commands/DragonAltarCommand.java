package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.minecraft.commands.Commands.literal;

public class DragonAltarCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.getRoot().addChild(literal("dragon-altar")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> runCommand(context.getSource().getPlayerOrException()))
                .build()
        );
    }

    private static int runCommand(final ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, OpenDragonAltar.INSTANCE);
        return 1;
    }
}