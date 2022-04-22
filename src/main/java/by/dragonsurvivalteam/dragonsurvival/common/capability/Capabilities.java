package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.common.EffectInstance2;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.GenericCapabilityProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.VillageRelationshipsProvider;
import by.dragonsurvivalteam.dragonsurvival.common.util.DragonModifiers;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.CompleteDataSync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber( modid = DragonSurvivalMod.MODID, bus = Bus.FORGE )
public class Capabilities{
	public static Capability<VillageRelationShips> VILLAGE_RELATIONSHIP = CapabilityManager.get(new CapabilityToken<>(){
	});
	public static Capability<GenericCapability> GENERIC_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){
	});
	public static Capability<DragonStateHandler> DRAGON_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){
	});

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event){
		GenericCapabilityProvider genericCapabilityProvider = new GenericCapabilityProvider();
		event.addCapability(new ResourceLocation("dragonsurvival", "generic_capability_data"), genericCapabilityProvider);
		event.addListener(genericCapabilityProvider::invalidate);

		if(event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer)){
			DragonStateProvider provider = new DragonStateProvider();
			event.addCapability(new ResourceLocation("dragonsurvival", "playerstatehandler"), provider);
			event.addListener(provider::invalidate);

			VillageRelationshipsProvider villageRelationshipsProvider = new VillageRelationshipsProvider();
			event.addCapability(new ResourceLocation("dragonsurvival", "village_relations"), villageRelationshipsProvider);
			event.addListener(villageRelationshipsProvider::invalidate);
		}
	}

	private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();


	//TODO Find a better solution to fix the error of data being synced too early on LAN
	@SubscribeEvent
	public static void register(RegisterCapabilitiesEvent ev){
		ev.register(DragonStateHandler.class);
		ev.register(VillageRelationShips.class);
		ev.register(GenericCapability.class);
	}

	@SubscribeEvent
	public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent loggedInEvent){
		EXECUTOR_SERVICE.schedule(() -> {
			Player player = loggedInEvent.getPlayer();
			if(!player.level.isClientSide){
				DragonStateProvider.getCap(player).ifPresent(cap -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new RequestClientData(cap.getType(), cap.getLevel())));
				syncCapability(player);
			}
		}, 1, TimeUnit.SECONDS);
	}

	public static void syncCapability(Player player){
		NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new CompleteDataSync(player));
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent playerRespawnEvent){
		Player player = playerRespawnEvent.getPlayer();
		if(!player.level.isClientSide){
			syncCapability(player);
		}
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event){
		Player player = event.getPlayer();
		if(!player.level.isClientSide()){
			syncCapability(player);
		}
	}

	@SubscribeEvent
	public static void onTrackingStart(PlayerEvent.StartTracking startTracking){
		Player trackingPlayer = startTracking.getPlayer();
		if(trackingPlayer instanceof ServerPlayer){
			Entity tracked = startTracking.getTarget();
			if(tracked instanceof ServerPlayer){
				DragonStateProvider.getCap(tracked).ifPresent(dragonStateHandler -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)trackingPlayer), new CompleteDataSync(tracked.getId(), dragonStateHandler.writeNBT())));
			}
		}
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone e){
		Player player = e.getPlayer();
		Player original = e.getOriginal();
		if(!e.isWasDeath()){
			return;
		}
		original.revive();

		DragonStateProvider.getCap(player).ifPresent(capNew -> DragonStateProvider.getCap(original).ifPresent(capOld -> {
			CompoundTag nbt = capOld.writeNBT();
			capNew.readNBT(nbt);
			NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new CompleteDataSync(player.getId(), nbt));
		}));

		VillageRelationshipsProvider.getVillageRelationships(player).ifPresent(villageRelationShips -> {
			VillageRelationshipsProvider.getVillageRelationships(original).ifPresent(old -> {
				villageRelationShips.readNBT(old.writeNBT());
				if(ConfigHandler.COMMON.preserveEvilDragonEffectAfterDeath.get() && villageRelationShips.evilStatusDuration > 0){
					player.addEffect(new EffectInstance2(DragonEffects.EVIL_DRAGON, villageRelationShips.evilStatusDuration));
				}
			});
		});
		original.remove(RemovalReason.KILLED);
		DragonModifiers.updateModifiers(original, player);
		player.refreshDimensions();
	}
}