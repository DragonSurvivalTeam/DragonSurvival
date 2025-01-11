package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

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
                .requires(commandSourceStack -> commandSourceStack.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(literal("clear")
                        .executes(commandSourceStack -> clearModifiers(commandSourceStack.getSource(), ImmutableList.of(commandSourceStack.getSource().getEntityOrException())))
                        .then(argument("targets", EntityArgument.entities())
                                .executes(commandSourceStack -> clearModifiers(commandSourceStack.getSource(), EntityArgument.getEntities(commandSourceStack, "targets")))
                        )
                )
        );
    }

    private static int clearModifiers(final CommandSourceStack source, final Collection<? extends Entity> targets) {
        AtomicInteger totalRemoved = new AtomicInteger();

        for (Entity target : targets) {
            if (target instanceof LivingEntity) {
                target.getExistingData(DSDataAttachments.MODIFIERS_WITH_DURATION).ifPresent(data -> {
                    data.all().forEach(entry -> entry.onRemovalFromStorage(target));
                    target.removeData(DSDataAttachments.MODIFIERS_WITH_DURATION);
                    totalRemoved.getAndIncrement();
                });

                target.getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).ifPresent(data -> {
                    data.all().forEach(entry -> entry.onRemovalFromStorage(target));
                    target.removeData(DSDataAttachments.DAMAGE_MODIFICATIONS);
                    totalRemoved.getAndIncrement();
                });

                target.getExistingData(DSDataAttachments.HARVEST_BONUSES).ifPresent(data -> {
                    data.all().forEach(entry -> entry.onRemovalFromStorage(target));
                    target.removeData(DSDataAttachments.HARVEST_BONUSES);
                    totalRemoved.getAndIncrement();
                });
            }
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable(FROM_SINGLE_ENTITY, totalRemoved.get(), targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(FROM_ENTITES, totalRemoved.get(), targets.size()), true);
        }

        return 1;
    }
}
