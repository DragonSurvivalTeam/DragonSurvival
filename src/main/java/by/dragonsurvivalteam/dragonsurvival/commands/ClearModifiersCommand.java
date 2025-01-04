package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HarvestBonuses;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class ClearModifiersCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        RootCommandNode<CommandSourceStack> rootCommandNode = commandDispatcher.getRoot();
        LiteralCommandNode<CommandSourceStack> dragon = literal("clear-modifiers").requires(commandSource -> commandSource.hasPermission(2)).executes(context -> runCommand(context.getSource().getPlayerOrException())).build();
        rootCommandNode.addChild(dragon);
    }

    private static int runCommand(ServerPlayer serverPlayer) {
        ModifiersWithDuration modifiersWithDuration = ModifiersWithDuration.getData(serverPlayer);
        DamageModifications damageModifications = DamageModifications.getData(serverPlayer);
        HarvestBonuses harvestBonuses = HarvestBonuses.getData(serverPlayer);
        harvestBonuses.all().forEach(entry -> entry.onRemovalFromStorage(serverPlayer));
        damageModifications.all().forEach(entry -> entry.onRemovalFromStorage(serverPlayer));
        modifiersWithDuration.all().forEach(entry -> entry.onRemovalFromStorage(serverPlayer));
        return 1;
    }
}
