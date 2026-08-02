package by.dragonsurvivalteam.dragonsurvival.client;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.DragonPenaltyHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.DragonSoulBar;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.GrowthHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.SpinHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.ClientDietComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.ClientTimeComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.DietComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.TimeComponent;
import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonBoots;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonChestplate;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonHelmet;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonLeggings;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.AmbusherModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.GriffinModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.HoundModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.KnightModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.LeaderModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.SpearmanModel;
import by.dragonsurvivalteam.dragonsurvival.client.models.projectiles.GenericBallModel;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.DragonBeaconRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.DragonSoulRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.DragonVaultRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.HelmetEntityRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.AmbusherRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.GriffinRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.HoundRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.KnightRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.LeaderRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.SpearmanRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.BolasEntityRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.GenericArrowRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.GenericBallRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DragonPartLoader;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.compat.curios.CuriosButtonHandler;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.LocalPlayerAccessor;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class DragonSurvivalClient {
    private static final float TIMER_INCREMENT = 0.01f;

    public static float TIMER;
    public static DragonRenderer DRAGON_RENDERER; // Needed for access in LevelRendererMixin

    public static DragonModel DRAGON_MODEL = new DragonModel();
    public static AmbusherModel AMBUSHER_MODEL = new AmbusherModel();

    public DragonSurvivalClient(final IEventBus bus) {
        bus.addListener(this::setup);
        bus.addListener(this::addReloadListeners);
        bus.addListener(this::registerGuiLayers);
        bus.addListener(this::registerTooltips);

        MinecraftForge.EVENT_BUS.addListener(this::incrementTimer);
        MinecraftForge.EVENT_BUS.addListener(this::preventThirdPersonWhenSuffocating);

        if (ModID.CURIOS.isLoaded()) {
            MinecraftForge.EVENT_BUS.addListener(CuriosButtonHandler::handleCurios);
        }
    }

    private void incrementTimer(final ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (TIMER + TIMER_INCREMENT > Float.MAX_VALUE) {
            TIMER = 0;
        } else {
            TIMER += TIMER_INCREMENT;
        }
    }

    private void setup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(DSEntities.BOLAS_ENTITY.get(), BolasEntityRenderer::new);
            EntityRenderers.register(DSEntities.GENERIC_ARROW_ENTITY.get(), GenericArrowRenderer::new);

            BlockEntityRenderers.register(DSBlockEntities.HELMET.get(), HelmetEntityRenderer::new);
            BlockEntityRenderers.register(DSBlockEntities.DRAGON_BEACON.get(), DragonBeaconRenderer::new);
            BlockEntityRenderers.register(DSBlockEntities.DRAGON_SOUL.get(), DragonSoulRenderer::new);
            BlockEntityRenderers.register(DSBlockEntities.DRAGON_VAULT.get(), DragonVaultRenderer::new);

            // GeckoLib renderers
            EntityRenderers.register(DSEntities.GENERIC_BALL_ENTITY.get(), manager -> new GenericBallRenderer(manager, new GenericBallModel()));
            EntityRenderers.register(DSEntities.DRAGON.get(), manager -> {
                DRAGON_RENDERER = new DragonRenderer(manager, DRAGON_MODEL);
                return DRAGON_RENDERER;
            });
            EntityRenderers.register(DSEntities.HUNTER_KNIGHT.get(), manager -> new KnightRenderer(manager, new KnightModel()));
            EntityRenderers.register(DSEntities.HUNTER_SPEARMAN.get(), manager -> new SpearmanRenderer(manager, new SpearmanModel()));
            EntityRenderers.register(DSEntities.HUNTER_AMBUSHER.get(), manager -> new AmbusherRenderer(manager, AMBUSHER_MODEL));
            EntityRenderers.register(DSEntities.HUNTER_HOUND.get(), manager -> new HoundRenderer(manager, new HoundModel()));
            EntityRenderers.register(DSEntities.HUNTER_GRIFFIN.get(), manager -> new GriffinRenderer(manager, new GriffinModel()));
            EntityRenderers.register(DSEntities.HUNTER_LEADER.get(), manager -> new LeaderRenderer(manager, new LeaderModel()));
        });
    }

    private void addReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new DragonPartLoader());
        event.registerReloadListener(new DefaultPartLoader());
    }

    private void registerGuiLayers(final RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(), DragonPenaltyHUD.ID.getPath(),
                (gui, graphics, partialTick, width, height) -> DragonPenaltyHUD.render(graphics, partialTick));
        event.registerAbove(DragonPenaltyHUD.ID, MagicHUD.ID.getPath(),
                (gui, graphics, partialTick, width, height) -> MagicHUD.render(graphics, partialTick));
        event.registerAbove(MagicHUD.ID, GrowthHUD.ID.getPath(),
                (gui, graphics, partialTick, width, height) -> GrowthHUD.render(graphics, partialTick));
        event.registerAbove(GrowthHUD.ID, DragonSoulBar.ID.getPath(),
                (gui, graphics, partialTick, width, height) -> DragonSoulBar.render(graphics, partialTick));
        event.registerAbove(MagicHUD.ID, SpinHUD.ID.getPath(),
                (gui, graphics, partialTick, width, height) -> SpinHUD.render(graphics, partialTick));
    }

    private void registerTooltips(final RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(DietComponent.class, ClientDietComponent::new);
        event.register(TimeComponent.class, ClientTimeComponent::new);
    }

    private void preventThirdPersonWhenSuffocating(final ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = DragonSurvival.PROXY.getLocalPlayer();

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        if (((LocalPlayerAccessor) player).dragonSurvival$suffocatesAt(BlockPos.containing(player.position()))) {
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    public static IClientItemExtensions createArmorExtension(final ArmorItem.Type type) {
        return new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
                return createModel(entity, defaultModel,
                        type == ArmorItem.Type.HELMET,
                        type == ArmorItem.Type.CHESTPLATE,
                        type == ArmorItem.Type.LEGGINGS,
                        type == ArmorItem.Type.BOOTS);
            }
        };
    }

    private static HumanoidModel<?> createModel(final LivingEntity entity, final HumanoidModel<?> defaultModel, boolean head, boolean body, boolean leggings, boolean boots) {
        HumanoidModel<?> model = new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                "hat", empty(),
                "head", head ? head().head : empty(),
                "body", body ? body().body : empty(),
                "right_arm", body ? body().right_arm : empty(),
                "left_arm", body ? body().left_arm : empty(),
                "right_leg", leggings ? leggings().right_leg : boots ? boots().right_shoe : empty(),
                "left_leg", leggings ? leggings().left_leg : boots ? boots().left_shoe : empty()
        )));

        model.crouching = entity.isShiftKeyDown();
        model.riding = defaultModel.riding;
        model.young = entity.isBaby();

        return model;
    }

    private static ModelPart empty() {
        return new ModelPart(Collections.emptyList(), Collections.emptyMap());
    }

    private static DragonHelmet<?> head() {
        return new DragonHelmet<>(Minecraft.getInstance().getEntityModels().bakeLayer(DragonHelmet.LAYER_LOCATION));
    }

    private static DragonChestplate<?> body() {
        return new DragonChestplate<>(Minecraft.getInstance().getEntityModels().bakeLayer(DragonChestplate.LAYER_LOCATION));
    }

    private static DragonLeggings<?> leggings() {
        return new DragonLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(DragonLeggings.LAYER_LOCATION));
    }

    private static DragonBoots<?> boots() {
        return new DragonBoots<>(Minecraft.getInstance().getEntityModels().bakeLayer(DragonBoots.LAYER_LOCATION));
    }
}
