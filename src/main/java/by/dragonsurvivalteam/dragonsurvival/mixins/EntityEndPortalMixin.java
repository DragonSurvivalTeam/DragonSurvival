package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.structures.EndPlatformHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityEndPortalMixin {
    /** Applies the species-specific End spawn and keeps dragons clear of the platform floor. */
    @ModifyReturnValue(method = "findDimensionEntryPoint", at = @At("RETURN"))
    private PortalInfo dragonSurvival$modifyEndPortalDestination(final PortalInfo original, @Local(argsOnly = true) final ServerLevel destination) {
        Entity entity = (Entity) (Object) this;

        if (original == null || !(entity instanceof Player player)) {
            return original;
        }

        boolean enteringEnd = entity.level().dimension() == Level.OVERWORLD && destination.dimension() == Level.END;
        boolean usingEndPortal = enteringEnd || entity.level().dimension() == Level.END && destination.dimension() == Level.OVERWORLD;
        Vec3 position = original.pos;

        if (enteringEnd) {
            BlockPos spawnPoint = EndPlatformHandler.getSpawnPoint(player);

            if (spawnPoint != null) {
                position = Vec3.atBottomCenterOf(spawnPoint);
            }
        }

        if (usingEndPortal && DragonStateProvider.isDragon(player)) {
            position = position.add(0, 0.1D, 0);
        }

        return position == original.pos ? original : new PortalInfo(position, original.speed, original.yRot, original.xRot);
    }
}
