package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.GrowthCrystalButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.PenaltyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.BarComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.DietMenuComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.ScrollableComponent;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthIcon;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscDragonTextures;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonRidingHandler;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public Holder<DragonType> dragonType;

    private final List<ScrollableComponent> scrollableComponents = new ArrayList<>();
    private Holder<DragonStage> dragonStage;
    private HoverButton growthButton;
    private ScrollableComponent crystalBar;

    private int guiLeft;
    private int guiTop;
    private int growthTooltipScroll;

    public DragonSpeciesScreen() {
        super(Component.empty());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (ScrollableComponent component : scrollableComponents) {
            // Ignore scrolling on the crystal bar; we need scrolling to be available for the tooltip to work
            if (component == crystalBar) {
                continue;
            }

            component.scroll(mouseX, mouseY, scrollX, scrollY);
        }

        if (growthButton.isHovered()) {
            // invert the value so that scrolling down shows further entries
            growthTooltipScroll += (int) -scrollY;
        } else {
            growthTooltipScroll = 0;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void setFocused(@Nullable final GuiEventListener listener) {
        if (listener instanceof DragonBodyButton) {
            // Button can be clicked (and therefor focused) but there is no reason to do so in this screen
            return;
        }

        super.setFocused(listener);
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        renderBlurredBackground(partialTick);

        int startX = guiLeft + 23;
        int startY = guiTop - 13;

        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);
        graphics.blit(BACKGROUND_MAIN, startX, startY, 0, 0, 256, 256);
        graphics.blit(data.getType().value().miscResources().altarBanner(), startX + 7, startY + 8, 0, 0, 49, 147, 49, 294);

        for (ScrollableComponent component : scrollableComponents) {
            component.update();
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Don't render the vanilla background, it darkens the UI in an undesirable way
    }

    @Override
    public void init() {
        //noinspection DataFlowIssue -> player is present
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

        DietMenuComponent dietMenu = new DietMenuComponent(dragonType, startX + 78, startY + 10);
        scrollableComponents.add(dietMenu);
        renderables.add(dietMenu);

        // Wing button
        HoverButton wingButton = new HoverButton(startX + 79, startY - 19, 20, WINGS_MAIN, WINGS_HOVER);
        addRenderableWidget(wingButton);

        FlightData flightData = FlightData.getData(minecraft.player);
        MutableComponent flightTooltip = Component.empty();

        if (flightData.hasFlight) {
            flightTooltip = Component.translatable(LangKey.FLIGHT_CAN_FLY);
        }

        if (flightData.hasSpin) {
            if (flightTooltip.getContents() != PlainTextContents.EMPTY) {
                flightTooltip.append("\n");
            }

            flightTooltip.append(Component.translatable(LangKey.FLIGHT_CAN_SPIN));
        }

        if (!flightData.hasFlight && !flightData.hasSpin) {
            flightTooltip.append(Component.translatable(LangKey.FLIGHT_CANNOT_FLY));
        }

        wingButton.setTooltip(Tooltip.create(flightTooltip));

        // Growth stage button
        GrowthIcon growthIcon = data.getType().value().getGrowthIcon(data.getStage());
        growthButton = new HoverButton(startX + 99, startY - 21, 20, growthIcon.icon(), growthIcon.hoverIcon(),
                () -> {
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
            MiscDragonTextures textures = data.getType().value().miscResources();

            crystalBar = new BarComponent(this,
                    startX + 130, startY - 19, 4,
                    crystals, 10,
                    -11, 39, 1, 12, 16, 12, 16,
                    textures.growthLeftArrow().hoverIcon(), textures.growthLeftArrow().icon(), textures.growthRightArrow().hoverIcon(), textures.growthRightArrow().icon());

            scrollableComponents.add(crystalBar);
        }

        // Riding button
        HoverButton ridingButton = new HoverButton(startX + 186, startY - 18, 16, RIDING_MAIN, RIDING_HOVER);
        ridingButton.setTooltip(Tooltip.create(Component.translatable(LangKey.RIDING_INFO, DragonRidingHandler.PLAYER_RIDING_SIZE, (int) (data.getSize() / 2))));
        addRenderableWidget(ridingButton);

        // Body type button
        DragonBodyButton bodyTypeButton = new DragonBodyButton(this, startX + 29, startY + 92, 25, 25, data.getBody(), false, button -> {});
        addRenderableWidget(bodyTypeButton);

        // Penalties bar
        List<AbstractWidget> penalties = data.getType().value().penalties().stream().map(penalty -> (AbstractWidget) new PenaltyButton(0, 0, penalty)).toList();
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

        if (dragonType == null) {
            onClose();
        }

        if (dragonType != data.getType() || dragonStage != data.getStage()) {
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