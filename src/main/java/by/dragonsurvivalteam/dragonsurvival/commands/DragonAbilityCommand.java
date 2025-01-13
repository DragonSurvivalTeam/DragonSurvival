package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonAbilityArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import com.google.common.collect.ImmutableList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;

public class DragonAbilityCommand {
    @Translation(comments = "%s abilities cleared from %s players.")
    private static final String CLEAR_FROM_PLAYERS = Translation.Type.COMMAND.wrap("ability.clear_from_players");

    @Translation(comments = "%s abilities cleared from %s.")
    private static final String CLEAR_FROM_SINGLE_PLAYER = Translation.Type.COMMAND.wrap("ability.clear_from_single_player");

    @Translation(comments = "Removed %s from %s.")
    private static final String REMOVED_FROM_SINGLE_PLAYER = Translation.Type.COMMAND.wrap("ability.removed_from_single_player");

    @Translation(comments = "Removed %s from %s players.")
    private static final String REMOVED_FROM_PLAYERS = Translation.Type.COMMAND.wrap("ability.removed_from_players");

    @Translation(comments = "Added %s to %s.")
    private static final String ADDED_TO_SINGLE_PLAYER = Translation.Type.COMMAND.wrap("ability.added_to_single_player");

    @Translation(comments = "Added %s to %s players.")
    private static final String ADDED_TO_PLAYERS = Translation.Type.COMMAND.wrap("ability.added_to_players");

    @Translation(comments = "Refreshed abilities for %s players.")
    private static final String REFRESHED_PLAYERS = Translation.Type.COMMAND.wrap("ability.refreshed_players");

    @Translation(comments = "Refreshed abilities for %s.")
    private static final String REFRESHED_SINGLE_PLAYER = Translation.Type.COMMAND.wrap("ability.refreshed_single_player");

    @Translation(comments = "Failed to use ability command on non-dragon player %s.")
    private static final String FAILED_NON_DRAGON = Translation.Type.COMMAND.wrap("ability.failed_non_dragon");


    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dragon-ability")
                .requires(sourceStack -> sourceStack.hasPermission(2))
                .then(Commands.literal("remove")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(source -> clearAbilities(source.getSource(), EntityArgument.getPlayers(source, "targets")))
                                .then(argument(DragonAbilityArgument.ID, new DragonAbilityArgument(event.getBuildContext()))
                                        .executes(source -> removeAbility(source.getSource(), EntityArgument.getPlayers(source, "targets"), DragonAbilityArgument.get(source)))
                                )
                                .then(Commands.literal("all")
                                        .executes(source -> clearAbilities(source.getSource(), ImmutableList.of(source.getSource().getPlayerOrException())))
                                )
                        ))
                .then(Commands.literal("add")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(argument(DragonAbilityArgument.ID, new DragonAbilityArgument(event.getBuildContext()))
                                        .executes(source -> addAbility(source.getSource(), EntityArgument.getPlayers(source, "targets"), DragonAbilityArgument.get(source)))
                                )
                        )
                )
                .then(Commands.literal("refresh")
                        .executes(source -> refreshAbilities(source.getSource(), ImmutableList.of(source.getSource().getPlayerOrException())))
                        .then(argument("targets", EntityArgument.players())
                                .executes(source -> refreshAbilities(source.getSource(), EntityArgument.getPlayers(source, "targets")))
                        )
                )
        );
    }

    private static int clearAbilities(final CommandSourceStack source, final Collection<? extends Player> targets) {
        int count = 0;
        int numFailures = 0;
        for (Player target : targets) {
            if (target instanceof ServerPlayer player) {
                DragonStateHandler handler = DragonStateProvider.getData(player);
                if(!handler.isDragon()) {
                    source.sendFailure(Component.translatable(FAILED_NON_DRAGON, target.getDisplayName()));
                    numFailures++;
                    continue;
                }
                MagicData data = MagicData.getData(target);
                count += data.clear(player);
                PacketDistributor.sendToPlayer(player, new SyncMagicData(data.serializeNBT(target.registryAccess())));
            }
        }

        int finalCount = count;

        if (targets.size() == 1 && numFailures == 0) {
            source.sendSuccess(() -> Component.translatable(CLEAR_FROM_SINGLE_PLAYER, finalCount, targets.iterator().next().getDisplayName()), true);
        } else if(numFailures < targets.size()) {
            source.sendSuccess(() -> Component.translatable(CLEAR_FROM_PLAYERS, finalCount, targets.size()), true);
        }

        return 1;
    }

    private static int removeAbility(final CommandSourceStack source, final Collection<? extends Player> targets, final Holder<DragonAbility> ability) {
        int numFailures = 0;
        for (Player target : targets) {
            if (target instanceof ServerPlayer player) {
                DragonStateHandler handler = DragonStateProvider.getData(player);
                if(!handler.isDragon()) {
                    source.sendFailure(Component.translatable(FAILED_NON_DRAGON, target.getDisplayName()));
                    numFailures++;
                    continue;
                }
                MagicData data = MagicData.getData(player);
                data.removeAbility(ability.getKey());
                PacketDistributor.sendToPlayer(player, new SyncMagicData(data.serializeNBT(player.registryAccess())));
            }
        }

        //noinspection DataFlowIssue -> key is present
        MutableComponent abilityTranslation = Component.translatable(Translation.Type.ABILITY.wrap(ability.getKey().location()));

        if (targets.size() == 1 && numFailures == 0) {
            source.sendSuccess(() -> Component.translatable(REMOVED_FROM_SINGLE_PLAYER, abilityTranslation, targets.iterator().next().getDisplayName()), true);
        } else if(numFailures < targets.size()) {
            source.sendSuccess(() -> Component.translatable(REMOVED_FROM_PLAYERS, abilityTranslation, targets.size()), true);
        }

        return 1;
    }

    private static int addAbility(final CommandSourceStack source, final Collection<? extends Player> targets, final Holder<DragonAbility> ability) {
        int numFailures = 0;
        for (Player target : targets) {
            if (target instanceof ServerPlayer player) {
                DragonStateHandler handler = DragonStateProvider.getData(player);
                if(!handler.isDragon()) {
                    source.sendFailure(Component.translatable(FAILED_NON_DRAGON, target.getDisplayName()));
                    numFailures++;
                    continue;
                }
                MagicData data = MagicData.getData(player);
                data.addAbility(player, ability);
                PacketDistributor.sendToPlayer(player, new SyncMagicData(data.serializeNBT(player.registryAccess())));
            }
        }

        //noinspection DataFlowIssue -> key is present
        MutableComponent abilityTranslation = Component.translatable(Translation.Type.ABILITY.wrap(ability.getKey().location()));

        if (targets.size() == 1 && numFailures == 0) {
            source.sendSuccess(() -> Component.translatable(ADDED_TO_SINGLE_PLAYER, abilityTranslation, targets.iterator().next().getDisplayName()), true);
        } else if(numFailures < targets.size()) {
            source.sendSuccess(() -> Component.translatable(ADDED_TO_PLAYERS, abilityTranslation, targets.size()), true);
        }

        return 1;
    }

    private static int refreshAbilities(final CommandSourceStack source, final Collection<? extends Player> targets) {
        int numFailures = 0;
        for (Player target : targets) {
            if (target instanceof ServerPlayer player) {
                DragonStateHandler handler = DragonStateProvider.getData(player);
                if(!handler.isDragon()) {
                    source.sendFailure(Component.translatable(FAILED_NON_DRAGON, target.getDisplayName()));
                    numFailures++;
                    continue;
                }
                MagicData data = MagicData.getData(player);
                data.refresh(player, DragonStateProvider.getData(player).species());
                PacketDistributor.sendToPlayer(player, new SyncMagicData(data.serializeNBT(player.registryAccess())));
            }
        }

        if (targets.size() == 1 && numFailures == 0) {
            source.sendSuccess(() -> Component.translatable(REFRESHED_SINGLE_PLAYER, targets.iterator().next().getDisplayName()), true);
        } else if(numFailures < targets.size()) {
            source.sendSuccess(() -> Component.translatable(REFRESHED_PLAYERS, targets.size()), true);
        }

        return 1;
    }
}
