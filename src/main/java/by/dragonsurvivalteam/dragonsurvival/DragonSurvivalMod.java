package by.dragonsurvivalteam.dragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.api.appleskin.AppleSkinEventHandler;
import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.SoundRegistry;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonAltarCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonEditorCommand;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonSizeCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.WingObtainmentController;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler.Event_busHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.data.loot.DragonHeartLootModifierSerializer;
import by.dragonsurvivalteam.dragonsurvival.data.loot.DragonOreLootModifierSerializer;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSCreativeTabs;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.resource.PathPackResources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod( DragonSurvivalMod.MODID )
public class DragonSurvivalMod{
	public static final String MODID = "dragonsurvival";
	public static final Logger LOGGER = LogManager.getLogger("Dragon Survival");
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MODID, name);
    }

	public DragonSurvivalMod(){
		GeckoLib.initialize();
		DragonTypes.registerTypes();
		DragonBodies.registerBodies();

		ConfigHandler.initConfig();
		DragonAbilities.initAbilities();

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::addPackFinders);

		DSParticles.register();
		SoundRegistry.register();
		// TODO :: Move to clientSetup?
		DSParticles.REGISTRY.register(modEventBus);
		SoundRegistry.SOUNDS.register(modEventBus);
		DSEntities.ENTITY_TYPES.register(modEventBus);
		DSCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
		GLM.register(FMLJavaModLoadingContext.get().getModEventBus());


		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new DragonFoodHandler());
		MinecraftForge.EVENT_BUS.register(new Event_busHandler());

		MinecraftForge.EVENT_BUS.addListener(this::serverRegisterCommandsEvent);
	}

	private void commonSetup(final FMLCommonSetupEvent event){
		NetworkHandler.setup();
		LOGGER.info("Successfully registered packets!");
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		if (ModList.get().isLoaded("appleskin")) {
			MinecraftForge.EVENT_BUS.register(new AppleSkinEventHandler());
		}
		WingObtainmentController.loadDragonPhrases();
	}

	@SubscribeEvent
	public void serverRegisterCommandsEvent(RegisterCommandsEvent event){
		CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
		DragonCommand.register(commandDispatcher);
		DragonEditorCommand.register(commandDispatcher);
		DragonAltarCommand.register(commandDispatcher);
		DragonSizeCommand.register(commandDispatcher);
		LOGGER.info("Registered commands");
	}
	
	@SubscribeEvent
	public void addPackFinders(AddPackFindersEvent event) {
		if (event.getPackType() == PackType.CLIENT_RESOURCES) {
			HashMap<MutableComponent, String> resourcePacks = new HashMap<MutableComponent, String>();
			//resourcePacks.put(Component.literal("- Dragon East"), "ds_east");
			//resourcePacks.put(Component.literal("- Dragon North"), "ds_north");
			//resourcePacks.put(Component.literal("- Dragon South"), "ds_south");
			//resourcePacks.put(Component.literal("- Dragon West"), "ds_west");
			resourcePacks.put(Component.literal("- Old Magic Icons for DS"), "ds_old_magic");
			resourcePacks.put(Component.literal("- Dark GUI for DS"), "ds_dark_gui");
			for (Map.Entry<MutableComponent, String> entry : resourcePacks.entrySet()) {
				registerBuiltinResourcePack(event, entry.getKey(), entry.getValue());
			}
		}
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class EventHandlers {
		@SubscribeEvent
		public static void runData(GatherDataEvent event) {
			event.getGenerator().addProvider(event.includeServer(), new DragonOreLootModifierSerializer(event.getGenerator().getPackOutput(), MODID));
			event.getGenerator().addProvider(event.includeServer(), new DragonHeartLootModifierSerializer(event.getGenerator().getPackOutput(), MODID));
		}
	}

	private static void registerBuiltinResourcePack(AddPackFindersEvent event, MutableComponent name, String folder) {
		event.addRepositorySource((constructor) -> {
			String path = res(folder).toString();
			IModFile file = ModList.get().getModFileById(MODID).getFile();
			try(PathPackResources pack = new PathPackResources(path, true, file.findResource("resourcepacks/" + folder))) {
				constructor.accept(Pack.create(
					res(folder).toString(),
					name,
					false,
					(none) -> pack,
                        Objects.requireNonNull(Pack.readPackInfo(MODID, (pId) -> pack)),
					PackType.CLIENT_RESOURCES,
					Pack.Position.TOP,
					false,
					PackSource.BUILT_IN)
				);
			}
        });
	}

	@SubscribeEvent
	public static void register(final RegisterCapabilitiesEvent event){
		event.register(DragonStateHandler.class);
		event.register(EntityStateHandler.class);
	}
}