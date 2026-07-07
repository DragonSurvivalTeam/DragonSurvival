package by.dragonsurvivalteam.dragonsurvival.client.render.blocks;

import by.dragonsurvivalteam.dragonsurvival.client.DragonSurvivalClient;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block.RequestDragonSoulData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public class DragonSoulRenderer<T extends DragonSoulBlockEntity> implements BlockEntityRenderer<T, DragonSoulRenderer.State> {
    @Translation(key = "enable_soul_block_indicator", type = Translation.Type.CONFIGURATION, comments = "Renders the soul block base if enabled, as a visual indicator for the actual block")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"rendering"}, key = "enable_soul_block_indicator")
    public static boolean ENABLE_SOUL_BLOCK_INDICATOR = true;

    public DragonSoulRenderer(final BlockEntityRendererProvider.Context ignored) { /* Nothing to do */ }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        final T soul,
        final State state,
        final float partialTicks,
        final Vec3 cameraPosition,
        final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(soul, state, partialTicks, cameraPosition, breakProgress);
        state.facing = soul.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.fakePlayer = null;
        state.dragonRenderState = null;

        DragonStateHandler handler = soul.getHandler();

        if (handler == null || !handler.isDragon()) {
            if (soul.packetTimeout <= 0) {
                // When a player places the block, the components are not synchronized to the other clients.
                ClientPacketDistributor.sendToServer(new RequestDragonSoulData(soul.getBlockPos()));
                soul.packetTimeout = Functions.secondsToTicks(2);
            } else {
                soul.packetTimeout -= partialTicks;
            }

            return;
        }

        if (soul.fakePlayerIndex == -1) {
            soul.fakePlayerIndex = FakeClientPlayerUtils.getNextIndex();
        }

        DragonEntity dragon = FakeClientPlayerUtils.getFakeDragon(soul.fakePlayerIndex, handler);
        FakeClientPlayer player = FakeClientPlayerUtils.getFakePlayer(soul.fakePlayerIndex, handler);
        String animation = AnimationUtils.doesAnimationExist(DragonSurvivalClient.DRAGON_MODEL, dragon, soul.animation)
            ? soul.animation
            : DragonSoulBlockEntity.DEFAULT_ANIMATION;

        player.useVisualScale = true;
        player.scale = soul.getScale();
        player.animationSupplier = () -> animation;

        Vec3 position = soul.getBlockPos().getCenter();
        int tickCount = soul.getLevel() == null ? soul.tick : (int)soul.getLevel().getGameTime();

        dragon.setPos(position);
        dragon.xOld = position.x();
        dragon.yOld = position.y();
        dragon.zOld = position.z();
        dragon.xo = position.x();
        dragon.yo = position.y();
        dragon.zo = position.z();
        dragon.tickCount = tickCount;
        dragon.setYRot(0.0F);
        dragon.setYBodyRot(0.0F);
        dragon.yRotO = 0.0F;
        dragon.yBodyRotO = 0.0F;

        player.tickCount = tickCount;
        soul.tick = tickCount;

        state.fakePlayer = player;
        state.dragonRenderState = Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(dragon, partialTicks);
        state.dragonRenderState.shadowPieces.clear();
        state.dragonRenderState.outlineColor = 0;
    }

    @Override
    public void submit(final State state, final PoseStack poseStack, final SubmitNodeCollector nodeCollector, final CameraRenderState cameraRenderState) {
        if (state.dragonRenderState == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        rotateBlock(state.facing, poseStack);

        try {
            Minecraft.getInstance().getEntityRenderDispatcher().submit(state.dragonRenderState, cameraRenderState, 0.0D, 0.0D, 0.0D, poseStack, nodeCollector);
        } finally {
            if (state.fakePlayer != null) {
                state.fakePlayer.useVisualScale = false;
            }
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(final T soul) {
        double size = 6.0D * soul.getScale();
        return AABB.ofSize(soul.getBlockPos().getCenter(), size, size, size);
    }

    /** Taken from {@link com.geckolib.renderer.GeoBlockRenderer#rotateBlock(net.minecraft.core.Direction, com.mojang.blaze3d.vertex.PoseStack)} */
    private void rotateBlock(final Direction facing, final PoseStack pose) {
        switch (facing) {
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(270));
            case NORTH, UP, DOWN -> pose.mulPose(Axis.YP.rotationDegrees(0));
        }
    }

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public @Nullable EntityRenderState dragonRenderState;
        public @Nullable FakeClientPlayer fakePlayer;
    }
}
