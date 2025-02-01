package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.commands.ClearMarkCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.ClearModifiersCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonAbilityCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonAltarCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonEditorCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonGrowthCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonAbilityArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonBodyArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonGrowthArgument;
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
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, DragonSurvival.MODID);

    static {
        REGISTRY.register(DragonBodyArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonBodyArgument.class, SingletonArgumentInfo.contextAware(DragonBodyArgument::new)));
        REGISTRY.register(DragonStageArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonStageArgument.class, SingletonArgumentInfo.contextAware(DragonStageArgument::new)));
        REGISTRY.register(DragonGrowthArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonGrowthArgument.class, SingletonArgumentInfo.contextAware(DragonGrowthArgument::new)));
        REGISTRY.register(DragonSpeciesArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonSpeciesArgument.class, SingletonArgumentInfo.contextAware(DragonSpeciesArgument::new)));
        REGISTRY.register(DragonAbilityArgument.ID, () -> ArgumentTypeInfos.registerByClass(DragonAbilityArgument.class, SingletonArgumentInfo.contextAware(DragonAbilityArgument::new)));
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
        DragonGrowthCommand.register(event);
    }
}
