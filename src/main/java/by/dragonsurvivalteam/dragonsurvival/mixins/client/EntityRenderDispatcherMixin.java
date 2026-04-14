package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "submit", at = @At("HEAD"))
    private <S extends EntityRenderState> void dragonSurvival$hideHunterShadows(
        final S renderState,
        final CameraRenderState camera,
        final double x,
        final double y,
        final double z,
        final PoseStack poseStack,
        final SubmitNodeCollector submitNodeCollector,
        final CallbackInfo callback
    ) {
        if (!(renderState instanceof AvatarRenderState avatarRenderState)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(avatarRenderState.id);

        if (!(entity instanceof Player player)) {
            return;
        }

        if (DragonStateProvider.isDragon(player)) {
            renderState.shadowPieces.clear();
            renderState.shadowRadius = 0;
            return;
        }

        float alpha = HunterHandler.calculateAlphaAsFloat(player);

        if (alpha != HunterHandler.UNMODIFIED && alpha != HunterHandler.NON_TRANSPARENT) {
            renderState.shadowPieces.clear();
            renderState.shadowRadius = 0;
        }
    }
}
