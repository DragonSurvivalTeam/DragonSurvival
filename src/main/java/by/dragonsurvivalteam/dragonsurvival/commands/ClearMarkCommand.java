package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ClearMarkCommand {
    @Translation(comments = "Mark cleared from %s players.")
    private static final String FROM_PLAYERS = Translation.Type.COMMAND.wrap("clear_mark.from_players");

    @Translation(comments = "Mark cleared from %s.")
    private static final String FROM_SINGLE_PLAYER = Translation.Type.COMMAND.wrap("clear_mark.from_single_player");


    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("dragon-mark")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(
                        literal("clear")
                                .executes(commandSourceStack -> clearMark(commandSourceStack.getSource(), ImmutableList.of(commandSourceStack.getSource().getPlayerOrException())))
                                .then(
                                        argument("targets", EntityArgument.players())
                                                .executes(commandSourceStack -> clearMark(commandSourceStack.getSource(), EntityArgument.getPlayers(commandSourceStack, "targets")))
                                )
                )
        );
    }

    private static int clearMark(CommandSourceStack source, Collection<? extends Player> targets) {
        for (Entity target : targets) {
            if (target instanceof ServerPlayer) {
                DragonStateProvider.getData((ServerPlayer) target).markedByEnderDragon = false;
            }
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable(FROM_SINGLE_PLAYER, targets.stream().findFirst().get().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(FROM_PLAYERS, targets.size()), true);
        }

        return 1;
    }
}
