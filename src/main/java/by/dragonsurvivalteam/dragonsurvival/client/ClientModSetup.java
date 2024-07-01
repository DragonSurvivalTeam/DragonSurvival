package by.dragonsurvivalteam.dragonsurvival.client;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.KeyInputHandler;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.KnightModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.projectiles.FireballModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.projectiles.LightningBallModel;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.DragonBeaconRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.HelmetEntityRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.*;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.BallLightningRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.BolasEntityRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.DragonSpikeRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.FireBallRenderer;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTileEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber( bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT )
@SuppressWarnings( "unused" )
public class ClientModSetup {
	@SubscribeEvent
	public static void setup(FMLClientSetupEvent event)
	{
		EntityRenderers.register(DSEntities.DRAGON_SPIKE.get(), DragonSpikeRenderer::new);

		EntityRenderers.register(DSEntities.BOLAS_ENTITY.get(), BolasEntityRenderer::new);

		EntityRenderers.register(DSEntities.HUNTER_HOUND.get(), HunterHoundRenderer::new);
		EntityRenderers.register(DSEntities.SHOOTER_HUNTER.get(), ShooterHunterRenderer::new);
		EntityRenderers.register(DSEntities.SQUIRE_HUNTER.get(), SquireHunterRenderer::new);

		BlockEntityRenderers.register(DSTileEntities.HELMET_TILE.get(), HelmetEntityRenderer::new);
		BlockEntityRenderers.register(DSTileEntities.DRAGON_BEACON.get(), DragonBeaconRenderer::new);

		//ShaderHelper.initShaders();

		//Gecko renderers
		EntityRenderers.register(DSEntities.BALL_LIGHTNING.get(), manager -> new BallLightningRenderer(manager, new LightningBallModel()));
		EntityRenderers.register(DSEntities.FIREBALL.get(), manager -> new FireBallRenderer(manager, new FireballModel()));

		EntityRenderers.register(DSEntities.DRAGON.get(), manager -> new DragonRenderer(manager, ClientDragonRenderer.dragonModel));
		EntityRenderers.register(DSEntities.DRAGON_ARMOR.get(), manager -> new DragonRenderer(manager, ClientDragonRenderer.dragonArmorModel));
		EntityRenderers.register(DSEntities.KNIGHT.get(), manager -> new KnightRenderer(manager, new KnightModel()));
	}

	@SubscribeEvent
	public static void onKeyRegister(final RegisterKeyMappingsEvent event) {
		KeyInputHandler.registerKeys(event);
	}
}