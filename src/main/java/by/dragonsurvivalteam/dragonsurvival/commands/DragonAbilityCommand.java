package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonAbilityArgument;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

import static net.minecraft.commands.Commands.argument;

public class DragonAbilityCommand {
    @Translation(comments = "%s abilities cleared from %s entites.")
    private static final String CLEAR_FROM_ENTITES = Translation.Type.COMMAND.wrap("ability.clear_from_entities");

    @Translation(comments = "%s abilities cleared from %s.")
    private static final String CLEAR_FROM_SINGLE_ENTITY = Translation.Type.COMMAND.wrap("ability.clear_from_single_entity");

    @Translation(comments = "Removed %s from %s.")
    private static final String REMOVED_FROM_SINGLE_ENTITY = Translation.Type.COMMAND.wrap("ability.removed_from_single_entity");

    @Translation(comments = "Removed %s from %s entities.")
    private static final String REMOVED_FROM_ENTITES = Translation.Type.COMMAND.wrap("ability.removed_from_entities");

    @Translation(comments = "Added %s to %s.")
    private static final String ADDED_TO_SINGLE_ENTITY = Translation.Type.COMMAND.wrap("ability.added_to_single_entity");

    @Translation(comments = "Added %s to %s entities.")
    private static final String ADDED_TO_ENTITES = Translation.Type.COMMAND.wrap("ability.added_to_entities");

    @Translation(comments = "Refreshed abilities for %s entities.")
    private static final String REFRESHED_ENTITIES = Translation.Type.COMMAND.wrap("ability.refreshed_entities");

    @Translation(comments = "Refreshed abilities for %s.")
    private static final String REFRESHED_SINGLE_ENTITY = Translation.Type.COMMAND.wrap("ability.refreshed_single_entity");


    public static void register(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("dragon-ability")
                        .requires(sourceStack -> sourceStack.hasPermission(2))
                        .then(
                                Commands.literal("remove")
                                        .then(
                                                Commands.argument("targets", EntityArgument.entities())
                                                        .executes(sourceStackCommandContext -> clearAbilities(sourceStackCommandContext.getSource(), EntityArgument.getEntities(sourceStackCommandContext, "targets")))
                                                        .then(
                                                                argument(DragonAbilityArgument.ID, new DragonAbilityArgument(event.getBuildContext()))
                                                                        .executes(sourceStackCommandContext -> removeAbility(sourceStackCommandContext.getSource(), EntityArgument.getEntities(sourceStackCommandContext, "targets"), DragonAbilityArgument.get(sourceStackCommandContext)))
                                                        )
                                                        .then(
                                                                Commands.literal("all")
                                                                        .executes(sourceStackCommandContext -> clearAbilities(sourceStackCommandContext.getSource(), ImmutableList.of(sourceStackCommandContext.getSource().getEntityOrException())))
                                                        )
                        ))
                        .then(
                                Commands.literal("add")
                                        .then(
                                                Commands.argument("targets", EntityArgument.entities())
                                                        .then(
                                                                argument(DragonAbilityArgument.ID, new DragonAbilityArgument(event.getBuildContext()))
                                                                        .executes(sourceStackCommandContext -> addAbility(sourceStackCommandContext.getSource(), EntityArgument.getEntities(sourceStackCommandContext, "targets"), DragonAbilityArgument.get(sourceStackCommandContext)))
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("refresh")
                                          .executes(sourceStackCommandContext -> clearAbilities(sourceStackCommandContext.getSource(), ImmutableList.of(sourceStackCommandContext.getSource().getEntityOrException())))
                                          .then(
                                                 argument("targets", EntityArgument.entities())
                                                          .executes(sourceStackCommandContext -> refreshAbilities(sourceStackCommandContext.getSource(), EntityArgument.getEntities(sourceStackCommandContext, "targets")))
                                           )
                        )
        );
    }

    private static int clearAbilities(CommandSourceStack source, Collection<? extends Entity> targets) {
        int count = 0;
        for (Entity target : targets) {
            if(target instanceof ServerPlayer player) {
                MagicData data = MagicData.getData(player);
                count = data.clear(player);
                PacketDistributor.sendToPlayer(player, new SyncMagicData(data.serializeNBT(player.registryAccess())));
            }
        }

        int finalCount = count;
        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable(CLEAR_FROM_SINGLE_ENTITY, finalCount, targets.stream().findFirst().get().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(CLEAR_FROM_ENTITES, finalCount, targets.size()), true);
        }

        return 1;
    }

    private static int removeAbility(CommandSourceStack source, Collection<? extends Entity> targets, Holder<DragonAbility> ability) {
        for (Entity target : targets) {
            if(target instanceof ServerPlayer player) {
                MagicData data = MagicData.getData(player);
                data.removeAbility(ability.getKey());
                PacketDistributor.sendToPlayer(player, new SyncMagicData(data.serializeNBT(player.registryAccess())));
            }
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable(REMOVED_FROM_SINGLE_ENTITY, Component.translatable(Translation.Type.ABILITY.wrap(ability.getKey().location())), targets.stream().findFirst().get().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(REMOVED_FROM_ENTITES, Component.translatable(Translation.Type.ABILITY.wrap(ability.getKey().location())), targets.size()), true);
        }

        return 1;
    }

    private static int addAbility(CommandSourceStack source, Collection<? extends Entity> targets, Holder<DragonAbility> ability) {
        for (Entity target : targets) {
            if(target instanceof ServerPlayer player) {
                MagicData data = MagicData.getData(player);
                data.addAbility(player, ability);
                PacketDistributor.sendToPlayer(player, new SyncMagicData(data.serializeNBT(player.registryAccess())));
            }
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable(ADDED_TO_SINGLE_ENTITY, Component.translatable(Translation.Type.ABILITY.wrap(ability.getKey().location())), targets.stream().findFirst().get().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(ADDED_TO_ENTITES, Component.translatable(Translation.Type.ABILITY.wrap(ability.getKey().location())), targets.size()), true);
        }

        return 1;
    }

    private static int refreshAbilities(CommandSourceStack source, Collection<? extends Entity> targets) {
        for (Entity target : targets) {
            if(target instanceof ServerPlayer player) {
                MagicData data = MagicData.getData(player);
                data.refresh(player, DragonStateProvider.getData(player).species());
                PacketDistributor.sendToPlayer(player, new SyncMagicData(data.serializeNBT(player.registryAccess())));
            }
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable(REFRESHED_SINGLE_ENTITY, targets.stream().findFirst().get().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(REFRESHED_ENTITIES, targets.size()), true);
        }

        return 1;
    }
}
