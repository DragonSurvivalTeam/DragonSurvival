package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow
    public abstract Identifier getTextureLocation(S state);

    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private @Nullable RenderType dragonSurvival$useHunterTranslucentRenderType(final @Nullable RenderType original, final S state) {
        Player player = dragonSurvival$getHunterPlayer(state);

        if (player != null && dragonSurvival$hasHunterTransparency(player) && !state.isInvisible) {
            return RenderTypes.entityTranslucentCullItemTarget(getTextureLocation(state));
        }

        return original;
    }

    @ModifyReturnValue(method = "getModelTint", at = @At("RETURN"))
    private int dragonSurvival$modifyHunterAlpha(final int original, final S state) {
        Player player = dragonSurvival$getHunterPlayer(state);

        if (player != null && dragonSurvival$hasHunterTransparency(player)) {
            return HunterHandler.modifyAlpha(player, original);
        }

        return original;
    }

    private static @Nullable Player dragonSurvival$getHunterPlayer(final LivingEntityRenderState state) {
        if (!(state instanceof AvatarRenderState avatarRenderState)) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return null;
        }

        return minecraft.level.getEntity(avatarRenderState.id) instanceof Player player ? player : null;
    }

    private static boolean dragonSurvival$hasHunterTransparency(final Player player) {
        float alpha = HunterHandler.calculateAlphaAsFloat(player);
        return alpha != HunterHandler.UNMODIFIED && alpha < 1.0F;
    }
}
