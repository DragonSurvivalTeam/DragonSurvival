package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.GrowthCrystalButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.PenaltyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.BarComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.DietMenuComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.ScrollableComponent;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscResources;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.compat.jei.JEIPlugin;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryCache;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonRidingHandler;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonSpeciesScreen extends Screen {
    @Translation(comments = {
            "■ In order to allow other players to mount you, you must crouch and they can right click on you to mount.",
            "\n§6■ Human players can ride you at below or equal to scale %s§r§7",
            "\n§6■ Dragon players can ride you at below or equal to scale %s§r§7"
    })
    private static final String RIDING_INFO = Translation.Type.GUI.wrap("dragon_species_screen.riding_info");

    @Translation(comments = "■ This species cannot gain the ability to fly.")
    public static final String FLIGHT_CANNOT_GAIN = Translation.Type.GUI.wrap("dragon_species_screen.flight_cannot_gain");

    @Translation(comments = "■ You currently cannot fly.")
    public static final String FLIGHT_CANNOT_FLY = Translation.Type.GUI.wrap("dragon_species_screen.flight_cannot_fly_or_spin");

    @Translation(comments = "■ You currently can fly.")
    public static final String FLIGHT_CAN_FLY = Translation.Type.GUI.wrap("dragon_species_screen.flight_can_fly");

    @Translation(comments = "■ You currently can spin.")
    public static final String FLIGHT_CAN_SPIN = Translation.Type.GUI.wrap("dragon_species_screen.flight_can_spin");

    @Translation(comments = "■ Players cannot ride this species.")
    private static final String RIDING_DISABLED = Translation.Type.GUI.wrap("dragon_species_screen.riding_disabled");

    @Translation(comments = "This species has no penalties.")
    private static final String NO_PENALTIES = Translation.Type.GUI.wrap("dragon_species_screen.no_penalties");

    @Translation(comments = "This species has no special diet.")
    private static final String NO_DIET = Translation.Type.GUI.wrap("dragon_species_screen.no_diet");

    private static final ResourceLocation BACKGROUND_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/species_background.png");
    private static final ResourceLocation RIDING_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/riding_hover.png");
    private static final ResourceLocation RIDING_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/riding_main.png");
    private static final ResourceLocation WINGS_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/wing_hover.png");
    private static final ResourceLocation WINGS_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/wing_main.png");

    private static final ResourceLocation PENALTIES_LEFT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/penalties_left_arrow_hover.png");
    private static final ResourceLocation PENALTIES_LEFT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/penalties_left_arrow_main.png");
    private static final ResourceLocation PENALTIES_RIGHT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/penalties_right_arrow_hover.png");
    private static final ResourceLocation PENALTIES_RIGHT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/species/penalties_right_arrow_main.png");

    public Holder<DragonSpecies> species;
    private Holder<DragonStage> stage;

    private final List<ScrollableComponent> scrollableComponents = new ArrayList<>();
    private DietMenuComponent dietMenu;
    private HoverButton growthButton;
    private ExtendedButton speciesBanner;
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
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (dietMenu != null && dietMenu.getHovered() != null && Compat.isModLoaded(Compat.JEI)) {
            return JEIPlugin.handleKeyPress(InputConstants.getKey(keyCode, scanCode), dietMenu.getHovered());
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
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

        for (ScrollableComponent component : scrollableComponents) {
            component.update();
        }

        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Hack to absolutely ensure the banner is rendered behind everything else
        speciesBanner.render(graphics, mouseX, mouseY, partialTick);

        for (Renderable renderable : this.renderables) {
            if(renderable != speciesBanner) {
                renderable.render(graphics, mouseX, mouseY, partialTick);
            }
        }
    }

    @Override
    public void renderBackground(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Don't render the vanilla background, it darkens the UI in an undesirable way
    }

    @Override
    public void init() {
        //noinspection DataFlowIssue -> player is present
        species = DragonStateProvider.getData(minecraft.player).species();
        stage = DragonStateProvider.getData(minecraft.player).stage();

        int xSize = 256;
        int ySize = 256;

        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize / 2) / 2;

        int startX = guiLeft + 13;
        int startY = guiTop + 17;

        TabButton.addTabButtonsToScreen(this, startX + 17, startY - 56, TabButton.TabButtonType.SPECIES_TAB);
        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);

        if (DietEntryCache.isEmpty(species)) {
            ExtendedButton noDietText = new ExtendedButton(startX + 77, startY + 30, 140, 20, Component.empty(), button -> {}){
                @Override
                public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                    final FormattedText buttonText = Minecraft.getInstance().font.ellipsize(this.getMessage(), this.width + 26); // Remove 6 pixels so that the text is always contained within the button's borders
                    TextRenderUtil.drawScaledText(graphics, this.getX(), this.getY() + (float) (this.height - 8) / 2, 0.8f, buttonText.getString(), getFGColor());
                }
            };
            noDietText.setMessage(Component.translatable(NO_DIET));
            addRenderableOnly(noDietText);
        } else {
            dietMenu = new DietMenuComponent(species, startX + 78, startY + 10);
            scrollableComponents.add(dietMenu);
            renderables.add(dietMenu);
        }

        // Dragon species banner
        speciesBanner = new ExtendedButton(startX + 17, startY - 22, 49, 147, Component.empty(), button -> {}){
            private boolean isTop(double mouseY) {
                return mouseY > getY() + 6 && mouseY < getY() + 100;
            }

            @Override
            public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                if (isHovered() && isTop(mouseY)) {
                    graphics.blit(data.species().value().miscResources().altarBanner(), getX(), getY(), 0, 0, 49, 147, 49, 294);
                    List<Either<FormattedText, TooltipComponent>> components = new ArrayList<>();
                    //noinspection DataFlowIssue -> key is present
                    components.addFirst(Either.left(Component.translatable(Translation.Type.DRAGON_SPECIES_INVENTORY_DESCRIPTION.wrap(species.getKey().location()))));
                    graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, components, mouseX, mouseY, ItemStack.EMPTY);
                } else {
                    graphics.blit(data.species().value().miscResources().altarBanner(), getX(), getY(), 0, 147, 49, 147, 49, 294);
                }
            }
        };
        addRenderableWidget(speciesBanner);

        // Wing button
        HoverButton wingButton = new HoverButton(startX + 79, startY - 19, 20, WINGS_MAIN, WINGS_HOVER);
        addRenderableWidget(wingButton);

        FlightData flightData = FlightData.getData(minecraft.player);
        MutableComponent flightTooltip = Component.empty();

        if (flightData.hasFlight) {
            flightTooltip = Component.translatable(FLIGHT_CAN_FLY);
        }

        if (flightData.hasSpin) {
            if (flightTooltip.getContents() != PlainTextContents.EMPTY) {
                flightTooltip.append("\n");
            }

            flightTooltip.append(Component.translatable(FLIGHT_CAN_SPIN));
        }

        if (!flightData.hasFlight && !flightData.hasSpin) {
            if(!MagicData.getData(minecraft.player).hasFlightGrantingAbility()) {
                flightTooltip.append(Component.translatable(FLIGHT_CANNOT_GAIN));
            } else {
                flightTooltip.append(Component.translatable(FLIGHT_CANNOT_FLY));
            }
        }

        wingButton.setTooltip(Tooltip.create(flightTooltip));

        // Growth stage button
        StageResources.GrowthIcon growthIcon = StageResources.getGrowthIcon(data.species(), data.stageKey());
        growthButton = new HoverButton(startX + 99, startY - 21, 20, growthIcon.icon(), growthIcon.hoverIcon(), () -> {
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

            crystalBar = new BarComponent(this,
                    startX + 130, startY - 19, 4,
                    crystals, 2,
                    -11, 39, 1, 12, 16,
                    textures.growthLeftArrow().hoverIcon(), textures.growthLeftArrow().icon(), textures.growthRightArrow().hoverIcon(), textures.growthRightArrow().icon());

            scrollableComponents.add(crystalBar);
        }

        // Riding button
        HoverButton ridingButton = new HoverButton(startX + 186, startY - 18, 16, RIDING_MAIN, RIDING_HOVER);
        if(data.body().value().mountingOffsets().isPresent()) {
            ridingButton.setTooltip(Tooltip.create(Component.translatable(RIDING_INFO, String.format("%.2f", (minecraft.player.getScale() * DragonRidingHandler.PLAYER_RIDING_SCALE_RATIO)), String.format("%.2f", (minecraft.player.getScale() * DragonRidingHandler.DRAGON_RIDING_SCALE_RATIO)))));
        } else {
            ridingButton.setTooltip(Tooltip.create(Component.translatable(RIDING_DISABLED)));
        }
        addRenderableWidget(ridingButton);

        // Body type button
        DragonBodyButton bodyTypeButton = new DragonBodyButton(this, startX + 29, startY + 101, 25, 25, data.body(), false, button -> {});
        addRenderableWidget(bodyTypeButton);

        // Penalties bar
        List<AbstractWidget> penalties = data.species().value().penalties().stream().filter(penalty -> penalty.value().icon().isPresent()).map(penalty -> (AbstractWidget) new PenaltyButton(0, 0, penalty)).toList();

        if (!penalties.isEmpty()) {
            scrollableComponents.add(new BarComponent(this,
                    startX + 85, startY + 85, 3,
                    penalties, 5,
                    -10, 116, 10, 9, 16,
                    PENALTIES_LEFT_ARROW_HOVER, PENALTIES_LEFT_ARROW_MAIN, PENALTIES_RIGHT_ARROW_HOVER, PENALTIES_RIGHT_ARROW_MAIN));
        } else {
            ExtendedButton noPenaltiesText = new ExtendedButton(startX + 82, startY + 100, 140, 10, Component.empty(), button -> { /* Nothing to do */ }) {
                @Override
                public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                    final FormattedText buttonText = Minecraft.getInstance().font.ellipsize(this.getMessage(), this.width + 26); // Remove 6 pixels so that the text is always contained within the button's borders
                    TextRenderUtil.drawScaledText(graphics, this.getX(), this.getY() + (float) (this.height - 8) / 2, 0.8f, buttonText.getString(), getFGColor());
                }
            };
            noPenaltiesText.setMessage(Component.translatable(NO_PENALTIES));
            addRenderableOnly(noPenaltiesText);
        }
    }


    @Override
    public void tick() {
        //noinspection DataFlowIssue -> players should be present
        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);

        if (species == null) {
            onClose();
        }

        if (species != data.species() || stage != data.stage()) {
            species = data.species();
            stage = data.stage();
            clearWidgets();
            init();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}