package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.LevelRendererAccess;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncLargeDragonDestruction;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncMultiMining;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.SortedSet;

/** See {@link by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonDestructionHandler} for server-specific handling */
@EventBusSubscriber(Dist.CLIENT)
public class DragonDestructionHandler {
    @Translation(comments = "Destruction mode enabled")
    private static final String ENABLED = Translation.Type.GUI.wrap("destruction.enabled");

    @Translation(comments = "Destruction mode disabled")
    private static final String DISABLED = Translation.Type.GUI.wrap("destruction.disabled");

    @Translation(comments = "Multiblock mining enabled")
    private static final String MULTIBLOCK_MINING_ENABLED = Translation.Type.GUI.wrap("multiblock_mining.enabled");

    @Translation(comments = "Multiblock mining disabled")
    private static final String MULTIBLOCK_MINING_DISABLED = Translation.Type.GUI.wrap("multiblock_mining.disabled");

    /** Currently this is only tracked for the local player */
    public static BlockPos centerOfDestruction = BlockPos.ZERO;

    /**
     * This code is mostly from {@link net.minecraft.client.renderer.LevelRenderer#renderLevel(DeltaTracker, boolean, Camera, GameRenderer, LightTexture, Matrix4f, Matrix4f)} <br>
     * From the section where the profiler starts tracking 'destroyProgress'
     */
    @SubscribeEvent
    @SuppressWarnings({"DataFlowIssue", "resource"}) // level should not be null / there is no resource to close
    public static void renderAdditionalBreakProgress(final RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player.isCrouching()) {
                return;
            }

            int radius = (int) player.getAttributeValue(DSAttributes.BLOCK_BREAK_RADIUS);

            if (radius < 1) {
                return;
            }

            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.multiMining == DragonStateHandler.MultiMining.DISABLED) {
                return;
            }

            Vec3 cameraPosition = event.getCamera().getPosition();
            double x = cameraPosition.x();
            double y = cameraPosition.y();
            double z = cameraPosition.z();

            LevelRendererAccess access = (LevelRendererAccess) event.getLevelRenderer();
            SortedSet<BlockDestructionProgress> set = access.dragonSurvival$getDestructionProgress().get(centerOfDestruction.asLong());
            int progress = set != null ? set.last().getProgress() : -1;

            if (progress == -1) {
                return;
            }

            BlockPos.betweenClosedStream(AABB.ofSize(centerOfDestruction.getCenter(), radius, radius, radius)).forEach(offsetPosition -> {
                double xDistance = (double) offsetPosition.getX() - x;
                double yDistance = (double) offsetPosition.getY() - y;
                double zDistance = (double) offsetPosition.getZ() - z;

                // Check if the position is close enough to be rendered
                if (!(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance > 1024)) {
                    event.getPoseStack().pushPose();
                    event.getPoseStack().translate((double) offsetPosition.getX() - x, (double) offsetPosition.getY() - y, (double) offsetPosition.getZ() - z);
                    PoseStack.Pose lastPose = event.getPoseStack().last();
                    VertexConsumer consumer = new SheetedDecalTextureGenerator(access.dragonSurvival$getRenderBuffers().crumblingBufferSource().getBuffer(ModelBakery.DESTROY_TYPES.get(progress)), lastPose, 1.0F);
                    ModelData modelData = access.dragonSurvival$getLevel().getModelData(offsetPosition);
                    Minecraft.getInstance().getBlockRenderer().renderBreakingTexture(access.dragonSurvival$getLevel().getBlockState(offsetPosition), offsetPosition, access.dragonSurvival$getLevel(), event.getPoseStack(), consumer, modelData);
                    event.getPoseStack().popPose();
                }
            });
        }
    }

    @SubscribeEvent
    public static void toggleDestructionMode(final InputEvent.Key event) {
        toggleDestructionMode(KeyHandler.checkAndGet(event, Keybind.TOGGLE_LARGE_DRAGON_DESTRUCTION, true));
    }

    @SubscribeEvent
    public static void toggleDestructionMode(final InputEvent.MouseButton.Pre event) {
        toggleDestructionMode(KeyHandler.checkAndGet(event, Keybind.TOGGLE_MULTI_MINING, true));
    }

    private static void toggleDestructionMode(Pair<Player, DragonStateHandler> data) {
        if (data == null) {
            return;
        }

        DragonStateHandler handler = data.getSecond();
        MiscCodecs.DestructionData destructionData = handler.stage().value().destructionData().orElse(null);

        if (destructionData == null || !destructionData.isDestructionAllowed(handler.getGrowth())) {
            return;
        }

        handler.largeDragonDestruction = Functions.cycleEnum(handler.largeDragonDestruction);
        data.getFirst().displayClientMessage(KeyHandler.cycledEnum(handler.largeDragonDestruction), true);

        PacketDistributor.sendToServer(new SyncLargeDragonDestruction(handler.largeDragonDestruction));
        Keybind.TOGGLE_LARGE_DRAGON_DESTRUCTION.consumeClick();
    }

    @SubscribeEvent
    public static void toggleMultiMining(final InputEvent.Key event) {
        toggleMultiMining(KeyHandler.checkAndGet(event, Keybind.TOGGLE_MULTI_MINING, true));
    }

    @SubscribeEvent
    public static void toggleMultiMining(final InputEvent.MouseButton.Pre event) {
        toggleMultiMining(KeyHandler.checkAndGet(event, Keybind.TOGGLE_MULTI_MINING, true));
    }

    private static void toggleMultiMining(Pair<Player, DragonStateHandler> data) {
        if (data == null) {
            return;
        }

        if (data.getFirst().getAttributeValue(DSAttributes.BLOCK_BREAK_RADIUS) < 1) {
            return;
        }

        DragonStateHandler handler = data.getSecond();
        handler.multiMining = Functions.cycleEnum(handler.multiMining);
        data.getFirst().displayClientMessage(KeyHandler.cycledEnum(handler.multiMining), true);

        PacketDistributor.sendToServer(new SyncMultiMining(handler.multiMining));
        Keybind.TOGGLE_MULTI_MINING.consumeClick();
    }
}
