package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.GrowthCrystalButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.PenaltyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.*;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthIcon;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscDragonTextures;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonSpeciesScreen extends Screen {
    private static final ResourceLocation BACKGROUND_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/species_background.png");
    private static final ResourceLocation RIDING_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/riding_hover.png");
    private static final ResourceLocation RIDING_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/riding_main.png");
    private static final ResourceLocation WINGS_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/wing_hover.png");
    private static final ResourceLocation WINGS_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/wing_main.png");

    private static final ResourceLocation PENALTIES_LEFT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/penalties_left_arrow_hover.png");
    private static final ResourceLocation PENALTIES_LEFT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/penalties_left_arrow_main.png");
    private static final ResourceLocation PENALTIES_RIGHT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/penalties_right_arrow_hover.png");
    private static final ResourceLocation PENALTIES_RIGHT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/penalties_right_arrow_main.png");

    public Screen sourceScreen;

    public Holder<DragonType> dragonType;
    private Holder<DragonStage> dragonStage;
    private int guiLeft;
    private int guiTop;
    private int growthTooltipScroll;
    private HoverButton growthButton;
    private ScrollableComponent crystalBar;

    private final List<ScrollableComponent> scrollableComponents = new ArrayList<>();

    public DragonSpeciesScreen(Screen sourceScreen) {
        super(Component.empty());
        this.sourceScreen = sourceScreen;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (ScrollableComponent component : scrollableComponents) {
            // Ignore scrolling on the crystal bar; we need scrolling to be available for the tooltip to work
            if(component == crystalBar) {
                continue;
            }
            component.scroll(mouseX, mouseY, scrollX, scrollY);
        }

        if(growthButton.isHovered()) {
            growthTooltipScroll += (int) -scrollY; // invert the value so that scrolling down shows further entries
        } else {
            growthTooltipScroll = 0;
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

        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);
        guiGraphics.blit(BACKGROUND_MAIN, startX, startY, 0, 0, 256, 256);
        guiGraphics.blit(data.getType().value().miscResources().altarBanner(), startX + 7, startY + 8, 0, 0, 49, 147, 49, 294);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Don't render the vanilla background, it darkens the UI in an undesirable way
    }

    @Override
    public void init() {
        dragonType = DragonStateProvider.getData(minecraft.player).getType();
        dragonStage = DragonStateProvider.getData(minecraft.player).getStage();

        int xSize = 256;
        int ySize = 256;

        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize / 2) / 2;

        int startX = guiLeft + 13;
        int startY = guiTop + 17;

        TabButton.addTabButtonsToScreen(this, startX + 17, startY - 56, TabButton.Type.SPECIES_TAB);
        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);
        scrollableComponents.add(new DietMenuComponent());

        // Wing button
        HoverButton wingButton = new HoverButton(startX + 79, startY - 19, 20, 20, 0, 0, 20, 20, button -> {}, WINGS_HOVER, WINGS_MAIN);
        addRenderableWidget(wingButton);
        FlightData flightData = FlightData.getData(minecraft.player);
        MutableComponent flightTooltip = Component.empty();
        if(flightData.hasFlight) {
            flightTooltip.append(Component.translatable(LangKey.FLIGHT_CAN_FLY));
        }
        if(flightData.hasSpin) {
            if(flightData.hasFlight) {
                flightTooltip.append("\n");
            }
            flightTooltip.append(Component.translatable(LangKey.FLIGHT_CAN_SPIN));
        }

        if(!flightData.hasFlight && !flightData.hasSpin) {
            flightTooltip.append(Component.translatable(LangKey.FLIGHT_CAN_FLY));
        }
        wingButton.setTooltip(Tooltip.create(flightTooltip));

        // Growth stage button
        GrowthIcon growthIcon = data.getType().value().getGrowthIcon(data.getStage());
        growthButton = new HoverButton(startX + 99, startY - 21, 20, 20, 0, 0, 20, 20,
                () -> {
                        DragonStateHandler handler = DragonStateProvider.getData(minecraft.player);
                        Pair<List<Either<FormattedText, TooltipComponent>>, Integer> growthDescriptionResult = handler.getGrowthDescription(growthTooltipScroll);
                        List<Either<FormattedText, TooltipComponent>> components = growthDescriptionResult.getFirst();
                        growthTooltipScroll = growthDescriptionResult.getSecond();

                        return components;
                },
                button -> {}, growthIcon.hoverIcon(), growthIcon.icon());
        addRenderableWidget(growthButton);

        // Growth stage crystals
        List<Holder<DragonStage>> stages = data.getStagesSortedByProgression(minecraft.player.registryAccess());
        if(!stages.isEmpty()) {
            List<AbstractWidget> crystals = stages.stream().map(stage -> (AbstractWidget)(new GrowthCrystalButton(0, 0, stage))).toList();
            MiscDragonTextures textures = data.getType().value().miscResources();
            crystalBar = new BarComponent(this,
                    startX + 130, startY - 19, 4,
                    crystals, 10,
                    -11, 39, 1, 12, 16, 12, 16,
                    textures.growthLeftArrow().hoverIcon(), textures.growthLeftArrow().icon(), textures.growthRightArrow().hoverIcon(), textures.growthRightArrow().icon());
            scrollableComponents.add(crystalBar);
        }

        // Riding button
        HoverButton ridingButton = new HoverButton(startX + 186, startY - 18, 16, 16, 0, 0, 16, 16, button -> {}, RIDING_HOVER, RIDING_MAIN);
        addRenderableWidget(ridingButton);


        // Body type button
        DragonBodyButton bodyTypeButton = new DragonBodyButton(this, startX + 29, startY + 92, 25, 25, data.getBody(), false, button -> {});
        addRenderableWidget(bodyTypeButton);

        // Penalties bar
        List<AbstractWidget> penalties = data.getType().value().penalties().stream().map(penalty -> (AbstractWidget)(new PenaltyButton(0, 0, penalty))).toList();
        scrollableComponents.add(new BarComponent(this,
                startX + 85, startY + 85, 3,
                penalties, 40,
                -10, 116, 10, 9, 16, 20, 20,
                PENALTIES_LEFT_ARROW_MAIN, PENALTIES_LEFT_ARROW_HOVER, PENALTIES_RIGHT_ARROW_MAIN, PENALTIES_RIGHT_ARROW_HOVER));
    }


    @Override
    public void tick() {
        //noinspection DataFlowIssue -> players should be present
        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);
        if(dragonType == null) {
            onClose();
        }

        if(dragonType != data.getType() || dragonStage != data.getStage()) {
            dragonType = data.getType();
            dragonStage = data.getStage();
            clearWidgets();
            init();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}