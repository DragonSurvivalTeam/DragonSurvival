package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncChatEvent;
import by.dragonsurvivalteam.dragonsurvival.network.status.RefreshDragon;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class WingObtainmentController{

	private static final Map<String, Integer> dragonPhrases = new HashMap<>();

	private static final UUID enderDragonUUID = UUID.fromString("426642b9-2e88-4350-afa8-f99f75af5479");

	@OnlyIn( Dist.CLIENT )
	public static void loadDragonPhrases(){

		try{
			List<String> langs = new ArrayList<>();
			InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/dragonsurvival/lang");
			if(stream == null){
				return;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String file;
			while((file = reader.readLine()) != null){
				langs.add(file);
			}
			reader.close();
			Gson gson = GsonFactory.getDefault();
			Type type = new TypeToken<Map<String, String>>(){
			}.getType();
			for(String langFile : langs){
				URL resource = Thread.currentThread().getContextClassLoader().getResource("assets/dragonsurvival/lang/" + langFile);
				Map<String, String> langData = gson.fromJson(new String(Files.readAllBytes(Paths.get(resource.toURI()))), type);
				int phraseCount = 0;
				for(String key : langData.keySet()){
					if(key.contains("ds.endmessage")){
						phraseCount++;
					}
				}
				if(phraseCount > 0){
					dragonPhrases.put(langFile.replace(".json", ""), phraseCount);
				}
			}
		}catch(Exception ex){
			DragonSurvivalMod.LOGGER.error(ex);
		}
	}


	private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	@SubscribeEvent
	public static void inTheEnd(PlayerEvent.PlayerChangedDimensionEvent changedDimensionEvent){
		if (!(changedDimensionEvent.getEntity() instanceof ServerPlayer player))
			return;

		if(changedDimensionEvent.getTo() == Level.END){
			DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
				if(dragonStateHandler.isDragon() && !dragonStateHandler.getMovementData().spinLearned && ServerFlightHandler.enderDragonGrantsSpin){
					executorService.schedule(() -> PacketDistributor.sendToPlayer(player, new SyncChatEvent.Data(enderDragonUUID.toString(), "ds.endmessage")), 3, TimeUnit.SECONDS);
				}
			});
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void clientMessageRecieved(SyncChatEvent.Data event){
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;

		if(event.signerId().equals(enderDragonUUID.toString())){
			Vec3 centerPoint = new Vec3(0D, 128D, 0D);
			List<EnderDragon> enderDragons = player.level().getEntitiesOfClass(EnderDragon.class, AABB.ofSize(centerPoint,192 ,192 ,192));
			if (enderDragons.isEmpty())
				return;
			String dragonName = "<"+enderDragons.get(0).getDisplayName().getString()+"> ";
			if(event.chatId().equals("ds.endmessage")){
				String language = Minecraft.getInstance().getLanguageManager().getSelected();
				int messageId = player.getRandom().nextInt(dragonPhrases.getOrDefault(language, dragonPhrases.getOrDefault("en_us", 1))) + 1;
				player.sendSystemMessage(Component.literal(dragonName).append(Component.translatable("ds.endmessage." + messageId, player.getDisplayName().getString())));
			}else if(event.chatId().equals("ds.dragon.grants.wings")){
				player.sendSystemMessage(Component.translatable("ds.dragon.grants.wings"));
			}
		}
	}


	@SubscribeEvent
	public static void serverChatEvent(ServerChatEvent chatEvent){
		Component message = chatEvent.getMessage();
		ServerPlayer player = chatEvent.getPlayer();
		String lowercase = message.getString().toLowerCase();
		DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon() && !dragonStateHandler.getMovementData().spinLearned && ServerFlightHandler.enderDragonGrantsSpin){
				if(player.level().dimension() == Level.END) {
					if(!player.serverLevel().getDragons().isEmpty()){
						if(!lowercase.isEmpty()){
							executorService.schedule(() -> player.sendSystemMessage(Component.translatable("ds.dragon.grants.wings")), 2, TimeUnit.SECONDS);

							dragonStateHandler.setHasFlight(true);
							dragonStateHandler.getMovementData().spinLearned = true;
							PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncSpinStatus.Data(player.getId(), dragonStateHandler.getMovementData().spinAttack, dragonStateHandler.getMovementData().spinCooldown, dragonStateHandler.getMovementData().spinLearned));
							PacketDistributor.sendToAllPlayers(new SyncComplete.Data(player.getId(), DragonStateProvider.getOrGenerateHandler(player).serializeNBT(player.registryAccess())));
						}
					}
				}
			}
		});
	}

	@SubscribeEvent
	public static void teleportAway(LivingIncomingDamageEvent damageEvent){
		if(!ServerConfig.endVoidTeleport){
			return;
		}
		LivingEntity living = damageEvent.getEntity();
		if(living instanceof Player){
			DamageSource damageSource = damageEvent.getSource();
			if(living.level().dimension() == Level.END && damageSource == living.damageSources().fellOutOfWorld() && living.position().y < -60){
				DragonStateProvider.getCap(living).ifPresent(dragonStateHandler -> {
					if(dragonStateHandler.isDragon()){
						DimensionTransition transition = new DimensionTransition(living.level().getServer().overworld(), living, DimensionTransition.DO_NOTHING);
						living.changeDimension(transition);
						PacketDistributor.sendToAllPlayers(new RefreshDragon.Data(living.getId()));
						damageEvent.setCanceled(true);
					}
				});
			}
		}
	}
}