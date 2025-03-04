package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.GrowthCrystalButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ClickHoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.BarComponent;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscResources;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawMenuToggle;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenVanillaInventory;
import by.dragonsurvivalteam.dragonsurvival.network.container.SortInventory;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonInventoryScreen extends EffectRenderingInventoryScreen<DragonContainer> {
    @Translation(comments = "Toggle showing claws and teeth textures on your model.")
    private static final String TOGGLE_CLAWS = Translation.Type.GUI.wrap("dragon_inventory.toggle_claws");

    @Translation(comments = "Sort inventory")
    private static final String SORT_INVENTORY = Translation.Type.GUI.wrap("dragon_inventory.sort_inventory");

    @Translation(comments = "Open vanilla inventory screen")
    private static final String TOGGLE_VANILLA_INVENTORY = Translation.Type.GUI.wrap("dragon_inventory.toggle_vanilla_inventory");

    @Translation(comments = "Open the config screen")
    private static final String TOGGLE_CONFIG = Translation.Type.GUI.wrap("inventory.toggle_config");

    @Translation(comments = {
            "■ Just put §6any tools§r§f here in your claw slots and your bare paw will borrow their aspect as long as they are intact.",
            "§7■ Does not stack with §2«Claws and Teeth»§r§7 skill, which only applies if these slots are empty."
    })
    private static final String HELP_CLAWS = Translation.Type.GUI.wrap("help.claws");

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/dragon_inventory.png");
    private static final ResourceLocation CLAWS_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/dragon_claws.png");

    private static final ResourceLocation CLAW_ARROW_CLICK = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/claw_arrow_left_click.png");
    private static final ResourceLocation CLAW_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/claw_arrow_left_hover.png");
    private static final ResourceLocation CLAW_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/claw_arrow_left_main.png");

    private static final ResourceLocation CLAW_DISPLAY_ON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/claw_display_on.png");
    private static final ResourceLocation CLAW_DISPLAY_OFF = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/claw_display_off.png");

    private static final ResourceLocation INFO_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/info_hover.png");
    private static final ResourceLocation INFO_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/info_main.png");

    private static final ResourceLocation CONFIG_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/config_hover.png");
    private static final ResourceLocation CONFIG_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/config_main.png");

    private static final ResourceLocation VANILLA_INVENTORY_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/vanilla_inventory_hover.png");
    private static final ResourceLocation VANILLA_INVENTORY_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/vanilla_inventory_main.png");

    private static final ResourceLocation SORT_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/sort_hover.png");
    private static final ResourceLocation SORT_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory/sort_main.png");

    public static double mouseX = -1;
    public static double mouseY = -1;

    private HoverButton growthButton;
    private int growthTooltipScroll;

    private boolean clawsMenu;
    private final Player player;
    private final List<ExtendedButton> clawMenuButtons = new ArrayList<>();

    public DragonInventoryScreen(DragonContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        player = inv.player;
        clawsMenu = ClawInventoryData.getData(player).isMenuOpen();
        imageWidth = 203;
        imageHeight = 166;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (growthButton.isHovered()) {
            // invert the value so that scrolling down shows further entries
            growthTooltipScroll += (int) -scrollY;
        } else {
            growthTooltipScroll = 0;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    @SuppressWarnings("DataFlowIssue") // minecraft gets set from setScreen -> init
    protected void init() {
        super.init();

        if (mouseX != -1 && mouseY != -1) {
            InputConstants.grabOrReleaseMouse(minecraft.getWindow().getWindow(), 212993, mouseX, mouseY);
            mouseX = -1;
            mouseY = -1;
        }

        leftPos = (width - imageWidth) / 2;

        ClawInventoryData clawInventory = ClawInventoryData.getData(player);

        clawsMenu = clawInventory.isMenuOpen();
        PacketDistributor.sendToServer(new SyncDragonClawMenuToggle(clawsMenu));

        TabButton.addTabButtonsToScreen(this, leftPos + 5, topPos - 26, TabButton.TabButtonType.INVENTORY_TAB);

        ClickHoverButton clawMenuArrow = new ClickHoverButton(leftPos - 8, topPos + 73, 10, 18, 0, 0, 18, 18, Component.empty(), button -> {
            clawsMenu = !clawsMenu;
            PacketDistributor.sendToServer(new SyncDragonClawMenuToggle(clawsMenu));
        }, CLAW_ARROW_CLICK, CLAW_ARROW_HOVER, CLAW_ARROW_MAIN);
        addRenderableWidget(clawMenuArrow);

        // Info button at the bottom of the claw menu
        HoverButton infoButton = new HoverButton(leftPos - 23, topPos + 4, 12, INFO_MAIN, INFO_HOVER);
        infoButton.setTooltip(Tooltip.create(Component.translatable(HELP_CLAWS)));
        addRenderableWidget(infoButton);
        clawMenuButtons.add(infoButton);

        // Button to enable / disable the rendering of claws (extra x and y offset is the location of the button in the texture)
        ExtendedButton clawRenderButton = new ExtendedButton(leftPos - 30 + 7, topPos + 120 + 27, 10, 10, Component.translatable(TOGGLE_CLAWS), button -> {
            ClientDragonRenderer.renderDragonClaws = !ClientDragonRenderer.renderDragonClaws;
            PacketDistributor.sendToServer(new SyncDragonClawRender(player.getId(), ClientDragonRenderer.renderDragonClaws));
        }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { /* Texture is rendered separately */ }
        };
        clawRenderButton.setTooltip(Tooltip.create(Component.translatable(TOGGLE_CLAWS)));
        addRenderableWidget(clawRenderButton);
        clawMenuButtons.add(clawRenderButton);

        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);

        // Growth stage button
        StageResources.GrowthIcon growthIcon = StageResources.getGrowthIcon(data.species(), data.stageKey());
        growthButton = new HoverButton(leftPos + 175, topPos + 4, 20, growthIcon.icon(), growthIcon.hoverIcon(), () -> {
            DragonStateHandler handler = DragonStateProvider.getData(minecraft.player);
            Pair<List<Either<FormattedText, TooltipComponent>>, Integer> growthDescriptionResult = handler.getGrowthDescription(growthTooltipScroll);
            List<Either<FormattedText, TooltipComponent>> components = growthDescriptionResult.getFirst();
            growthTooltipScroll = growthDescriptionResult.getSecond();

            return components;
        });
        addRenderableWidget(growthButton);

        // Growth stage crystals
        List<Holder<DragonStage>> stages = data.getStagesSortedByProgression(minecraft.player.registryAccess());

        if (!stages.isEmpty()) {
            List<AbstractWidget> crystals = stages.stream().map(stage -> (AbstractWidget) new GrowthCrystalButton(0, 0, stage)).toList();
            MiscResources textures = data.species().value().miscResources();

            new BarComponent(this,
                    leftPos + 124, topPos + 6, 4,
                    crystals, 2,
                    -11, 39, 1, 12, 16,
                    textures.growthLeftArrow().hoverIcon(), textures.growthLeftArrow().icon(), textures.growthRightArrow().hoverIcon(), textures.growthRightArrow().icon());
        }

        // Vanilla inventory
        HoverButton vanillaInventoryButton = new HoverButton(leftPos + 177, topPos + 84, 18, 16, 18, 18, VANILLA_INVENTORY_MAIN, VANILLA_INVENTORY_HOVER, button -> {
            Minecraft.getInstance().setScreen(new InventoryScreen(player));
            PacketDistributor.sendToServer(RequestOpenVanillaInventory.INSTANCE);
        });
        vanillaInventoryButton.setTooltip(Tooltip.create(Component.translatable(TOGGLE_VANILLA_INVENTORY)));
        addRenderableWidget(vanillaInventoryButton);

        // Config
        HoverButton configButton = new HoverButton(leftPos + 177, topPos + 102, 18, 16, 18, 18, CONFIG_MAIN, CONFIG_HOVER, button -> {
            Minecraft minecraft = Minecraft.getInstance();
            // Copied from ConfigHelper.java
            Optional<Screen> configScreen = ModList.get()
                    .getModContainerById(MODID)
                    .flatMap(m -> {
                        IModInfo modInfo = m.getModInfo();
                        return IConfigScreenFactory.getForMod(modInfo)
                                .map(f -> f.createScreen(m, minecraft.screen));
                    });
            minecraft.setScreen(configScreen.orElse(null));
        });
        configButton.setTooltip(Tooltip.create(Component.translatable(TOGGLE_CONFIG)));
        addRenderableWidget(configButton);

        // Sorting button
        HoverButton sortInventoryButton = new HoverButton(leftPos + 177, topPos + 120, 18, 16, 18, 18, SORT_MAIN, SORT_HOVER, button -> {
            PacketDistributor.sendToServer(SortInventory.INSTANCE);
        });
        sortInventoryButton.setTooltip(Tooltip.create(Component.translatable(SORT_INVENTORY)));
        addRenderableWidget(sortInventoryButton);
    }

    @Override
    protected void renderLabels(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY) { /* Nothing to do */ }

    @Override
    protected void renderBg(@NotNull final GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        RenderSystem.disableBlend();

        int scissorY1 = topPos + 77;
        int scissorX1 = leftPos + 101;
        int scissorX0 = leftPos + 25;
        int scissorY0 = topPos + 8;

        // In order to scale up the smaller dragon sizes, since they are too small otherwise
        int scale = (int) (20 * player.getScale());
        scale = Math.clamp(scale, 10, 40); // Very large dragon sizes (above the default max. size) will have a < 20 scale value

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, scissorX0, scissorY0, scissorX1, scissorY1, scale, 0, mouseX, mouseY, player);

        if (!clawsMenu) {
            return;
        }

        guiGraphics.blit(CLAWS_TEXTURE, leftPos - 33, topPos, 0, 0, 77, 170);
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        clawMenuButtons.forEach(button -> button.visible = clawsMenu);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (clawsMenu) {
            ResourceLocation texture = ClientDragonRenderer.renderDragonClaws ? CLAW_DISPLAY_ON : CLAW_DISPLAY_OFF;
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 100);
            graphics.blit(texture, leftPos - 30, topPos + 120, 0, 0, 24, 42, 24, 42);
            graphics.pose().popPose();
        }

        renderTooltip(graphics, mouseX, mouseY);

        // Bandaid solution since we are rendering the tooltip twice now
        // But with this it is guaranteed to be rendered after the slot icons
        growthButton.renderTooltip(graphics, mouseX, mouseY);
    }
}