package by.dragonsurvivalteam.dragonsurvival.client.render.blocks;

import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonBeaconTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DragonBeaconRenderer implements BlockEntityRenderer<DragonBeaconTileEntity>{

	public DragonBeaconRenderer(BlockEntityRendererProvider.Context pContext){}

	@Override
	public void render(DragonBeaconTileEntity dragonBeaconEntity, float v, PoseStack PoseStack, MultiBufferSource iRenderTypeBuffer, int light, int overlay){
		dragonBeaconEntity.tick += 0.5;
		PoseStack.pushPose();
		DragonBeaconTileEntity.Type type = dragonBeaconEntity.type;

		Item item = DSBlocks.DRAGON_BEACON.asItem();

		ClientLevel clientWorld = (ClientLevel)dragonBeaconEntity.getLevel();
		Minecraft minecraft = Minecraft.getInstance();
		RandomSource random = clientWorld.random;
		double x = 0.25 + random.nextInt(5) / 10d;
		double z = 0.25 + random.nextInt(5) / 10d;

		boolean hasMemoryBlock = dragonBeaconEntity.getLevel().getBlockState(dragonBeaconEntity.getBlockPos().below()).is(DSBlocks.DRAGON_MEMORY_BLOCK);

		switch(type){
			case PEACE -> {
				item = hasMemoryBlock ? DSItems.PASSIVE_PEACE_BEACON : DSItems.INACTIVE_PEACE_DRAGON_BEACON;

				if(!minecraft.isPaused() && dragonBeaconEntity.tick % 5 == 0 && hasMemoryBlock){
					clientWorld.addParticle(DSParticles.peaceBeaconParticle, dragonBeaconEntity.getX() + x, dragonBeaconEntity.getY() + 0.5, dragonBeaconEntity.getZ() + z, 0, 0, 0);
				}
			}
			case MAGIC -> {
				item = hasMemoryBlock ? DSItems.PASSIVE_MAGIC_BEACON : DSItems.INACTIVE_MAGIC_DRAGON_BEACON;

				if(!minecraft.isPaused() && dragonBeaconEntity.tick % 5 == 0 && hasMemoryBlock){
					clientWorld.addParticle(DSParticles.magicBeaconParticle, dragonBeaconEntity.getX() + x, dragonBeaconEntity.getY() + 0.5, dragonBeaconEntity.getZ() + z, 0, 0, 0);
				}
			}
			case FIRE -> {
				item = hasMemoryBlock ? DSItems.PASSIVE_FIRE_BEACON : DSItems.INACTIVE_FIRE_DRAGON_BEACON;

				if(!minecraft.isPaused() && dragonBeaconEntity.tick % 5 == 0 && hasMemoryBlock){
					clientWorld.addParticle(DSParticles.fireBeaconParticle, dragonBeaconEntity.getX() + x, dragonBeaconEntity.getY() + 0.5, dragonBeaconEntity.getZ() + z, 0, 0, 0);
				}
			}
		}

		float f1 = Mth.sin(((float)dragonBeaconEntity.tick + v) / 20.0F + dragonBeaconEntity.bobOffs) * 0.1F + 0.1F;
		PoseStack.translate(0.5, 0.25 + f1 / 2f, 0.5);
		PoseStack.mulPose(Axis.YP.rotationDegrees(dragonBeaconEntity.tick));
		PoseStack.scale(2, 2, 2);
		Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(item), ItemDisplayContext.GROUND, light, overlay, PoseStack, iRenderTypeBuffer, clientWorld, 0);
		PoseStack.popPose();
	}
}