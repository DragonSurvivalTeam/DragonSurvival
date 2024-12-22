package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonBodyArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonStageArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonTypeArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DragonCommand {
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();

        RootCommandNode<CommandSourceStack> rootCommandNode = commandDispatcher.getRoot();
        LiteralCommandNode<CommandSourceStack> dragon = literal("dragon").requires(commandSource -> commandSource.hasPermission(2)).executes(context -> {
            Holder<DragonType> type = DragonTypeArgument.get(context);
            return runCommand(type, null, null, context.getSource().getPlayerOrException());
        }).build();

        ArgumentCommandNode<CommandSourceStack, Holder<DragonType>> dragonType = argument(DragonTypeArgument.ID, new DragonTypeArgument(event.getBuildContext())).executes(context -> {
            Holder<DragonType> type = DragonTypeArgument.get(context);
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            return runCommand(type, null, null, serverPlayer);
        }).build();

        ArgumentCommandNode<CommandSourceStack, Holder<DragonBody>> dragonBody = argument(DragonBodyArgument.ID, new DragonBodyArgument(event.getBuildContext())).executes(context -> {
            Holder<DragonType> type = DragonTypeArgument.get(context);
            Holder<DragonBody> body = DragonBodyArgument.get(context);
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            return runCommand(type, body, null, serverPlayer);
        }).build();

        ArgumentCommandNode<CommandSourceStack, Holder<DragonStage>> dragonStage = argument(DragonStageArgument.ID, new DragonStageArgument(event.getBuildContext())).executes(context -> {
            Holder<DragonType> type = DragonTypeArgument.get(context);
            Holder<DragonBody> body = DragonBodyArgument.get(context);
            Holder<DragonStage> level = DragonStageArgument.get(context);
            ServerPlayer serverPlayer = context.getSource().getPlayerOrException();
            return runCommand(type, body, level, serverPlayer);
        }).build();

        ArgumentCommandNode<CommandSourceStack, EntitySelector> target = argument("target", EntityArgument.players()).executes(context -> {
            Holder<DragonType> type = DragonTypeArgument.get(context);
            Holder<DragonBody> body = DragonBodyArgument.get(context);
            Holder<DragonStage> level = DragonStageArgument.get(context);
            EntitySelector selector = context.getArgument("target", EntitySelector.class);
            List<ServerPlayer> serverPlayers = selector.findPlayers(context.getSource());
            serverPlayers.forEach(player -> runCommand(type, body, level, player));
            return 1;
        }).build();

        rootCommandNode.addChild(dragon);
        dragon.addChild(dragonType);
        dragonType.addChild(dragonBody);
        dragonBody.addChild(dragonStage);
        dragonStage.addChild(target);
    }

    private static int runCommand(Holder<DragonType> type, @Nullable Holder<DragonBody> dragonBody, @Nullable Holder<DragonStage> dragonStage, ServerPlayer player) {
        DragonStateHandler data = DragonStateProvider.getData(player);

        if (type != null && dragonBody == null) {
            dragonBody = DragonBody.random(player.registryAccess());
        }

        if (type != null && dragonStage == null) {
            dragonStage = player.registryAccess().holderOrThrow(DragonStages.newborn);
        }

        if (type == null && data.getType() != null) {
            reInsertClawTools(player);
        }

        data.setType(type, player);
        data.setBody(dragonBody, player);

        if (dragonStage != null) {
            data.setStage(player, dragonStage);
        } else {
            data.setSize(player, DragonStateHandler.NO_SIZE);
        }

        data.setPassengerId(-1);
        data.isGrowing = true;

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncComplete.Data(player.getId(), data.serializeNBT(player.registryAccess())));
        player.refreshDimensions();
        return 1;
    }

    public static void reInsertClawTools(Player player) {
        SimpleContainer clawsContainer = ClawInventoryData.getData(player).getContainer();
        for (int i = 0; i < 4; i++) {
            ItemStack stack = clawsContainer.getItem(i);

            if (player instanceof ServerPlayer serverPlayer) {
                if (!serverPlayer.addItem(stack)) {
                    serverPlayer.level().addFreshEntity(new ItemEntity(serverPlayer.level(), serverPlayer.position().x, serverPlayer.position().y, serverPlayer.position().z, stack));
                }
            }
        }

        clawsContainer.clearContent();
    }
}