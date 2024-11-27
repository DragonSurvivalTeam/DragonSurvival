package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.VillageRelationShips;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncSize;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.CompleteDataSync;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonModifiers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber( modid = DragonSurvivalMod.MODID, bus = Bus.FORGE )
public class Capabilities{
	public static Capability<VillageRelationShips> VILLAGE_RELATIONSHIP = CapabilityManager.get(new CapabilityToken<>(){});
	public static Capability<DragonStateHandler> GENERIC_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
	public static Capability<EntityStateHandler> ENTITY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
	public static Capability<DragonStateHandler> DRAGON_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event){
		Entity entity = event.getObject();

		if (entity instanceof Player player) {
			if (entity.level().isClientSide() && isFakePlayer(player)) {
				return;
			}

			DragonStateProvider provider = new DragonStateProvider();
			event.addCapability(new ResourceLocation("dragonsurvival", "playerstatehandler"), provider);
		} else if (entity instanceof LivingEntity) {
			EntityStateProvider provider = new EntityStateProvider();
			event.addCapability(new ResourceLocation("dragonsurvival", "entitystatehandler"), provider);
		}
	}

	@OnlyIn( Dist .CLIENT)
	private static boolean isFakePlayer(Player player){
		return player instanceof FakeClientPlayer;
	}

	/** Only called on the server-side */
	@SubscribeEvent
	public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			DragonStateProvider.getCap(serverPlayer).ifPresent(handler -> {
				NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new RequestClientData(handler.getType(), handler.getBody(), handler.getLevel()));
				NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SyncDragonClawsMenu(serverPlayer.getId(), handler.getClawToolData().isMenuOpen(), handler.getClawToolData().getClawsInventory()));
			});
			// TODO: Investigate whether this call to syncCapability actually results in data loss under bad network conditions
			syncCapability(serverPlayer);

			// Due to them being transient modifiers
			DragonModifiers.updateModifiers(serverPlayer);
		}
	}

	public static void syncCapability(final Player player) {
		NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new CompleteDataSync(player));
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent playerRespawnEvent){
		Player player = playerRespawnEvent.getEntity();
		syncCapability(player);

		// Fixes dragon size upon respawn if size is less than default player
		DragonStateProvider.getCap(player).ifPresent(cap -> {
			NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SyncSize(player.getId(), cap.getSize()));
		});
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event){
		// TODO :: Might not needed if EntityJoinLevelEvent is used instead of PlayerLoggedInEvent?
		Player player = event.getEntity();
		syncCapability(player);
		DragonStateProvider.getCap(player).ifPresent(cap -> cap.getSkinData().compileSkin());
	}

	@SubscribeEvent
	public static void onTrackingStart(PlayerEvent.StartTracking startTracking){
		Player trackingPlayer = startTracking.getEntity();
		if(trackingPlayer instanceof ServerPlayer target){
			Entity tracked = startTracking.getTarget();
			if(tracked instanceof ServerPlayer){
				DragonStateProvider.getCap(tracked).ifPresent(dragonStateHandler -> NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), new CompleteDataSync(tracked.getId(), dragonStateHandler.writeNBT())));
			}
		}
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone e){
		Player player = e.getEntity();
		Player original = e.getOriginal();
		original.reviveCaps();

		// Need to call the old capability directly, otherwise the cache will return the value of the new capability (which is empty)
		DragonStateProvider.getCap(player).ifPresent(capNew -> original.getCapability(DRAGON_CAPABILITY).ifPresent(capOld -> {
			CompoundTag nbt = capOld.writeNBT();
			capNew.readNBT(nbt);
			capNew.getSkinData().compileSkin();

			if(ServerConfig.preserveRoyalChaseEffectAfterDeath && capOld.getVillageRelationShips().evilStatusDuration > 0){
				player.addEffect(new MobEffectInstance(DragonEffects.ROYAL_CHASE, capOld.getVillageRelationShips().evilStatusDuration));
			}


			NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new CompleteDataSync(player.getId(), nbt));
		}));

		DragonModifiers.updateModifiers(player);
		original.invalidateCaps();
		player.refreshDimensions();
	}

	@SubscribeEvent
	public static void clearCache(final EntityLeaveLevelEvent event) {
		if (event.getEntity() instanceof Player player) {
			DragonStateProvider.clearCache(player);
		}
	}
}