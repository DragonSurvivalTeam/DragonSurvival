package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonEditor;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.minecraft.commands.Commands.literal;

public class DragonEditorCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.getRoot().addChild(literal("dragon-editor")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS)) // TODO :: should this be allowed with lower permissions?
                .executes(context -> runCommand(context.getSource().getPlayerOrException()))
                .build()
        );
    }

    private static int runCommand(final ServerPlayer player) {
        if (DragonStateProvider.isDragon(player)) {
            PacketDistributor.sendToPlayer(player, new RequestOpenDragonEditor.Data());
        }

        return 1;
    }
}