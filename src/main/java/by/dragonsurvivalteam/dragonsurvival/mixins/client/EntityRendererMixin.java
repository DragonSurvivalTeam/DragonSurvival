package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Unique private static final double dragonSurvival$MODEL_CULLING_PADDING = 10.0D;

    @ModifyReturnValue(method = "getBoundingBoxForCulling", at = @At("RETURN"))
    private AABB dragonSurvival$expandDragonCullingBounds(final AABB original, final T entity) {
        if (entity instanceof Player player && DragonStateProvider.isDragon(player)) {
            return original.inflate(dragonSurvival$MODEL_CULLING_PADDING * player.getScale());
        }

        return original;
    }
}
