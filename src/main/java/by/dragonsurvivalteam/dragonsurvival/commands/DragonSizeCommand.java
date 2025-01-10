package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

public class DragonSizeCommand {
    @Translation(comments = "Set %s size to %s.")
    private static final String SET_SIZE_TO_PLAYERS = Translation.Type.COMMAND.wrap("ability.clear_from_entities");

    @Translation(comments = "Set %s size to %s.")
    private static final String SET_SIZE_TO_PLAYER = Translation.Type.COMMAND.wrap("ability.clear_from_single_entity");

    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("dragon-size")
                        .requires(sourceStack -> sourceStack.hasPermission(2))
                        .then(
                                Commands.argument("targets", EntityArgument.players())
                                        .executes(sourceStackCommandContext -> {
                                            Collection<? extends Player> players = EntityArgument.getPlayers(sourceStackCommandContext, "targets");
                                            for(Player player : players) {
                                                ServerPlayer serverPlayer = (ServerPlayer) player;
                                                DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);

                                                if (handler.isDragon()) {
                                                    handler.setDesiredSize(serverPlayer, 1.0);
                                                }
                                            }

                                            return 1;
                                        })
                        )
        );
    }
}
