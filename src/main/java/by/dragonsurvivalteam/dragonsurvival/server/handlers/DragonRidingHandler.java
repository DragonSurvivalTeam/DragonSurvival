package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.player.SynchronizeDragonCap;
import by.dragonsurvivalteam.dragonsurvival.network.status.RefreshDragons;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
@EventBusSubscriber
public class DragonRidingHandler{
	/**
	 * Mounting a dragon
	 */
	@SubscribeEvent
	public static void onEntityInteract(EntityInteractSpecific event){
		Entity ent = event.getTarget();

		if(event.getHand() != InteractionHand.MAIN_HAND){
			return;
		}


		if(ent instanceof ServerPlayer target){
			Player self = event.getEntity();

			DragonStateProvider.getCap(target).ifPresent(targetCap -> {
				if(targetCap.isDragon() && target.getPose() == Pose.CROUCHING && targetCap.getSize() >= 40 && !target.isVehicle()){
					DragonStateProvider.getCap(self).ifPresent(selfCap -> {
						if(!selfCap.isDragon() || selfCap.getLevel() == DragonLevel.NEWBORN){
							self.startRiding(target);
							target.connection.send(new ClientboundSetPassengersPacket(target));
							targetCap.setPassengerId(self.getId());
							NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), new SynchronizeDragonCap(target.getId(), targetCap.isHiding(), targetCap.getType(), targetCap.getBody(), targetCap.getSize(), targetCap.hasFlight(), self.getId()));
							event.setCancellationResult(InteractionResult.SUCCESS);
							event.setCanceled(true);
						}
					});
				}
			});
		}
	}

	@SubscribeEvent
	public static void onServerPlayerTick(TickEvent.PlayerTickEvent event){ // TODO: Find a better way of doing this.
		if(!(event.player instanceof ServerPlayer player)){
			return;
		}
		DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
			int passengerId = dragonStateHandler.getPassengerId();
			Entity passenger = player.level().getEntity(passengerId);
			boolean stopRiding = false;
			if(!dragonStateHandler.isDragon() && player.isVehicle() && player.getPassengers().get(0) instanceof ServerPlayer){
				stopRiding = true;
				player.getPassengers().get(0).stopRiding();
				player.connection.send(new ClientboundSetPassengersPacket(player));
			}else if(player.isSpectator() && passenger != null && player.getPassengers().get(0) instanceof ServerPlayer){
				stopRiding = true;
				player.getPassengers().get(0).stopRiding();
				player.connection.send(new ClientboundSetPassengersPacket(player));
			}else if(dragonStateHandler.isDragon() && dragonStateHandler.getSize() < 40 && player.isVehicle() && player.getPassengers().get(0) instanceof ServerPlayer){
				stopRiding = true;
				player.getPassengers().get(0).stopRiding();
				player.connection.send(new ClientboundSetPassengersPacket(player));
			}else if(player.isSleeping() && player.isVehicle() && player.getPassengers().get(0) instanceof ServerPlayer){
				stopRiding = true;
				player.getPassengers().get(0).stopRiding();
				player.connection.send(new ClientboundSetPassengersPacket(player));
			}
			if(passenger instanceof ServerPlayer){
				DragonStateHandler passengerCap = DragonUtils.getHandler(passenger);
				if(passengerCap.isDragon() && passengerCap.getLevel() != DragonLevel.NEWBORN){
					stopRiding = true;
					passenger.stopRiding();
					player.connection.send(new ClientboundSetPassengersPacket(player));
				}else if(passenger.getRootVehicle() != player.getRootVehicle()){
					stopRiding = true;
					passenger.stopRiding();
					player.connection.send(new ClientboundSetPassengersPacket(player));
				}
			}

			if (stopRiding || passenger != null && (!player.hasPassenger(passenger) || player.isSpectator() || passenger.isSpectator())) {
				dragonStateHandler.setPassengerId(0);
				NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SynchronizeDragonCap(player.getId(), dragonStateHandler.isHiding(), dragonStateHandler.getType(), dragonStateHandler.getBody(), dragonStateHandler.getSize(), dragonStateHandler.hasFlight(), 0));
			}
		});
	}

	@SubscribeEvent
	public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event){
		ServerPlayer player = (ServerPlayer)event.getEntity();
		if(player.getVehicle() == null || !(player.getVehicle() instanceof ServerPlayer vehicle)){
			return;
		}
		DragonStateProvider.getCap(player).ifPresent(playerCap -> {
			DragonStateProvider.getCap(vehicle).ifPresent(vehicleCap -> {
				player.stopRiding();
				vehicle.connection.send(new ClientboundSetPassengersPacket(vehicle));
				vehicleCap.setPassengerId(0);
				NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> vehicle), new SynchronizeDragonCap(player.getId(), vehicleCap.isHiding(), vehicleCap.getType(), vehicleCap.getBody(), vehicleCap.getSize(), vehicleCap.hasFlight(), 0));
			});
		});
	}

	@SubscribeEvent
	public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent changedDimensionEvent){
		Player player = changedDimensionEvent.getEntity();
		DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
			NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SynchronizeDragonCap(player.getId(), dragonStateHandler.isHiding(), dragonStateHandler.getType(), dragonStateHandler.getBody(), dragonStateHandler.getSize(), dragonStateHandler.hasFlight(), 0));
			NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new RefreshDragons(player.getId()));
		});
	}
}