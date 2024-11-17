package by.dragonsurvivalteam.dragonsurvival.client;

import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.SourceOfMagicScreen;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.Keybind;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.KnightModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.PrinceModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.PrincessHorseModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.projectiles.FireballModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.projectiles.LightningBallModel;
import by.dragonsurvivalteam.dragonsurvival.client.particles.BeaconParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaSweepParticle;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.DragonBeaconRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.HelmetEntityRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.*;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.BallLightningRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.BolasEntityRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.DragonSpikeRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.FireBallRenderer;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSContainers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DSTileEntities;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT )
@SuppressWarnings( "unused" )
public class ClientModEvents{
	@SubscribeEvent
	public static void setup(FMLClientSetupEvent event)
	{
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_stone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_sandstone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_red_sandstone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_purpur_block, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_oak_log, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_nether_bricks, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_mossy_cobblestone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_blackstone, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.dragon_altar_birch_log, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.birchDoor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.acaciaDoor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.peaceDragonBeacon, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.fireDragonBeacon, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.magicDragonBeacon, RenderType.cutout());

		// Enable transparecny for certain small doors
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.birchSmallDoor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(DSBlocks.acaciaSmallDoor, RenderType.cutout());

		EntityRenderers.register(DSEntities.DRAGON_SPIKE.get(), DragonSpikeRenderer::new);

		EntityRenderers.register(DSEntities.BOLAS_ENTITY.get(), BolasEntityRenderer::new);

		EntityRenderers.register(DSEntities.PRINCESS.get(), PrincessRenderer::new);
		EntityRenderers.register(DSEntities.HUNTER_HOUND.get(), HunterHoundRenderer::new);
		EntityRenderers.register(DSEntities.SHOOTER_HUNTER.get(), ShooterHunterRenderer::new);
		EntityRenderers.register(DSEntities.SQUIRE_HUNTER.get(), SquireHunterRenderer::new);

		BlockEntityRenderers.register(DSTileEntities.helmetTile, HelmetEntityRenderer::new);
		BlockEntityRenderers.register(DSTileEntities.dragonBeacon, DragonBeaconRenderer::new);

		//ShaderHelper.initShaders();

		MenuScreens.register(DSContainers.nestContainer, SourceOfMagicScreen::new);
		MenuScreens.register(DSContainers.dragonContainer, DragonScreen::new);

		//Gecko renderers
		EntityRenderers.register(DSEntities.BALL_LIGHTNING.get(), manager -> new BallLightningRenderer(manager, new LightningBallModel()));
		EntityRenderers.register(DSEntities.FIREBALL.get(), manager -> new FireBallRenderer(manager, new FireballModel()));

		EntityRenderers.register(DSEntities.DRAGON.get(), manager -> new DragonRenderer(manager, ClientDragonRender.dragonModel));
		EntityRenderers.register(DSEntities.DRAGON_ARMOR.get(), manager -> new DragonRenderer(manager, ClientDragonRender.dragonArmorModel));
		EntityRenderers.register(DSEntities.KNIGHT.get(), manager -> new KnightRenderer(manager, new KnightModel()));
		EntityRenderers.register(DSEntities.PRINCESS_ON_HORSE.get(), manager -> new PrincessHorseRenderer(manager, new PrincessHorseModel()));
		EntityRenderers.register(DSEntities.PRINCE_ON_HORSE.get(), manager -> new PrinceHorseRenderer(manager, new PrinceModel()));
	}

	@SubscribeEvent
	public static void onKeyRegister(final RegisterKeyMappingsEvent event) {
		Keybind.registerKeys(event);
	}

	@SubscribeEvent
	public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(DSParticles.fireBeaconParticle, spriteSet -> (particleType, clientWorld, x, y, z, speedX, speedY, speedZ) -> {
			BeaconParticle beaconParticle = new BeaconParticle(clientWorld, x, y, z, speedX, speedY, speedZ);
			beaconParticle.pickSprite(spriteSet);
			return beaconParticle;
		});

		event.registerSpriteSet(DSParticles.magicBeaconParticle, spriteSet -> (particleType, clientWorld, x, y, z, speedX, speedY, speedZ) -> {
			BeaconParticle beaconParticle = new BeaconParticle(clientWorld, x, y, z, speedX, speedY, speedZ);
			beaconParticle.pickSprite(spriteSet);
			return beaconParticle;
		});

		event.registerSpriteSet(DSParticles.peaceBeaconParticle, spriteSet -> (particleType, clientWorld, x, y, z, speedX, speedY, speedZ) -> {
			BeaconParticle beaconParticle = new BeaconParticle(clientWorld, x, y, z, speedX, speedY, speedZ);
			beaconParticle.pickSprite(spriteSet);
			return beaconParticle;
		});

		event.registerSpriteSet(DSParticles.seaSweep, spriteSet -> (particleType, clientWorld, x, y, z, speedX, speedY, speedZ) -> {
			SeaSweepParticle beaconParticle = new SeaSweepParticle(clientWorld, x, y, z, speedX, spriteSet);
			beaconParticle.pickSprite(spriteSet);
			return beaconParticle;
		});
	}
}