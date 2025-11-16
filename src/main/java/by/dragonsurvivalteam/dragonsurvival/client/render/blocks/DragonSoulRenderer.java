package by.dragonsurvivalteam.dragonsurvival.client.render.blocks;

import by.dragonsurvivalteam.dragonsurvival.client.DragonSurvivalClient;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.BlockRenderDispatcherAccess;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block.RequestDragonSoulData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class DragonSoulRenderer implements BlockEntityRenderer<DragonSoulBlockEntity> {
    @Translation(key = "enable_soul_block_indicator", type = Translation.Type.CONFIGURATION, comments = "Renders the soul block base if enabled, as a visual indicator for the actual block")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"rendering"}, key = "enable_soul_block_indicator")
    public static boolean ENABLE_SOUL_BLOCK_INDICATOR = true;

    public DragonSoulRenderer(final BlockEntityRendererProvider.Context ignored) { /* Nothing to do */ }

    @Override
    public void render(final DragonSoulBlockEntity soul, final float partialTick, @NotNull final PoseStack pose, @NotNull final MultiBufferSource buffer, final int packedLight, final int packedOverlay) {
        if (soul.isInvalid()) {
            if (soul.packetTimeout <= 0) {
                // When a player places the block, the components are not synchronized to the other clients
                // The player that places the block cannot determine when other clients receive the block entity
                // Therefor request the data when needed
                PacketDistributor.sendToServer(new RequestDragonSoulData(soul.getBlockPos()));
                soul.packetTimeout = Functions.secondsToTicks(2);
            } else if (soul.packetTimeout > 0) {
                soul.packetTimeout -= partialTick;
            }

            return;
        }

        if (soul.fakePlayerIndex == -1) {
            soul.fakePlayerIndex = FakeClientPlayerUtils.getNextIndex();
        }

        DragonEntity dragon = FakeClientPlayerUtils.getFakeDragon(soul.fakePlayerIndex, soul.getHandler());

        FakeClientPlayer player = FakeClientPlayerUtils.getFakePlayer(soul.fakePlayerIndex, soul.getHandler());
        player.useVisualScale = true;
        player.scale = soul.getScale();

        if (AnimationUtils.doesAnimationExist(DragonSurvivalClient.DRAGON_MODEL, dragon, soul.animation)) {
            player.animationSupplier = () -> soul.animation;
        } else {
            player.animationSupplier = () -> DragonSoulBlockEntity.DEFAULT_ANIMATION;
        }

        pose.pushPose();
        pose.translate(0.5, 0, 0.5); // Move to the center of the block
        rotateBlock(soul.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), pose);
        Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon).render(dragon, 0, partialTick, pose, buffer, packedLight);
        pose.popPose();

        player.useVisualScale = false;

        if (ENABLE_SOUL_BLOCK_INDICATOR) {
            renderBlock(soul.getBlockState(), pose, buffer, packedLight, packedOverlay);
        }
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(final DragonSoulBlockEntity soul) {
        return AABB.ofSize(soul.getBlockPos().getCenter(), 6 * soul.getScale(), 6 * soul.getScale(), 6 * soul.getScale());
    }

    /** Copy from {@link net.minecraft.client.renderer.block.BlockRenderDispatcher#renderSingleBlock}, bypassing the check for the rendershape defined in the block */
    private void renderBlock(final BlockState state, final PoseStack pose, final MultiBufferSource buffer, final int packedLight, final int packedOverlay) {
        BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();

        BakedModel bakedmodel = renderer.getBlockModel(state);
        int i = ((BlockRenderDispatcherAccess) renderer).dragonSurvival$getBlockColors().getColor(state, null, null, 0);
        float rede = (float) (i >> 16 & 0xFF) / 255.0F;
        float green = (float) (i >> 8 & 0xFF) / 255.0F;
        float blue = (float) (i & 0xFF) / 255.0F;

        for (net.minecraft.client.renderer.RenderType renderType : bakedmodel.getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY)) {
            renderer.getModelRenderer()
                    .renderModel(
                            pose.last(),
                            buffer.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false)),
                            state,
                            bakedmodel,
                            rede,
                            green,
                            blue,
                            packedLight,
                            packedOverlay,
                            ModelData.EMPTY,
                            renderType
                    );
        }
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