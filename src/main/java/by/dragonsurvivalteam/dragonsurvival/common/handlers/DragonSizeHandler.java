package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.Capabilities;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class DragonSizeHandler{
	private static final ConcurrentHashMap<Integer, Boolean> wasDragon = new ConcurrentHashMap<Integer, Boolean>(20);
	public static ConcurrentHashMap<Integer, Double> lastSize = new ConcurrentHashMap<Integer, Double>(20);

	@SubscribeEvent
	public static void getDragonSize(EntityEvent.Size event){
		if(!(event.getEntity() instanceof Player player) || !DragonUtils.isDragon(player)){
			return;
		}
		DragonStateHandler dragonStateHandler = DragonUtils.getHandler(player);

		double size = dragonStateHandler.getSize();
		// Calculate base values
		double height = calculateDragonHeight(size, ServerConfig.hitboxGrowsPastHuman);
		double width = calculateDragonWidth(size, ServerConfig.hitboxGrowsPastHuman);
		double eyeHeight = calculateDragonEyeHeight(size, ServerConfig.hitboxGrowsPastHuman);
		// Handle Pose stuff
		if(ServerConfig.sizeChangesHitbox){
			Pose overridePose = overridePose(player);
			height = calculateModifiedHeight(height, overridePose, true);
			eyeHeight = calculateModifiedEyeHeight(eyeHeight, overridePose);
			// Apply changes
			event.setNewEyeHeight((float)eyeHeight);
			// Rounding solves floating point issues that caused the dragon to get stuck inside a block at times.
			event.setNewSize(calculateDimensions(width, height));
		}
	}

	public static double calculateDragonHeight(double size, boolean growsPastHuman){
		double height = (size + 4.0D) / 20.0D; // 0.9 -> Config Dragon Max
		if(!growsPastHuman){
			height = 0.9D + 0.9D * (size - 14.0D) / (ServerConfig.maxGrowthSize - 14.0D); // 0.9 -> 1.8 (Min to Human Max)
		}
		return height;
	}

	public static double calculateDragonWidth(double size, boolean growsPastHuman){
		double width = (3.0D * size + 62.0D) / 260.0D; // 0.4 -> Config Dragon Max
		if(!growsPastHuman){
			width = 0.4D + 0.2D * (size - 14.0D) / (ServerConfig.maxGrowthSize - 14.0D); // 0.4 -> 0.6 (Min to Human Max)
		}
		return width;
	}

	public static double calculateDragonEyeHeight(double size, boolean growsPastHuman){
		double eyeHeight = (11.0D * size + 54.0D) / 260.0D; // 0.8 -> Config Dragon Max
		if(!growsPastHuman){
			eyeHeight = 0.8D + 0.8D * (size - 14.0D) / (ServerConfig.maxGrowthSize - 14.0D); // 0.8 -> 1.6 (Min to Human Max)
		}
		return eyeHeight;
	}

	public static double calculateModifiedEyeHeight(double eyeHeight, Pose pose){
		if(pose == Pose.CROUCHING){
			eyeHeight *= 5.0D / 6.0D;
		}else if(pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SPIN_ATTACK){
			eyeHeight *= 7.0D / 12.0D;
		}
		return eyeHeight;
	}

	public static EntityDimensions calculateDimensions(double width, double height)
	{
		return new EntityDimensions((float)(Math.round(width * 100.0D) / 100.0D), (float)(Math.round(height * 100.0D) / 100.0D), false);
	}

	public static Pose overridePose(Player player){
		if(player == null) return Pose.STANDING;

		Pose overridePose = getOverridePose(player);
		if(player.getForcedPose() != overridePose){
			player.setForcedPose(overridePose);
			if(player.level().isClientSide() && Minecraft.getInstance().getCameraEntity() != player){
				player.refreshDimensions();
			}
		}


		return overridePose;
	}

	public static Pose getOverridePose(LivingEntity player){
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if(player != null){
			boolean swimming = (player.isInWaterOrBubble() || player.isInLava() && ServerConfig.bonuses && ServerConfig.caveLavaSwimming && DragonUtils.isDragonType(handler, DragonTypes.CAVE)) && player.isSprinting() && !player.isPassenger();
			boolean flying = ServerFlightHandler.isFlying(player);
			boolean spinning = player.isAutoSpinAttack();
			boolean crouching = player.isShiftKeyDown();
			if(flying && !player.isSleeping()){
				return Pose.FALL_FLYING;
			}else if(swimming || (player.isInWaterOrBubble() || player.isInLava()) && !canPoseFit(player, Pose.STANDING) && canPoseFit(player, Pose.SWIMMING)){
				return Pose.SWIMMING;
			}else if(spinning){
				return Pose.SPIN_ATTACK;
			}else if(crouching || !canPoseFit(player, Pose.STANDING) && canPoseFit(player, Pose.CROUCHING)){
				return Pose.CROUCHING;
			}
		}
		return Pose.STANDING;
	}

	public static boolean canPoseFit(LivingEntity player, Pose pose){
		if(!DragonStateProvider.getCap(player).isPresent()){
			return false;
		}
		double size = player.getCapability(Capabilities.DRAGON_CAPABILITY).orElse(null).getSize();
		double height = calculateModifiedHeight(calculateDragonHeight((float)size, ServerConfig.hitboxGrowsPastHuman), pose, ServerConfig.sizeChangesHitbox);
		double width = calculateDragonWidth((float)size, ServerConfig.hitboxGrowsPastHuman);
		return player.level().noCollision(calculateDimensions(width,height).makeBoundingBox(player.position()));
	}

	public static double calculateModifiedHeight(double height, Pose pose, boolean sizeChangesHitbox){
		if(pose == Pose.CROUCHING){
			if(sizeChangesHitbox){
				height *= 5.0D / 6.0D;
			}else{
				height = 1.5D;
			}
		}else if(pose == Pose.SWIMMING || pose == Pose.FALL_FLYING || pose == Pose.SPIN_ATTACK){
			if(sizeChangesHitbox){
				height *= 7.0D / 12.0D;
			}else{
				height = 0.6D;
			}
		}
		return height;
	}

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event){
		Player player = event.player;
		if(player == null || event.phase == TickEvent.Phase.END || !ServerConfig.sizeChangesHitbox){
			return;
		}
		DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				overridePose(player);
				if(!wasDragon.getOrDefault(player.getId(), false)){
					player.refreshDimensions();
					wasDragon.put(player.getId(), true);
				}else if(lastSize.getOrDefault(player.getId(), 20.0) != dragonStateHandler.getSize()){
					player.refreshDimensions();
					lastSize.put(player.getId(), dragonStateHandler.getSize());
				}
			}else if(wasDragon.getOrDefault(player.getId(), false)){
				player.setForcedPose(null);
				player.refreshDimensions();
				wasDragon.put(player.getId(), false);
			}
		});
	}
}