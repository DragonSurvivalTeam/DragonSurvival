package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonSkinsScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonSpeciesScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverDisableable;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.BodyIcons;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DragonBodyButton extends ExtendedButton implements HoverDisableable {
    private static final int MAX_TOOLTIP_LINES = 14;

    @Translation(comments = "You can only change the body type in the altar when changing the dragon's species.")
    private static final String UNAVAILABLE = Translation.Type.GUI.wrap("dragon_body_button.unavailable");

    @Translation(comments = "This body type has not been unlocked yet.")
    private static final String NOT_UNLOCKED = Translation.Type.GUI.wrap("dragon_body_button.not_unlocked");

    @Translation(comments = "\n§6--- Body Modifiers ---§r§7")
    private static final String MODIFIERS = Translation.Type.GUI.wrap("dragon_body_button.modifiers");

    private static final ResourceLocation SELECTED_BACKGROUND = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/icon_skin_on.png");
    private static final ResourceLocation DESELECTED_BACKGROUND = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/icon_skin_off.png");

    public static final int HOVERED = 1;
    public static final int SELECTED = 2;
    private static final int LOCKED = 3;

    private final Screen screen;
    private final Holder<DragonBody> dragonBody;
    private final ResourceLocation icon;
    private final ResourceLocation bodyLocation;
    private boolean disableHover;
    private final boolean useBackground;
    private final boolean noTooltip;
    private List<FormattedCharSequence> tooltip = List.of();
    private int tooltipScroll;
    private int tooltipLineCount;

    public enum LockedReason {
        NOT_UNLOCKED,
        NOT_IN_ALTAR,
        NONE
    }

    private final LockedReason lockedReason;

    public DragonBodyButton(Screen screen, int x, int y, int xSize, int ySize, final Holder<DragonBody> dragonBody, LockedReason locked, OnPress action) {
        this(screen, x, y, xSize, ySize, dragonBody, Objects.requireNonNull(dragonBody.getKey()).location(), locked, action, false, false);
    }

    public DragonBodyButton(Screen screen, int x, int y, int xSize, int ySize, final Holder<DragonBody> dragonBody, LockedReason locked, OnPress action, boolean useBackground, boolean noTooltip) {
        this(screen, x, y, xSize, ySize, dragonBody, Objects.requireNonNull(dragonBody.getKey()).location(), locked, action, useBackground, noTooltip);
    }

    private DragonBodyButton(Screen screen, int x, int y, int xSize, int ySize, final Holder<DragonBody> dragonBody, final ResourceLocation location, LockedReason locked, OnPress action, boolean useBackground, boolean noTooltip) {
        super(x, y, xSize, ySize, Component.empty(), action, DEFAULT_NARRATION);

        ResourceKey<DragonSpecies> species = null;

        if (screen instanceof DragonEditorScreen dragonEditorScreen) {
            species = dragonEditorScreen.species.getKey();
        } else if (screen instanceof DragonSpeciesScreen dragonSpeciesScreen) {
            species = dragonSpeciesScreen.species.getKey();
        }

        if (species == null) {
            this.icon = dragonBody.value().defaultIcon().orElse(DragonSurvival.MISSING_TEXTURE);
        } else {
            this.icon = BodyIcons.getIcon(dragonBody, species);
        }

        this.screen = screen;
        this.dragonBody = dragonBody;
        this.bodyLocation = location;
        this.lockedReason = locked;
        this.useBackground = useBackground;
        this.noTooltip = noTooltip;

        if (!noTooltip && locked == LockedReason.NONE) {
            updateTooltip();
        }
    }

    public void disableHover() {
        this.disableHover = true;
    }

    public void enableHover() {
        this.disableHover = false;
    }

    public boolean isHovered() {
        boolean hovered = !disableHover && super.isHovered();

        if (!hovered && tooltipScroll > 0) {
            tooltipScroll = 0;
            updateTooltip();
        }

        return hovered;
    }

    public boolean isFocused() {
        return !disableHover && super.isFocused();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!noTooltip && isMouseOver(mouseX, mouseY) && lockedReason == LockedReason.NONE) {
            int oldScroll = tooltipScroll;
            tooltipScroll = Math.clamp(tooltipScroll + Double.compare(0, scrollY), 0, maxTooltipScroll());

            if (oldScroll != tooltipScroll) {
                updateTooltip();
            }

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public LockedReason lockedReason() {
        return lockedReason;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int state = 0;

        if (isSelected()) {
            state = SELECTED;
        } else if (lockedReason != LockedReason.NONE) {
            state = LOCKED;
        } else if (isHoveredOrFocused()) {
            state = HOVERED;
        }

        if (!noTooltip) {
            if (state == LOCKED) {
                if (lockedReason == LockedReason.NOT_IN_ALTAR) {
                    setTooltip(Tooltip.create(Component.translatable(UNAVAILABLE)));
                } else if (lockedReason == LockedReason.NOT_UNLOCKED) {
                    setTooltip(Tooltip.create(Component.translatable(NOT_UNLOCKED)));
                }
            } else if (isHovered()) {
                Minecraft.getInstance().screen.setTooltipForNextRenderPass(
                        tooltip,
                        new MenuTooltipPositioner(new ScreenRectangle(getX(), getY(), width, height)),
                        false
                );
            }
        }

        if (this.useBackground) {
            ResourceLocation background = state == SELECTED ? SELECTED_BACKGROUND : DESELECTED_BACKGROUND;
            graphics.blit(background, getX(), getY(), 0, 0, this.width, this.height, 35, 35);
            graphics.blit(icon, getX() + 5, getY() + 5, 0, state * 25, 25, 25, 32, 104);
        } else {
            graphics.blit(icon, getX(), getY(), 0, state * this.height, this.width, this.height, 32, 104);
        }
    }

    private boolean isSelected() {
        if (screen instanceof DragonEditorScreen dragonEditorScreen) {
            return DragonUtils.isBody(dragonBody, dragonEditorScreen.body);
        }

        if (screen instanceof DragonSkinsScreen skinsScreen) {
            return DragonUtils.isBody(dragonBody, skinsScreen.handler.body());
        }

        return false;
    }

    private void updateTooltip() {
        List<Component> components = new ArrayList<>();
        components.add(Component.translatable(Translation.Type.BODY_DESCRIPTION.wrap(bodyLocation)));
        components.add(Component.translatable(MODIFIERS));

        for (Modifier modifier : dragonBody.value().modifiers()) {
            components.add(modifier.getFormattedDescription(1, true));
        }

        MutableComponent tooltipComponent = Component.empty();

        for (int i = 0; i < components.size(); i++) {
            tooltipComponent.append(components.get(i));

            if (i < components.size() - 1) {
                tooltipComponent.append("\n");
            }
        }

        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(tooltipComponent, 200);
        List<FormattedCharSequence> shownTooltip = new ArrayList<>();
        tooltipLineCount = lines.size();
        tooltipScroll = Math.clamp(tooltipScroll, 0, maxTooltipScroll());

        for (int line = tooltipScroll; line < lines.size() && line - tooltipScroll < MAX_TOOLTIP_LINES; line++) {
            shownTooltip.add(lines.get(line));
        }

        tooltip = shownTooltip;
    }

    private int maxTooltipScroll() {
        return Math.max(0, tooltipLineCount - MAX_TOOLTIP_LINES);
    }
}
