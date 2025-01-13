package by.dragonsurvivalteam.dragonsurvival.client.render.blocks;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonBeaconBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DragonBeaconRenderer implements BlockEntityRenderer<DragonBeaconBlockEntity> {
    public DragonBeaconRenderer(final BlockEntityRendererProvider.Context ignored) { /* Nothing to do */ }

    @Override
    public void render(final DragonBeaconBlockEntity beacon, final float partialTick, final PoseStack pose, @NotNull final MultiBufferSource buffer, final int packedLight, final int packedOverlay) {
        beacon.tick += 0.5f;
        pose.pushPose();

        Level level = Objects.requireNonNull(beacon.getLevel());
        Item item = DSBlocks.EMPTY_DRAGON_BEACON.value().asItem();

        boolean hasMemoryBlock = level.getBlockState(beacon.getBlockPos().below()).is(DSBlocks.DRAGON_MEMORY_BLOCK);
        boolean isPaused = Minecraft.getInstance().isPaused();

        double x = beacon.getBlockPos().getX() + (0.25 + level.getRandom().nextInt(5) / 10d);
        double y = beacon.getBlockPos().getY() + 0.5;
        double z = beacon.getBlockPos().getZ() + (0.25 + level.getRandom().nextInt(5) / 10d);

        switch (beacon.type) {
            case PEACE -> {
                item = hasMemoryBlock ? DSItems.PASSIVE_PEACE_BEACON.value() : DSItems.INACTIVE_PEACE_DRAGON_BEACON.value();

                if (!isPaused && beacon.tick % 5 == 0 && hasMemoryBlock) {
                    level.addParticle(DSParticles.FOREST_BEACON_PARTICLE.value(), x, y, z, 0, 0, 0);
                }
            }
            case MAGIC -> {
                item = hasMemoryBlock ? DSItems.PASSIVE_MAGIC_BEACON.value() : DSItems.INACTIVE_MAGIC_DRAGON_BEACON.value();

                if (!isPaused && beacon.tick % 5 == 0 && hasMemoryBlock) {
                    level.addParticle(DSParticles.SEA_BEACON_PARTICLE.value(), x, y, z, 0, 0, 0);
                }
            }
            case FIRE -> {
                item = hasMemoryBlock ? DSItems.PASSIVE_FIRE_BEACON.value() : DSItems.INACTIVE_FIRE_DRAGON_BEACON.value();

                if (!isPaused && beacon.tick % 5 == 0 && hasMemoryBlock) {
                    level.addParticle(DSParticles.CAVE_BEACON_PARTICLE.value(), x, y, z, 0, 0, 0);
                }
            }
        }

        float bounce = Mth.sin((beacon.tick + partialTick) / 20 + beacon.bobOffset) * 0.1f + 0.1f;
        pose.translate(0.5, 0.25 + bounce / 2f, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(beacon.tick));
        pose.scale(2, 2, 2);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(item), ItemDisplayContext.GROUND, packedLight, packedOverlay, pose, buffer, level, 0);
        pose.popPose();
    }
}