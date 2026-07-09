package by.dragonsurvivalteam.dragonsurvival.client.render.blocks;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonBeaconBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DragonBeaconRenderer<T extends DragonBeaconBlockEntity> implements BlockEntityRenderer<T, DragonBeaconRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public DragonBeaconRenderer(final BlockEntityRendererProvider.Context context) {
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NonNull State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        final @NonNull T beacon,
        final @NonNull State state,
        final float partialTicks,
        final @NonNull Vec3 cameraPosition,
        final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(beacon, state, partialTicks, cameraPosition, breakProgress);

        Level level = beacon.getLevel();
        boolean isPaused = Minecraft.getInstance().isPaused();
        boolean isActive = beacon.getBlockState().getValue(BlockStateProperties.LIT);
        Item item = isActive ? DSItems.ACTIVATED_DRAGON_BEACON.value() : DSBlocks.DRAGON_BEACON.value().asItem();

        if (level != null) {
            spawnParticles(beacon, level, isActive, isPaused);
        }

        if (!isPaused) {
            beacon.tick += 0.5F;
        }

        state.tick = beacon.tick;
        state.bobOffset = beacon.bobOffset;
        state.partialTicks = partialTicks;
        itemModelResolver.updateForTopItem(state.item, item.getDefaultInstance(), ItemDisplayContext.GROUND, level, null, 0);
    }

    @Override
    public void submit(final State state, final @NonNull PoseStack poseStack, final @NonNull SubmitNodeCollector nodeCollector, final @NonNull CameraRenderState cameraRenderState) {
        if (state.item.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        float bounce = Mth.sin((state.tick + state.partialTicks) / 20.0F + state.bobOffset) * 0.1F + 0.1F;
        poseStack.translate(0.5F, 0.25F + bounce / 2.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.tick));
        poseStack.scale(2.0F, 2.0F, 2.0F);
        state.item.submit(poseStack, nodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static void spawnParticles(final DragonBeaconBlockEntity beacon, final Level level, final boolean isActive, final boolean isPaused) {
        if (isPaused || !isActive || beacon.tick % 5.0F != 0.0F) {
            return;
        }

        if (!level.getBlockState(beacon.getBlockPos().below()).is(DSBlocks.DRAGON_MEMORY_BLOCK.value())) {
            return;
        }

        double x = beacon.getBlockPos().getX() + 0.25D + level.getRandom().nextInt(5) / 10.0D;
        double y = beacon.getBlockPos().getY() + 0.5D;
        double z = beacon.getBlockPos().getZ() + 0.25D + level.getRandom().nextInt(5) / 10.0D;
        double random = level.getRandom().nextDouble();
        ParticleOptions particle;

        if (random < 0.33D) {
            particle = DSParticles.CAVE_BEACON_PARTICLE.value();
        } else if (random < 0.66D) {
            particle = DSParticles.FOREST_BEACON_PARTICLE.value();
        } else {
            particle = DSParticles.SEA_BEACON_PARTICLE.value();
        }

        level.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float tick;
        public float bobOffset;
        public float partialTicks;
    }
}
