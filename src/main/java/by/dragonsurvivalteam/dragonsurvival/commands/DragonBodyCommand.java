package by.dragonsurvivalteam.dragonsurvival.commands;


import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonBodyArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.DSCommands;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

public class DragonBodyCommand {
    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dragon-body")
                .requires(sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument(DSCommands.TARGETS, EntityArgument.players())
                        .then(Commands.argument(DragonBodyArgument.ID, new DragonBodyArgument(event.getBuildContext()))
                                .executes(source -> switchBody(source, EntityArgument.getPlayers(source, DSCommands.TARGETS), DragonBodyArgument.get(source)))
                        )
                )
        );
    }

    private static int switchBody(final CommandContext<CommandSourceStack> source, final Collection<ServerPlayer> targets, final Holder<DragonBody> body) {
        int processed = 0;

        for (ServerPlayer target : targets) {
            DragonStateHandler handler = DragonStateProvider.getData(target);

            if (!handler.isDragon() || body == null) {
                continue;
            }

            if (!handler.species().value().isValidForBody(body)) {
                continue;
            }

            handler.setBody(target, body);

            SyncComplete.handleDragonSync(target, false);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, new SyncComplete(target.getId(), handler.serializeNBT(target.registryAccess())));

            processed++;
        }

        int finalProcessed = processed;
        source.getSource().sendSuccess(() -> Component.translatable(DSCommands.PROCESSED, finalProcessed, targets.size()), true);
        return processed;
    }
}
