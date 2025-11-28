package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonAbilityArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicNCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class DragonAbilityCommand {
    @Translation(comments = "%s of %s players processed (non-dragons are skipped)")
    private static final String PROCESSED = Translation.Type.COMMAND.wrap("ability.processed");

    @Translation(comments = "[%s] does not have the ability [%s]")
    private static final String UNKNOWN_ABILITY = Translation.Type.COMMAND.wrap("ability.unknown");

    @Translation(comments = "%s of the ability %s from player %s has the value %s")
    private static final String QUERY_RESULT = Translation.Type.COMMAND.wrap("ability.query_result");

    private static final DynamicNCommandExceptionType UNKNOWN_ABILITY_EXCEPTION = new DynamicNCommandExceptionType(data -> Component.translatable(UNKNOWN_ABILITY, data));

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
                .then(Commands.literal("query")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument(DragonAbilityArgument.ID, new DragonAbilityArgument(event.getBuildContext()))
                                        .then(Commands.literal("level")
                                                .executes(source -> query(source, EntityArgument.getPlayer(source, "target"), DragonAbilityArgument.get(source), DragonAbilityInstance::level))
                                        )
                                        .then(Commands.literal("max_level")
                                                .executes(source -> query(source, EntityArgument.getPlayer(source, "target"), DragonAbilityArgument.get(source), DragonAbilityInstance::getMaxLevel))
                                        )
                                        .then(Commands.literal("current_cooldown")
                                                .executes(source -> query(source, EntityArgument.getPlayer(source, "target"), DragonAbilityArgument.get(source), DragonAbilityInstance::cooldown))
                                        )
                                        .then(Commands.literal("current_tick")
                                                .executes(source -> query(source, EntityArgument.getPlayer(source, "target"), DragonAbilityArgument.get(source), DragonAbilityInstance::getCurrentTick))
                                        )
                                        .then(Commands.literal("cooldown")
                                                .executes(source -> query(source, EntityArgument.getPlayer(source, "target"), DragonAbilityArgument.get(source), instance -> instance.value().activation().getCooldown(instance.level())))
                                        )
                                        .then(Commands.literal("cast_time")
                                                .executes(source -> query(source, EntityArgument.getPlayer(source, "target"), DragonAbilityArgument.get(source), instance -> instance.value().activation().getCastTime(instance.level())))
                                        )
                                        .then(Commands.literal("is_applying_effects")
                                                .executes(source -> query(source, EntityArgument.getPlayer(source, "target"), DragonAbilityArgument.get(source), instance -> instance.isApplyingEffects() ? 1 : 0))
                                        )
                                        .then(Commands.literal("is_enabled")
                                                .executes(source -> query(source, EntityArgument.getPlayer(source, "target"), DragonAbilityArgument.get(source), instance -> instance.isEnabled() ? 1 : 0))
                                        )
                                )
                        )
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

        return processed;
    }

    private static int query(final CommandContext<CommandSourceStack> source, final Player player, final Holder<DragonAbility> ability, final Function<DragonAbilityInstance, Integer> query) throws CommandSyntaxException {
        if (!DragonStateProvider.isDragon(player)) {
            // There is some weird 'a' parameter that is unused - the arguments (array) are specified after that
            throw UNKNOWN_ABILITY_EXCEPTION.create(null, player.getDisplayName(), ability.getRegisteredName());
        }

        MagicData data = MagicData.getData(player);
        DragonAbilityInstance instance = data.getAbility(ability.getKey());

        if (instance == null) {
            throw UNKNOWN_ABILITY_EXCEPTION.create(null, player.getDisplayName(), ability.getRegisteredName());
        }

        Integer result = query.apply(instance);

        source.getSource().sendSuccess(() -> Component.translatable(
                QUERY_RESULT,
                DSColors.withColor(source.getNodes().getLast().getNode().getName(), DSColors.GOLD),
                DSColors.withColor(ability.getRegisteredName(), DSColors.GOLD),
                DSColors.withColor(player.getDisplayName(), DSColors.GOLD),
                DSColors.withColor(result, DSColors.GOLD)
        ), false);

        return result;
    }
}
