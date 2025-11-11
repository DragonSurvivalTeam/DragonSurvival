package by.dragonsurvivalteam.dragonsurvival.client.render.blocks;

import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DragonSoulRenderer implements BlockEntityRenderer<DragonSoulBlockEntity> {
    @Translation(key = "enable_soul_block_indicator", type = Translation.Type.CONFIGURATION, comments = "If enabled particles will spawn at the block position of the soul block while the player is looking at it")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"rendering"}, key = "enable_soul_block_indicator")
    public static boolean ENABLE_SOUL_BLOCK_INDICATOR = true;

    public DragonSoulRenderer(final BlockEntityRendererProvider.Context ignored) { /* Nothing to do */ }

    @Override
    public void render(final DragonSoulBlockEntity soul, final float partialTick, @NotNull final PoseStack pose, @NotNull final MultiBufferSource buffer, final int packedLight, final int packedOverlay) {
        if (!soul.getHandler().isDragon()) {
            return;
        }

        if (soul.fakePlayerIndex == -1) {
            soul.fakePlayerIndex = FakeClientPlayerUtils.getNextIndex();
        }

        FakeClientPlayer player = FakeClientPlayerUtils.getFakePlayer(soul.fakePlayerIndex, soul.getHandler());
        player.animationSupplier = () -> "sit_dentist"; // FIXME :: configurable? store in data and then switch per right click or sth.?
        DragonEntity dragon = FakeClientPlayerUtils.getFakeDragon(soul.fakePlayerIndex, soul.getHandler());
        player.useVisualScale = true;
        player.scale = soul.getScale();

        pose.pushPose();
        pose.translate(0.5, 0, 0.5); // Move to the center of the block
        rotateBlock(soul.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), pose);
        Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon).render(dragon, 0, partialTick, pose, buffer, packedLight);
        pose.popPose();
        player.useVisualScale = false;

        if (!ENABLE_SOUL_BLOCK_INDICATOR || Minecraft.getInstance().isPaused()) {
            return;
        }

        soul.tick++;

        if (soul.tick % 40 != 0) {
            return;
        }

        soul.tick = 0;

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        AABB box = getRenderBoundingBox(soul);

        if (box.contains(camera.getPosition()) || box.clip(camera.getPosition(), camera.getPosition().add(new Vec3(camera.getLookVector()).scale(16))).isPresent()) {
            //noinspection DataFlowIssue -> level is present
            ParticleUtils.spawnParticles(soul.getLevel(), soul.getBlockPos(), 1, 0.75, 0.75, false, DSParticles.GLOW.get());
        }
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(final DragonSoulBlockEntity soul) {
        return AABB.ofSize(soul.getBlockPos().getCenter(), 6 * soul.getScale(), 6 * soul.getScale(), 6 * soul.getScale());
    }

    /** Taken from {@link software.bernie.geckolib.renderer.GeoBlockRenderer#rotateBlock(net.minecraft.core.Direction, com.mojang.blaze3d.vertex.PoseStack)} */
    private void rotateBlock(final Direction facing, final PoseStack pose) {
        switch (facing) {
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(90));
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(0));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(270));
        }
    }
}