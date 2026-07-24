package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.registry.DSCommands;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ClearModifiersCommand {
    @Translation(comments = "%s modifiers cleared from %s entities.")
    private static final String FROM_ENTITES = Translation.Type.COMMAND.wrap("clear_modifiers.from_entities");

    @Translation(comments = "%s modifiers cleared from %s.")
    private static final String FROM_SINGLE_ENTITY = Translation.Type.COMMAND.wrap("clear_modifiers.from_single_entity");

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("dragon-modifiers")
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("clear")
                        .executes(source -> clearModifiers(source.getSource(), ImmutableList.of(source.getSource().getEntityOrException())))
                        .then(argument(DSCommands.TARGETS, EntityArgument.entities())
                                .executes(source -> clearModifiers(source.getSource(), EntityArgument.getEntities(source, DSCommands.TARGETS)))
                        )
                )
        );
    }

    private static int clearModifiers(final CommandSourceStack source, final Collection<? extends Entity> targets) {
        AtomicInteger totalRemoved = new AtomicInteger();

        for (Entity target : targets) {
            DSDataAttachments.getStorages(target).forEach(storage -> {
                storage.clear(target);
                totalRemoved.getAndIncrement();
            });
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable(FROM_SINGLE_ENTITY, totalRemoved.get(), targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(FROM_ENTITES, totalRemoved.get(), targets.size()), true);
        }

        return targets.size();
    }
}
