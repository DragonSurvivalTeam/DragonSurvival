package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void dragonSurvival$submitFirstPersonDragonBody(final PoseStack poseStack, final LevelRenderState levelRenderState, final SubmitNodeCollector output, final CallbackInfo callback) {
        Entity cameraEntity = minecraft.getCameraEntity();

        if (!(cameraEntity instanceof Player player)) {
            return;
        }

        if (minecraft.gameRenderer.getMainCamera().isDetached() || !ClientDragonRenderer.renderInFirstPerson || !DragonStateProvider.isDragon(player) || !RenderingUtils.isFirstPerson(player)) {
            return;
        }

        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(!minecraft.level.tickRateManager().isEntityFrozen(player));
        EntityRenderState state = entityRenderDispatcher.extractEntity(player, partialTick);

        if (!levelRenderState.haveGlowingEntities) {
            state.outlineColor = 0;
        }

        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        entityRenderDispatcher.submit(state, levelRenderState.cameraRenderState, state.x - cameraPos.x(), state.y - cameraPos.y(), state.z - cameraPos.z(), poseStack, output);
    }

}
