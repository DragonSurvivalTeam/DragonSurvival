package by.jackraidenph.dragonsurvival.registration;

import by.jackraidenph.dragonsurvival.BeaconParticle;
import by.jackraidenph.dragonsurvival.DragonSurvivalMod;
import by.jackraidenph.dragonsurvival.gecko.PrinceRenderer;
import by.jackraidenph.dragonsurvival.gecko.*;
import by.jackraidenph.dragonsurvival.gui.DragonScreen;
import by.jackraidenph.dragonsurvival.handlers.ClientEvents;
import by.jackraidenph.dragonsurvival.magic.entity.Renderer.BallLightningRenderer;
import by.jackraidenph.dragonsurvival.magic.entity.Renderer.ChainLightningRenderer;
import by.jackraidenph.dragonsurvival.magic.entity.Renderer.DragonSpikeRenderer;
import by.jackraidenph.dragonsurvival.magic.entity.Renderer.FireBallRenderer;
import by.jackraidenph.dragonsurvival.magic.entity.models.FireballModel;
import by.jackraidenph.dragonsurvival.magic.entity.models.LightningBallModel;
import by.jackraidenph.dragonsurvival.nest.NestScreen;
import by.jackraidenph.dragonsurvival.renderer.PrincessRenderer;
import by.jackraidenph.dragonsurvival.renderer.*;
import by.jackraidenph.dragonsurvival.shader.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("unused")
public class ClientModEvents {

    public static KeyBinding TOGGLE_WINGS;
    public static KeyBinding DRAGON_INVENTORY;
    
    //Abilities
    public static KeyBinding USE_ABILITY;
    public static KeyBinding TOGGLE_ABILITIES;
    public static KeyBinding NEXT_ABILITY;
    public static KeyBinding PREV_ABILITY;
    public static KeyBinding ABILITY1;
    public static KeyBinding ABILITY2;
    public static KeyBinding ABILITY3;
    public static KeyBinding ABILITY4;
    
    @SubscribeEvent
    public static void onTextureStitchEvent(TextureStitchEvent.Pre event) {
        event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/cage"));
        event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/wind"));
        event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/open_eye"));
        event.addSprite(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/wind_vertical"));
        DragonSurvivalMod.LOGGER.info("Successfully added sprites!");
    }

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event) {
        Minecraft minecraft = event.getMinecraftSupplier().get();

        RenderTypeLookup.setRenderLayer(BlockInit.dragon_altar_stone, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.dragon_altar_sandstone, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.dragon_altar_red_sandstone, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.dragon_altar_purpur_block, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.dragon_altar_oak_log, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.dragon_altar_nether_bricks, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.dragon_altar_mossy_cobblestone, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.dragon_altar_blackstone, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.mediumCaveNest, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.mediumForestNest, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.mediumSeaNest, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.bigCaveNest, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.bigForestNest, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.bigSeaNest, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.birchDoor, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.acaciaDoor, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.peaceDragonBeacon, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.fireDragonBeacon, RenderType.cutout());
        RenderTypeLookup.setRenderLayer(BlockInit.magicDragonBeacon, RenderType.cutout());
    
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.DRAGON_SPIKE, DragonSpikeRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.CHAIN_LIGHTNING, ChainLightningRenderer::new);
    
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.MAGICAL_BEAST, MagicalPredatorRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.BOLAS_ENTITY, manager -> new SpriteRenderer<>(manager, minecraft.getItemRenderer()));

        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.PRINCESS, manager -> new PrincessRenderer(manager, (IReloadableResourceManager) minecraft.getResourceManager()));
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.HUNTER_HOUND, by.jackraidenph.dragonsurvival.renderer.HunterHoundRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.SHOOTER_HUNTER, by.jackraidenph.dragonsurvival.renderer.ShooterHunterRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.SQUIRE_HUNTER, by.jackraidenph.dragonsurvival.renderer.SquireHunterRenderer::new);

        ClientRegistry.bindTileEntityRenderer(TileEntityTypesInit.PREDATOR_STAR_TILE_ENTITY_TYPE, PredatorStarTESR::new);
        ClientRegistry.bindTileEntityRenderer(TileEntityTypesInit.helmetTile, HelmetEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(TileEntityTypesInit.dragonBeacon, DragonBeaconRenderer::new);
        ShaderHelper.initShaders();

        ScreenManager.register(Containers.nestContainer, NestScreen::new);
        ScreenManager.register(Containers.dragonContainer, DragonScreen::new);

        TOGGLE_WINGS = new KeyBinding("ds.keybind.wings", GLFW.GLFW_KEY_G, "Dragon Survival");
        ClientRegistry.registerKeyBinding(TOGGLE_WINGS);
    
        DRAGON_INVENTORY = new KeyBinding("ds.keybind.dragon_inv", GLFW.GLFW_KEY_UNKNOWN, "Dragon Survival");
        ClientRegistry.registerKeyBinding(DRAGON_INVENTORY);
    
        USE_ABILITY = new KeyBinding("ds.keybind.use_ability", GLFW.GLFW_KEY_C, "Dragon Survival");
        ClientRegistry.registerKeyBinding(USE_ABILITY);
    
        TOGGLE_ABILITIES = new KeyBinding("ds.keybind.toggle_abilities", GLFW.GLFW_KEY_X, "Dragon Survival");
        ClientRegistry.registerKeyBinding(TOGGLE_ABILITIES);
    
        NEXT_ABILITY = new KeyBinding("ds.keybind.next_ability", GLFW.GLFW_KEY_R, "Dragon Survival");
        ClientRegistry.registerKeyBinding(NEXT_ABILITY);
    
        PREV_ABILITY = new KeyBinding("ds.keybind.prev_ability", GLFW.GLFW_KEY_F, "Dragon Survival");
        ClientRegistry.registerKeyBinding(PREV_ABILITY);
    
        ABILITY1 = new KeyBinding("ds.keybind.ability1", GLFW.GLFW_KEY_UNKNOWN, "Dragon Survival");
        ClientRegistry.registerKeyBinding(ABILITY1);
    
        ABILITY2 = new KeyBinding("ds.keybind.ability2", GLFW.GLFW_KEY_UNKNOWN, "Dragon Survival");
        ClientRegistry.registerKeyBinding(ABILITY2);
    
        ABILITY3 = new KeyBinding("ds.keybind.ability3", GLFW.GLFW_KEY_UNKNOWN, "Dragon Survival");
        ClientRegistry.registerKeyBinding(ABILITY3);
    
        ABILITY4 = new KeyBinding("ds.keybind.ability4", GLFW.GLFW_KEY_UNKNOWN, "Dragon Survival");
        ClientRegistry.registerKeyBinding(ABILITY4);
        
        //Gecko renderers
        DragonModel dragonModel = new DragonModel();
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.BALL_LIGHTNING, manager -> new BallLightningRenderer(manager, new LightningBallModel()));
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.FIREBALL, manager -> new FireBallRenderer(manager, new FireballModel()));
    
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.DRAGON, manager -> new DragonRenderer(manager, ClientEvents.dragonModel = dragonModel));
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.DRAGON_ARMOR, manager -> new DragonRenderer(manager, ClientEvents.dragonArmorModel = new DragonArmorModel(dragonModel)));
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.KNIGHT, manager -> new KnightRenderer(manager, new KnightModel()));
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.PRINCESS_ON_HORSE, manager -> new by.jackraidenph.dragonsurvival.gecko.PrincessRenderer(manager, new PrincessModel()));
        RenderingRegistry.registerEntityRenderingHandler(EntityTypesInit.PRINCE_ON_HORSE, manager -> new PrinceRenderer(manager, new PrinceModel()));
    }

    @SubscribeEvent
    public static void registerParticleFactories(ParticleFactoryRegisterEvent factoryRegisterEvent) {
        ParticleManager particleManager = Minecraft.getInstance().particleEngine;
        particleManager.register(ParticleRegistry.fireBeaconParticle, p_create_1_ -> new IParticleFactory<BasicParticleType>() {
            @Nullable
            @Override
            public Particle createParticle(BasicParticleType p_199234_1_, ClientWorld clientWorld, double v, double v1, double v2, double v3, double v4, double v5) {
                BeaconParticle beaconParticle = new BeaconParticle(clientWorld, v, v1, v2, v3, v4, v5);
                beaconParticle.pickSprite(p_create_1_);
                return beaconParticle;
            }
        });
        particleManager.register(ParticleRegistry.magicBeaconParticle, p_create_1_ -> new IParticleFactory<BasicParticleType>() {
            @Nullable
            @Override
            public Particle createParticle(BasicParticleType p_199234_1_, ClientWorld clientWorld, double v, double v1, double v2, double v3, double v4, double v5) {
                BeaconParticle beaconParticle = new BeaconParticle(clientWorld, v, v1, v2, v3, v4, v5);
                beaconParticle.pickSprite(p_create_1_);
                return beaconParticle;
            }
        });
        particleManager.register(ParticleRegistry.peaceBeaconParticle, p_create_1_ -> new IParticleFactory<BasicParticleType>() {
            @Nullable
            @Override
            public Particle createParticle(BasicParticleType p_199234_1_, ClientWorld clientWorld, double v, double v1, double v2, double v3, double v4, double v5) {
                BeaconParticle beaconParticle = new BeaconParticle(clientWorld, v, v1, v2, v3, v4, v5);
                beaconParticle.pickSprite(p_create_1_);
                return beaconParticle;
            }
        });
    }
    
}
