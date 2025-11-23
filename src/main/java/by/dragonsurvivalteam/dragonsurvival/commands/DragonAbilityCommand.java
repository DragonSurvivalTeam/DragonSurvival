package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonAbilityArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.function.BiPredicate;

public class DragonAbilityCommand {
    @Translation(comments = "%s of %s players processed (non-dragons are skipped)")
    private static final String PROCESSED = Translation.Type.COMMAND.wrap("ability.processed");

    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dragon-ability")
                .requires(sourceStack -> sourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("remove")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(source -> handleCommand(source, EntityArgument.getPlayers(source, "targets"), (player, data) -> data.clear(player) > 0))
                                .then(Commands.argument(DragonAbilityArgument.ID, new DragonAbilityArgument(event.getBuildContext()))
                                        .executes(source -> handleCommand(source, EntityArgument.getPlayers(source, "targets"), (player, data) -> data.removeAbility(player, DragonAbilityArgument.get(source).getKey())))
                                )))
                .then(Commands.literal("add")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument(DragonAbilityArgument.ID, new DragonAbilityArgument(event.getBuildContext()))
                                        .executes(source -> handleCommand(source, EntityArgument.getPlayers(source, "targets"), (player, data) -> data.addAbility(player, DragonAbilityArgument.get(source))))
                                )
                        )
                )
                .then(Commands.literal("refresh")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(source -> handleCommand(source, EntityArgument.getPlayers(source, "targets"), (player, data) -> {
                                    data.refresh(player, DragonStateProvider.getData(player).species());
                                    return true;
                                })))
                )
        );
    }

    private static int handleCommand(final CommandContext<CommandSourceStack> source, final Collection<? extends Player> targets, final BiPredicate<ServerPlayer, MagicData> logic) {
        int processed = 0;

        for (Player target : targets) {
            if (target instanceof ServerPlayer player) {
                DragonStateHandler handler = DragonStateProvider.getData(player);

                if (!handler.isDragon()) {
                    continue;
                }

                MagicData data = MagicData.getData(player);

                if (logic.test(player, data)) {
                    processed++;
                }

                PacketDistributor.sendToPlayer(player, new SyncMagicData(data.serializeNBT(target.registryAccess())));
            }
        }

        int finalProcessed = processed;
        source.getSource().sendSuccess(() -> Component.translatable(PROCESSED, finalProcessed, targets.size()), true);

        return 1;
    }
}
