package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.commands.ClearMarkCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.ClearModifiersCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonAbilityCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonAltarCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonEditorCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonSizeCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonAbilityArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonBodyArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonSizeArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonSpeciesArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonStageArgument;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;


@EventBusSubscriber
public class DSCommands {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, DragonSurvival.MODID);

    static {
        ARGUMENT_TYPES.register(DragonBodyArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonBodyArgument.class, SingletonArgumentInfo.contextAware(DragonBodyArgument::new)));
        ARGUMENT_TYPES.register(DragonStageArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonStageArgument.class, SingletonArgumentInfo.contextAware(DragonStageArgument::new)));
        ARGUMENT_TYPES.register(DragonSizeArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonSizeArgument.class, SingletonArgumentInfo.contextAware(DragonSizeArgument::new)));
        ARGUMENT_TYPES.register(DragonSpeciesArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonSpeciesArgument.class, SingletonArgumentInfo.contextAware(DragonSpeciesArgument::new)));
        ARGUMENT_TYPES.register(DragonAbilityArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonAbilityArgument.class, SingletonArgumentInfo.contextAware(DragonAbilityArgument::new)));
    }

    @SubscribeEvent
    public static void serverRegisterCommandsEvent(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        DragonAbilityCommand.register(event);
        ClearModifiersCommand.register(dispatcher);
        ClearMarkCommand.register(dispatcher);
        DragonEditorCommand.register(dispatcher);
        DragonAltarCommand.register(dispatcher);

        DragonCommand.register(event);
        DragonSizeCommand.register(event);
    }
}
