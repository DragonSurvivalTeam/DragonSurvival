package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonGrowthArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

public class DragonGrowthCommand {
    @Translation(comments = "Set growth to %s for %s players.")
    private static final String SET_GROWTH_TO_PLAYERS = Translation.Type.COMMAND.wrap("growth.clear_from_entities");

    @Translation(comments = "Set growth to %s for %s.")
    private static final String SET_GROWTH_TO_PLAYER = Translation.Type.COMMAND.wrap("growth.clear_from_single_entity");

    @Translation(comments = "Failed to set growth since player was not a dragon.")
    private static final String FAILED_TO_SET_GROWTH = Translation.Type.COMMAND.wrap("growth.failed_to_set_growth");

    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dragon-growth")
                .requires(sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument(DragonGrowthArgument.ID, new DragonGrowthArgument(event.getBuildContext()))
                                .executes(
                                        source -> changeGrowth(
                                                source,
                                                EntityArgument.getPlayers(source, "targets"),
                                                DragonGrowthArgument.get(source),
                                                0
                                        )
                                ).then(
                                        Commands.literal("add").then(
                                                Commands.argument("amount", DoubleArgumentType.doubleArg()).executes(
                                                        source -> changeGrowth(
                                                                source,
                                                                EntityArgument.getPlayers(source, "targets"),
                                                                DoubleArgumentType.getDouble(source, "amount"),
                                                                1
                                                        )
                                                )
                                        )
                                ).then(
                                        Commands.literal("sub").then(
                                                Commands.argument("amount", DoubleArgumentType.doubleArg()).executes(
                                                        source -> changeGrowth(
                                                                source,
                                                                EntityArgument.getPlayers(source, "targets"),
                                                                DoubleArgumentType.getDouble(source, "amount"),
                                                                -1
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int changeGrowth(CommandContext<CommandSourceStack> source, Collection<? extends Player> players, double growth, int modify) {

        int count = 0;
        double target_growth;

        for (Player player : players) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                target_growth = growth;
                if (modify != 0) {
                    target_growth = growth * modify + handler.getGrowth();
                }
                handler.setDesiredGrowth(player, target_growth);
                count++;
            }
        }

        int finalCount = count;

        // TODO: Different log messages for modifying growth instead of setting
        if (players.size() == 1) {
            if (count == 0) {
                source.getSource().sendFailure(Component.translatable(FAILED_TO_SET_GROWTH));
            } else {
                source.getSource().sendSuccess(() -> Component.translatable(SET_GROWTH_TO_PLAYER, growth, players.iterator().next().getDisplayName()), true);
            }
        } else {
            source.getSource().sendSuccess(() -> Component.translatable(SET_GROWTH_TO_PLAYERS, growth, finalCount), true);
        }

        return 1;
    }
}
