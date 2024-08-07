package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import com.google.common.base.Objects;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class DragonUtils {

	public static AbstractDragonType getDragonType(Entity entity){
		return DragonStateProvider.getOrGenerateHandler(entity).getType();
	}
	
	public static AbstractDragonType getDragonType(DragonStateHandler handler) {
		return handler.getType();
	}

	public static AbstractDragonBody getDragonBody(Entity entity) {
		return DragonStateProvider.getOrGenerateHandler(entity).getBody();
	}

	public static AbstractDragonBody getDragonBody(DragonStateHandler handler) {
		return handler.getBody();
	}

	public static boolean isDragonType(final Entity entity, final AbstractDragonType typeToCheck) {
		if (!(entity instanceof Player)) {
			return false;
		}

		return isDragonType(DragonStateProvider.getOrGenerateHandler(entity), typeToCheck);
	}
	
	public static boolean isDragonType(final DragonStateHandler playerHandler, final AbstractDragonType typeToCheck) {
		if (playerHandler == null || typeToCheck == null || playerHandler.getType() == null) {
			return false;
		}

		return Objects.equal(playerHandler.getType().getTypeName(), typeToCheck.getTypeName());
	}

	public static boolean isDragonType(final AbstractDragonType playerType, final AbstractDragonType typeToCheck) {
		if (playerType == null || typeToCheck == null) {
			return false;
		}

		return Objects.equal(playerType.getTypeName(), typeToCheck.getTypeName());
	}

	public static DragonLevel getDragonLevel(Entity entity){
		return DragonStateProvider.getOrGenerateHandler(entity).getLevel();
	}

	public static boolean isNearbyDragonPlayerToEntity(double detectionRadius, Level level, Entity entity) {
		List<Player> players = level.getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(detectionRadius));

		for (Player player : players) {
			if (DragonStateProvider.isDragon(player)) {
				return true;
			}
		}
		return false;
	}

	/** Converts the supplied harvest level to a corresponding vanilla tier */
	public static @Nullable Tier levelToVanillaTier(int level) {
		if (level < 0) {
			return null;
		} else if (level == 0) {
			return Tiers.WOOD;
		} else if (level == 1) {
			return Tiers.STONE;
		} else if (level == 2) {
			return Tiers.IRON;
		} else if (level == 3) {
			return Tiers.DIAMOND;
		}

		return Tiers.NETHERITE;
	}
}