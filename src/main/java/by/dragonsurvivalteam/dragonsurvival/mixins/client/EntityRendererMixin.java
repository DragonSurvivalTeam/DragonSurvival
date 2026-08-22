package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
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

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"))
    private int dragonSurvival$useGlowDataColor(final int teamColor, @Local(argsOnly = true) final T entity) {
        return entity.getExistingData(DSDataAttachments.GLOW).map(GlowData::getColor).orElse(teamColor);
    }

    @ModifyReturnValue(method = "getBoundingBoxForCulling", at = @At("RETURN"))
    private AABB dragonSurvival$expandDragonCullingBounds(final AABB original, final T entity) {
        if (entity instanceof Player player && DragonStateProvider.isDragon(player)) {
            return original.inflate(dragonSurvival$MODEL_CULLING_PADDING * player.getScale());
        }

        return original;
    }
}
