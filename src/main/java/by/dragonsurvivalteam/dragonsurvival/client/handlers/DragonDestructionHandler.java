package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.LevelRendererAccess;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDestructionEnabled;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncMultiblockMiningEnabled;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MultiblockMiningToggled;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import org.checkerframework.checker.units.qual.C;
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
            int radius = (int)player.getAttributeValue(DSAttributes.BLOCK_BREAK_RADIUS);

            if (radius < 1 || player.isCrouching() || !player.getData(DSDataAttachments.MULTIBLOCK_MINING_TOGGLED).enabled) {
                return;
            }

            Vec3 cameraPosition = event.getCamera().getPosition();
            double x = cameraPosition.x();
            double y = cameraPosition.y();
            double z = cameraPosition.z();

            LevelRendererAccess access = (LevelRendererAccess) event.getLevelRenderer();
            SortedSet<BlockDestructionProgress> set = access.dragonSurvival$getDestructionProgress().get(centerOfDestruction.asLong());
            int progress = set != null ? set.last().getProgress() : -1;

            if (progress != -1) {
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
    }

    @SubscribeEvent
    public static void toggleDestructionMode(final InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null || event.getAction() != Keybind.KEY_PRESSED || !Keybind.TOGGLE_DESTRUCTION.isKey(event.getKey())) {
            return;
        }

        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return;
        }

        MiscCodecs.DestructionData destructionData = data.stage().value().destructionData().orElse(null);

        if (destructionData == null || !destructionData.isDestructionAllowed(data.getSize())) {
            return;
        }

        Keybind.TOGGLE_DESTRUCTION.consumeClick();
        data.setDestructionEnabled(!data.getDestructionEnabled());
        PacketDistributor.sendToServer(new SyncDestructionEnabled.Data(player.getId(), data.getDestructionEnabled()));
        player.displayClientMessage(Component.translatable(data.getDestructionEnabled() ? ENABLED : DISABLED), true);
    }

    @SubscribeEvent
    public static void toggleMultiblockMining(final InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null || event.getAction() != Keybind.KEY_PRESSED || !Keybind.TOGGLE_MULTIBLOCK_MINING.isKey(event.getKey())) {
            return;
        }

        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (player.getAttributeValue(DSAttributes.BLOCK_BREAK_RADIUS) < 1) {
            return;
        }

        MultiblockMiningToggled data = player.getData(DSDataAttachments.MULTIBLOCK_MINING_TOGGLED);
        Keybind.TOGGLE_MULTIBLOCK_MINING.consumeClick();
        data.enabled = !data.enabled;
        PacketDistributor.sendToServer(new SyncMultiblockMiningEnabled(data.enabled));
        player.displayClientMessage(Component.translatable(data.enabled ? MULTIBLOCK_MINING_ENABLED : MULTIBLOCK_MINING_DISABLED), true);
    }
}
