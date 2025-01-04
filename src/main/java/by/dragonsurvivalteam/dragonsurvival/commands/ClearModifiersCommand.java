package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class ClearModifiersCommand {
    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.getRoot().addChild(literal("clear-modifiers")
                .requires(source -> source.hasPermission(2))
                .executes(context -> runCommand(context.getSource().getPlayerOrException()))
                .build()
        );
    }

    private static int runCommand(final ServerPlayer player) {
        player.getExistingData(DSDataAttachments.MODIFIERS_WITH_DURATION).ifPresent(data -> {
            data.all().forEach(entry -> entry.onRemovalFromStorage(player));
            player.removeData(DSDataAttachments.MODIFIERS_WITH_DURATION);
        });

        player.getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).ifPresent(data -> {
            data.all().forEach(entry -> entry.onRemovalFromStorage(player));
            player.removeData(DSDataAttachments.DAMAGE_MODIFICATIONS);
        });

        player.getExistingData(DSDataAttachments.HARVEST_BONUSES).ifPresent(data -> {
            data.all().forEach(entry -> entry.onRemovalFromStorage(player));
            player.removeData(DSDataAttachments.HARVEST_BONUSES);
        });

        return 1;
    }
}
