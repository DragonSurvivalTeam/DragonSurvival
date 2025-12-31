package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonGrowthArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSCommands;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
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

    @Translation(comments = "Failed to set growth due to target not being a dragon.")
    private static final String FAILED_TO_SET_GROWTH = Translation.Type.COMMAND.wrap("growth.failed_to_set_growth");

    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dragon-growth")
                .requires(sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument(DSCommands.TARGETS, EntityArgument.players())
                        .then(Commands.argument(DragonGrowthArgument.ID, new DragonGrowthArgument(event.getBuildContext()))
                                .executes(source -> {
                                    Collection<? extends Player> players = EntityArgument.getPlayers(source, DSCommands.TARGETS);
                                    double growth = DragonGrowthArgument.get(source);

                                    int count = 0;

                                    for (Player player : players) {
                                        DragonStateHandler handler = DragonStateProvider.getData(player);

                                        if (handler.isDragon()) {
                                            handler.setDesiredGrowth(player, growth);
                                            count++;
                                        }
                                    }

                                    int finalCount = count;

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
                                })
                        )
                )
        );
    }
}
