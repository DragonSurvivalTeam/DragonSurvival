package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonSizeArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

public class DragonSizeCommand {
    @Translation(comments = "Set size to %s for %s players.")
    private static final String SET_SIZE_TO_PLAYERS = Translation.Type.COMMAND.wrap("size.clear_from_entities");

    @Translation(comments = "Set size to %s for %s.")
    private static final String SET_SIZE_TO_PLAYER = Translation.Type.COMMAND.wrap("size.clear_from_single_entity");

    @Translation(comments = "Failed to set size since player was not a dragon.")
    private static final String FAILED_TO_SET_SIZE = Translation.Type.COMMAND.wrap("size.failed_to_set_size");

    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("dragon-size")
                        .requires(sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument(DragonSizeArgument.ID, new DragonSizeArgument(event.getBuildContext()))
                                                        .executes(sourceStackCommandContext -> {
                                                            Collection<? extends Player> players = EntityArgument.getPlayers(sourceStackCommandContext, "targets");
                                                            double size = DragonSizeArgument.get(sourceStackCommandContext);
                                                            int count = 0;
                                                            for(Player player : players) {
                                                                ServerPlayer serverPlayer = (ServerPlayer) player;
                                                                DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);

                                                                if (handler.isDragon()) {
                                                                    handler.setDesiredSize(serverPlayer, size);
                                                                    count++;
                                                                }
                                                            }

                                                            int finalCount = count;
                                                            if (players.size() == 1) {
                                                                if(count == 0) {
                                                                    sourceStackCommandContext.getSource().sendFailure(Component.translatable(FAILED_TO_SET_SIZE));
                                                                } else {
                                                                    sourceStackCommandContext.getSource().sendSuccess(() -> Component.translatable(SET_SIZE_TO_PLAYER, size, players.stream().findFirst().get().getDisplayName()), true);
                                                                }
                                                            } else {
                                                                sourceStackCommandContext.getSource().sendSuccess(() -> Component.translatable(SET_SIZE_TO_PLAYERS, size, finalCount), true);
                                                            }

                                                            return 1;
                                                        })
                                        )
                        )
        );
    }
}
