package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonBodyArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonSpeciesArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonStageArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.DSCommands;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonRidingHandler;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.PlayerLoginHandler;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;


public class DragonCommand {
    @Translation(comments = "There are no available (unlocked) species present")
    private static final String NO_UNLOCKED_SPECIES = Translation.Type.GUI.wrap("message.no_unlocked_species");

    public static void register(final RegisterCommandsEvent event) {
        LiteralCommandNode<CommandSourceStack> dragon = Commands.literal("dragon").requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            Holder<DragonSpecies> species = DragonSpecies.getRandom(player);

            if (species == null) {
                context.getSource().sendFailure(Component.translatable(NO_UNLOCKED_SPECIES));
                return 0;
            }

            return runCommand(species, null, null, player);
        }).build();

        ArgumentCommandNode<CommandSourceStack, Holder<DragonSpecies>> dragonSpecies = Commands.argument(DragonSpeciesArgument.ID, new DragonSpeciesArgument(event.getBuildContext())).executes(context -> {
            Holder<DragonSpecies> species = DragonSpeciesArgument.get(context);
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            return runCommand(species, null, null, serverPlayer);
        }).build();

        ArgumentCommandNode<CommandSourceStack, Holder<DragonBody>> dragonBody = Commands.argument(DragonBodyArgument.ID, new DragonBodyArgument(event.getBuildContext())).executes(context -> {
            Holder<DragonSpecies> species = DragonSpeciesArgument.get(context);
            Holder<DragonBody> body = DragonBodyArgument.get(context);
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            return runCommand(species, body, null, serverPlayer);
        }).build();

        ArgumentCommandNode<CommandSourceStack, Holder<DragonStage>> dragonStage = Commands.argument(DragonStageArgument.ID, new DragonStageArgument(event.getBuildContext())).executes(context -> {
            Holder<DragonSpecies> species = DragonSpeciesArgument.get(context);
            Holder<DragonBody> body = DragonBodyArgument.get(context);
            Holder<DragonStage> level = DragonStageArgument.get(context);
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            return runCommand(species, body, level, serverPlayer);
        }).build();

        ArgumentCommandNode<CommandSourceStack, EntitySelector> target = Commands.argument(DSCommands.TARGETS, EntityArgument.players()).executes(context -> {
            Holder<DragonSpecies> species = DragonSpeciesArgument.get(context);
            Holder<DragonBody> body = DragonBodyArgument.get(context);
            Holder<DragonStage> level = DragonStageArgument.get(context);
            EntityArgument.getPlayers(context, DSCommands.TARGETS).forEach(player -> runCommand(species, body, level, player));
            return 1;
        }).build();

        event.getDispatcher().getRoot().addChild(dragon);
        dragon.addChild(dragonSpecies);
        dragonSpecies.addChild(dragonBody);
        dragonBody.addChild(dragonStage);
        dragonStage.addChild(target);
    }

    private static int runCommand(final Holder<DragonSpecies> species, @Nullable final Holder<DragonBody> dragonBody, @Nullable final Holder<DragonStage> dragonStage, final ServerPlayer player) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        boolean wasDragon = handler.isDragon();

        if (wasDragon && species.value() == DragonSpeciesArgument.EMPTY) {
            handler.revertToHumanForm(player, false);
            PlayerLoginHandler.syncHandler(player);
            return 1;
        } else if (species.value() == DragonSpeciesArgument.EMPTY) {
            return 0;
        }

        handler.setSpecies(player, species);
        handler.setBody(player, dragonBody == null ? DragonBody.getRandomUnlocked(player) : dragonBody);

        // Need to use 'setSize' since the desired size call doesn't set the stage
        if (dragonStage == null) {
            handler.setGrowth(player, species.value().getStartingGrowth(player.registryAccess()));
        } else {
            handler.setStage(player, dragonStage);
        }

        handler.setPassengerId(DragonRidingHandler.NO_PASSENGER);
        handler.isGrowing = true;

        SyncComplete.handleDragonSync(player, false);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncComplete(player.getId(), handler.serializeNBT(player.registryAccess())));
        return 1;
    }
}