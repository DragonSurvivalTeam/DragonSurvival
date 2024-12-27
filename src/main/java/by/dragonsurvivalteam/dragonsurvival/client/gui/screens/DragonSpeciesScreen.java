package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.AbilityButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.LevelButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ClickHoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.*;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonSpeciesScreen extends Screen {
    private static final ResourceLocation BACKGROUND_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/species_background.png");
    private static final ResourceLocation RIDING_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/riding_hover.png");
    private static final ResourceLocation RIDING_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/riding_main.png");
    private static final ResourceLocation WINGS_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/wings_hover.png");
    private static final ResourceLocation WINGS_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/wings_main.png");
    private static final ResourceLocation GROWTH_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/growth_hover.png");
    private static final ResourceLocation GROWTH_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/growth_main.png");

    private static final int ABILITIES_ON_HOTBAR = 4;

    public Screen sourceScreen;
    public LevelButton hoveredLevelButton;

    private Holder<DragonType> type;
    private int guiLeft;
    private int guiTop;

    private DietMenuComponent dietMenuComponent;
    private PenaltyBarComponent penaltyBarComponent;
    private GrowthCrystalsComponent growthCrystalsComponent;
    private HoverButton growthButton;
    private HoverButton wingsButton;
    private HoverButton rideButton;
    private HoverButton speciesButton;

    private final List<ScrollableComponent> scrollableComponents = new ArrayList<>();

    public DragonSpeciesScreen(Screen sourceScreen) {
        super(Component.empty());
        this.sourceScreen = sourceScreen;
    }

    public List<? extends GuiEventListener> widgetList() {
        return children();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (ScrollableComponent component : scrollableComponents) {
            component.scroll(mouseX, mouseY, scrollX, scrollY);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        for(ScrollableComponent component : scrollableComponents) {
            component.update();
        }

        this.renderBlurredBackground(partialTick);

        int startX = guiLeft + 23;
        int startY = guiTop - 13;

        guiGraphics.blit(BACKGROUND_MAIN, startX, startY, 0, 0, 256, 256);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Don't render the vanilla background, it darkens the UI in an undesirable way
    }

    @Override
    public void init() {
        int xSize = 256;
        int ySize = 256;

        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize / 2) / 2;

        int startX = guiLeft + 13;
        int startY = guiTop + 17;

        //Inventory
        addRenderableWidget(new TabButton(startX + 5 + 10, startY - 26 - 30, TabButton.Type.INVENTORY_TAB, this));
        addRenderableWidget(new TabButton(startX + 34 + 10, startY - 26 - 30, TabButton.Type.ABILITY_TAB, this));
        addRenderableWidget(new TabButton(startX + 63 + 10, startY - 28 - 30, TabButton.Type.SPECIES_TAB, this));
        addRenderableWidget(new TabButton(startX + 91 + 10, startY - 26 - 30, TabButton.Type.SKINS_TAB, this));

        scrollableComponents.add(new DietMenuComponent());
        scrollableComponents.add(new PenaltyBarComponent());
        scrollableComponents.add(new GrowthCrystalsComponent());
    }


    @Override
    public void tick() {
        //noinspection DataFlowIssue -> players should be present
        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);
        if(type != data.getType()) {
            type = data.getType();
            clearWidgets();
            init();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}