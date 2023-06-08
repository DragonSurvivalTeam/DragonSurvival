package by.dragonsurvivalteam.dragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.api.AppleSkinEventHandler;
import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.SoundRegistry;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonAltarCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonEditorCommand;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.WingObtainmentController;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler.Event_busHandler;
import by.dragonsurvivalteam.dragonsurvival.common.items.DragonSurvivalCreativeTab;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

@Mod( DragonSurvivalMod.MODID )
public class DragonSurvivalMod{
	public static final String MODID = "dragonsurvival";
	public static final Logger LOGGER = LogManager.getLogger("Dragon Survival");
	public static DragonSurvivalCreativeTab items = new DragonSurvivalCreativeTab("dragon.survival.blocks");

	public DragonSurvivalMod(){
		GeckoLib.initialize();
		DragonTypes.registerTypes();

		ConfigHandler.initConfig();
		DragonAbilities.initAbilities();

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::setup);

		DSParticles.register();
		SoundRegistry.register();
		DSParticles.REGISTRY.register(modEventBus);
		SoundRegistry.SOUNDS.register(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new DragonFoodHandler());
		MinecraftForge.EVENT_BUS.register(new Event_busHandler());

		if (ModList.get().isLoaded("appleskin")) {
			MinecraftForge.EVENT_BUS.register(new AppleSkinEventHandler());
		}

		MinecraftForge.EVENT_BUS.addListener(this::serverRegisterCommandsEvent);
	}

	private void setup(final FMLCommonSetupEvent event){
		WingObtainmentController.loadDragonPhrases();
		NetworkHandler.setup();
		LOGGER.info("Successfully registered packets!");
	}

	@SubscribeEvent
	public void serverRegisterCommandsEvent(RegisterCommandsEvent event){
		CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
		DragonCommand.register(commandDispatcher);
		DragonEditorCommand.register(commandDispatcher);
		DragonAltarCommand.register(commandDispatcher);
		LOGGER.info("Registered commands");
	}
}