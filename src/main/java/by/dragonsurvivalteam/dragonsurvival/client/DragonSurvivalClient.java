package by.dragonsurvivalteam.dragonsurvival.client;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.extensions.ShakeWhenUsedExtension;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.ClientDietComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.ClientTimeComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.DietComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.TimeComponent;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonBoots;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonChestplate;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonHelmet;
import by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor.DragonLeggings;
import by.dragonsurvivalteam.dragonsurvival.client.models.creatures.*;
import by.dragonsurvivalteam.dragonsurvival.client.models.projectiles.GenericBallModel;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.DragonBeaconRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.blocks.HelmetEntityRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.*;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.*;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DragonPartLoader;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.GeckoLibClient;

import java.util.Collections;
import java.util.Map;

@Mod(value = DragonSurvival.MODID, dist = Dist.CLIENT)
public class DragonSurvivalClient {
    public DragonSurvivalClient(IEventBus bus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        GeckoLibClient.init();

        bus.addListener(this::setup);
        bus.addListener(this::registerItemExtensions);
        bus.addListener(this::registerTooltips);
        bus.addListener(this::addReloadListeners);
    }

    private void setup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(DSEntities.BOLAS_ENTITY.get(), BolasEntityRenderer::new);
            EntityRenderers.register(DSEntities.GENERIC_ARROW_ENTITY.get(), GenericArrowRenderer::new);

            BlockEntityRenderers.register(DSBlockEntities.HELMET.get(), HelmetEntityRenderer::new);
            BlockEntityRenderers.register(DSBlockEntities.DRAGON_BEACON.get(), DragonBeaconRenderer::new);

            // GeckoLib renderers
            EntityRenderers.register(DSEntities.GENERIC_BALL_ENTITY.get(), manager -> new GenericBallRenderer(manager, new GenericBallModel()));

            EntityRenderers.register(DSEntities.DRAGON.get(), manager -> new DragonRenderer(manager, ClientDragonRenderer.dragonModel));
            EntityRenderers.register(DSEntities.HUNTER_KNIGHT.get(), manager -> new KnightRenderer(manager, new KnightModel()));
            EntityRenderers.register(DSEntities.HUNTER_SPEARMAN.get(), manager -> new SpearmanRenderer(manager, new SpearmanModel()));
            EntityRenderers.register(DSEntities.HUNTER_AMBUSHER.get(), manager -> new AmbusherRenderer(manager, new AmbusherModel()));
            EntityRenderers.register(DSEntities.HUNTER_HOUND.get(), manager -> new HoundRenderer(manager, new HoundModel()));
            EntityRenderers.register(DSEntities.HUNTER_GRIFFIN.get(), manager -> new GriffinRenderer(manager, new GriffinModel()));
            EntityRenderers.register(DSEntities.HUNTER_LEADER.get(), manager -> new LeaderRenderer(manager, new LeaderModel()));
        });
    }

    private void addReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new DragonPartLoader());
        event.registerReloadListener(new DefaultPartLoader());
    }

    private void registerTooltips(final RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(DietComponent.class, ClientDietComponent::new);
        event.register(TimeComponent.class, ClientTimeComponent::new);
    }

    private void registerItemExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new ShakeWhenUsedExtension(), DSItems.DRAGON_SOUL.value());

        // --- Light dragon armor --- //

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
                return createModel(entity, defaultModel, true, false, false, false);
            }
        }, DSItems.LIGHT_DRAGON_HELMET.value());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
                return createModel(entity, defaultModel, false, true, false, false);
            }
        }, DSItems.LIGHT_DRAGON_CHESTPLATE.value());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
                return createModel(entity, defaultModel, false, false, true, false);
            }
        }, DSItems.LIGHT_DRAGON_LEGGINGS.value());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
                return createModel(entity, defaultModel, false, false, false, true);
            }
        }, DSItems.LIGHT_DRAGON_BOOTS.value());

        // --- Dark dragon armor --- //

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
                return createModel(entity, defaultModel, true, false, false, false);
            }
        }, DSItems.DARK_DRAGON_HELMET.value());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
                return createModel(entity, defaultModel, false, true, false, false);
            }
        }, DSItems.DARK_DRAGON_CHESTPLATE.value());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
                return createModel(entity, defaultModel, false, false, true, false);
            }
        }, DSItems.DARK_DRAGON_LEGGINGS.value());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
                return createModel(entity, defaultModel, false, false, false, true);
            }
        }, DSItems.DARK_DRAGON_BOOTS.value());

        // TODO: This is part of the way to get the helmet block to render in hand correctly, not sure how to fix some of the other issues though
        /* event.registerItem(new IClientItemExtensions(){
            private final HelmetStackTileEntityRenderer renderer = new HelmetStackTileEntityRenderer();

            @Override
            public @NotNull HelmetStackTileEntityRenderer getCustomRenderer() {
                return renderer;
            }
        }, DSBlocks.HELMET_BLOCK_1_ITEM.get(), DSBlocks.HELMET_BLOCK_2_ITEM.get(), DSBlocks.HELMET_BLOCK_3_ITEM.get());*/
    }

    private HumanoidModel<?> createModel(final LivingEntity entity, final HumanoidModel<?> defaultModel, boolean head, boolean body, boolean leggings, boolean boots) {
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

    private ModelPart empty() {
        return new ModelPart(Collections.emptyList(), Collections.emptyMap());
    }

    private DragonHelmet<?> head() {
        return new DragonHelmet<>(Minecraft.getInstance().getEntityModels().bakeLayer(DragonHelmet.LAYER_LOCATION));
    }

    private DragonChestplate<?> body() {
        return new DragonChestplate<>(Minecraft.getInstance().getEntityModels().bakeLayer(DragonChestplate.LAYER_LOCATION));
    }

    private DragonLeggings<?> leggings() {
        return new DragonLeggings<>(Minecraft.getInstance().getEntityModels().bakeLayer(DragonLeggings.LAYER_LOCATION));
    }

    private DragonBoots<?> boots() {
        return new DragonBoots<>(Minecraft.getInstance().getEntityModels().bakeLayer(DragonBoots.LAYER_LOCATION));
    }
}