package by.dragonsurvivalteam.dragonsurvival.util;

import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

public class TargetingFunctions
{
	public static boolean attackTargets(Entity attacker, Function<Entity, Boolean> action, Entity... entities){
		boolean valid = false;
		for(Entity entity : entities){
			if(isValidTarget(attacker, entity)){
				if(action.apply(entity)){
					valid = true;
				}
			}
		}

		return valid;
	}
	
	public static boolean isValidTarget(Entity attacker, Entity target){
		if(target == null || attacker == null){
			return false;
		}
		if(target == attacker){
			return false;
		}

		if(target instanceof FakePlayer){
			return false;
		}

		if(attacker instanceof Player attackerPlayer && target instanceof Player targetPlayer){
			if(!attackerPlayer.canHarmPlayer(targetPlayer)){
				return false;
			}
		}

		if(attacker.getTeam() != null){
			if(target.getTeam() != null && attacker.getTeam().getPlayers().contains(target.getScoreboardName())){
				if(!target.getTeam().isAllowFriendlyFire()){
					return false;
				}
			}
		}

		if(target instanceof TamableAnimal && ((TamableAnimal)target).getOwner() == attacker){
			return false;
		}

		return !(attacker instanceof TamableAnimal) || isValidTarget(((TamableAnimal)attacker).getOwner(), target);
	}
	
	public static AABB boxForRange(Vec3 v, double range) {
		return boxForRange(v, range, range, range);
	}
	
	public static AABB boxForRange(Vec3 v, double rangeX, double rangeY, double rangeZ) {
		return new AABB(v.x - rangeX, v.y - rangeY, v.z - rangeZ, v.x + rangeX, v.y + rangeY, v.z + rangeZ);
	}
	
	public static Vec3 fromEntityCenter(Entity e) {
		return new Vec3(e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ());
	}
}