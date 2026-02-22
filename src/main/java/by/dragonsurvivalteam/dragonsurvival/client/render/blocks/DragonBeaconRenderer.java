package by.dragonsurvivalteam.dragonsurvival.client.render.blocks;

import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonBeaconBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;


public class DragonBeaconRenderer<T extends DragonBeaconBlockEntity> implements BlockEntityRenderer<T, BlockEntityRenderState> {
    public DragonBeaconRenderer(final BlockEntityRendererProvider.Context ignored) { /* Nothing to do */ }

//    @Override
//    public void render(final DragonBeaconBlockEntity beacon, final float partialTick, final PoseStack pose, @NotNull final MultiBufferSource buffer, final int packedLight, final int packedOverlay) {
//        Level level = Objects.requireNonNull(beacon.getLevel());
//
//        boolean hasMemoryBlock = level.getBlockState(beacon.getBlockPos().below()).is(DSBlocks.DRAGON_MEMORY_BLOCK);
//        boolean isPaused = Minecraft.getInstance().isPaused();
//
//        double x = beacon.getBlockPos().getX() + (0.25 + level.getRandom().nextInt(5) / 10d);
//        double y = beacon.getBlockPos().getY() + 0.5;
//        double z = beacon.getBlockPos().getZ() + (0.25 + level.getRandom().nextInt(5) / 10d);
//
//        boolean isActive = beacon.getBlockState().getValue(BlockStateProperties.LIT);
//        Item item = isActive ? DSItems.ACTIVATED_DRAGON_BEACON.value() : DSBlocks.DRAGON_BEACON.value().asItem();
//
//        if (!isPaused && isActive && beacon.tick % 5 == 0 && hasMemoryBlock) {
//            double random = level.getRandom().nextDouble();
//            ParticleOptions particle;
//
//            if (random < 0.33) {
//                particle = DSParticles.CAVE_BEACON_PARTICLE.value();
//            } else if (random < 0.66) {
//                particle = DSParticles.FOREST_BEACON_PARTICLE.value();
//            } else {
//                particle = DSParticles.SEA_BEACON_PARTICLE.value();
//            }
//
//            level.addParticle(particle, x, y, z, 0, 0, 0);
//        }
//
//        if (!isPaused) {
//            beacon.tick += 0.5f;
//        }
//
//        pose.pushMatrix();
//        float bounce = Mth.sin((beacon.tick + partialTick) / 20 + beacon.bobOffset) * 0.1f + 0.1f;
//        pose.translate(0.5, 0.25 + bounce / 2f, 0.5);
//        pose.mulPose(Axis.YP.rotationDegrees(beacon.tick));
//
//        pose.scale(2, 2, 2);
//        Minecraft.getInstance().getItemRenderer().renderStatic(item.getDefaultInstance(), ItemDisplayContext.GROUND, packedLight, packedOverlay, pose, buffer, level, 0);
//        pose.popMatrix();
//    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return null;
    }

    @Override
    public void submit(BlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        // FIXME
    }
}