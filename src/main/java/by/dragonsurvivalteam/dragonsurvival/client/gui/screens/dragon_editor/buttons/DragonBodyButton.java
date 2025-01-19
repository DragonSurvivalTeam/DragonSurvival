package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonSkinsScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonSpeciesScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverDisableable;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.TextureManagerAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DragonBodyButton extends ExtendedButton implements HoverDisableable {

    @Translation(comments = "You can only change the body type in the altar when changing the dragon's species.")
    private static final String UNAVAILABLE = Translation.Type.GUI.wrap("dragon_body_button.unavailable");

    private static final String LOCATION_PREFIX = "textures/gui/custom/body/";
    private static final String DEFAULT_SUFFIX = "default";

    private static final ResourceLocation SELECTED_BACKGROUND = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/icon_skin_on.png");
    private static final ResourceLocation DESELECTED_BACKGROUND = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/icon_skin_off.png");

    public static final int HOVERED = 1;
    public static final int SELECTED = 2;
    private static final int LOCKED = 3;

    private final Screen screen;
    private final Holder<DragonBody> dragonBody;
    private final ResourceLocation iconLocation;
    private final ResourceLocation bodyLocation;
    private final boolean locked;
    private boolean disableHover;
    private final boolean useBackground;
    private final boolean noTooltip;

    public DragonBodyButton(Screen screen, int x, int y, int xSize, int ySize, final Holder<DragonBody> dragonBody, boolean locked, OnPress action) {
        this(screen, x, y, xSize, ySize, dragonBody, Objects.requireNonNull(dragonBody.getKey()).location(), locked, action, false, false);
    }

    public DragonBodyButton(Screen screen, int x, int y, int xSize, int ySize, final Holder<DragonBody> dragonBody, boolean locked, OnPress action, boolean useBackground, boolean noTooltip) {
        this(screen, x, y, xSize, ySize, dragonBody, Objects.requireNonNull(dragonBody.getKey()).location(), locked, action, useBackground, noTooltip);
    }

    @SuppressWarnings("DataFlowIssue") // key is expected to be present
    private DragonBodyButton(Screen screen, int x, int y, int xSize, int ySize, final Holder<DragonBody> dragonBody, final ResourceLocation location, boolean locked, OnPress action, boolean useBackground, boolean noTooltip) {
        super(x, y, xSize, ySize, Component.empty(), action, DEFAULT_NARRATION);
        if(!noTooltip) {
            setTooltip(Tooltip.create(Component.translatable(Translation.Type.BODY_DESCRIPTION.wrap(location))));
        }

        String iconSuffix;

        if (screen instanceof DragonEditorScreen dragonEditorScreen) {
            iconSuffix = dragonEditorScreen.dragonSpecies.getKey().location().getPath();
        } else if (screen instanceof DragonSpeciesScreen dragonSpeciesScreen) {
            iconSuffix = dragonSpeciesScreen.dragonSpecies.getKey().location().getPath();
        } else {
            iconSuffix = DEFAULT_SUFFIX;
        }

        ResourceLocation iconLocation = location.withPrefix(LOCATION_PREFIX).withSuffix("/" + iconSuffix + ".png");
        ResourceManager manager = ((TextureManagerAccess) Minecraft.getInstance().getTextureManager()).dragonSurvival$getResourceManager();

        if (manager.getResource(iconLocation).isEmpty()) {
            DragonSurvival.LOGGER.warn("Icon [{}] does not exist - using icon from body type [{}] as fallback", iconLocation, DragonBodies.center);
            iconLocation = DragonBodies.center.location().withPrefix(LOCATION_PREFIX).withSuffix("/" + iconSuffix + ".png");
        }

        this.iconLocation = iconLocation;
        this.screen = screen;
        this.dragonBody = dragonBody;
        this.bodyLocation = location;
        this.locked = locked;
        this.useBackground = useBackground;
        this.noTooltip = noTooltip;
    }

    public void disableHover() {
        this.disableHover = true;
    }

    public void enableHover() {
        this.disableHover = false;
    }

    public boolean isHovered() {
        return !disableHover && super.isHovered();
    }

    public boolean isFocused() {
        return !disableHover && super.isFocused();
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int state = 0;

        if (isSelected()) {
            state = SELECTED;
        } else if (locked) {
            state = LOCKED;
        } else if (isHoveredOrFocused()) {
            state = HOVERED;
        }

        if(!noTooltip) {
            if(state == LOCKED) {
                setTooltip(Tooltip.create(Component.translatable(UNAVAILABLE)));
            } else {
                setTooltip(Tooltip.create(Component.translatable(Translation.Type.BODY_DESCRIPTION.wrap(bodyLocation))));
            }
        }

        if(this.useBackground) {
            ResourceLocation background = state == SELECTED ? SELECTED_BACKGROUND : DESELECTED_BACKGROUND;
            graphics.blit(background, getX(), getY(), 0, 0, this.width, this.height, 35, 35);
            graphics.blit(iconLocation, getX() + 5, getY() + 5, 0, state * 25, 25, 25, 32, 104);
        } else {
            graphics.blit(iconLocation, getX(), getY(), 0, state * this.height, this.width, this.height, 32, 104);
        }
    }

    private boolean isSelected() {
        if (screen instanceof DragonEditorScreen dragonEditorScreen) {
            return DragonUtils.isBody(dragonBody, dragonEditorScreen.dragonBody);
        }

        if (screen instanceof DragonSkinsScreen skinsScreen) {
            return DragonUtils.isBody(dragonBody, skinsScreen.handler.body());
        }

        return false;
    }
}
