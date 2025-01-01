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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DragonBodyButton extends Button implements HoverDisableable {
    private static final String LOCATION_PREFIX = "textures/gui/custom/body/";
    private static final String DEFAULT_SUFFIX = "default";

    public static final int HOVERED = 1;
    public static final int SELECTED = 2;
    private static final int LOCKED = 3;

    private final Screen screen;
    private final Holder<DragonBody> dragonBody;
    private final ResourceLocation iconLocation;
    private final boolean locked;
    private boolean disableHover;

    public DragonBodyButton(Screen screen, int x, int y, int xSize, int ySize, final Holder<DragonBody> dragonBody, boolean locked, OnPress action) {
        this(screen, x, y, xSize, ySize, dragonBody, Objects.requireNonNull(dragonBody.getKey()).location(), locked, action);
    }

    @SuppressWarnings("DataFlowIssue") // key is expected to be present
    private DragonBodyButton(Screen screen, int x, int y, int xSize, int ySize, final Holder<DragonBody> dragonBody, final ResourceLocation location, boolean locked, OnPress action) {
        super(x, y, xSize, ySize, Component.translatable(Translation.Type.BODY.wrap(location)), action, DEFAULT_NARRATION);
        setTooltip(Tooltip.create(Component.translatable(Translation.Type.BODY_DESCRIPTION.wrap(location))));

        String iconSuffix;

        if (screen instanceof DragonEditorScreen dragonEditorScreen) {
            iconSuffix = dragonEditorScreen.dragonSpecies.getKey().location().getPath();
        } else if (screen instanceof DragonSpeciesScreen dragonSpeciesScreen) {
            iconSuffix = dragonSpeciesScreen.dragonSpecies.getKey().location().getPath();
        } else {
            iconSuffix = DEFAULT_SUFFIX;
        }

        ResourceLocation iconLocation = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), LOCATION_PREFIX + location.getPath() + "/" + iconSuffix + ".png");
        ResourceManager manager = ((TextureManagerAccess) Minecraft.getInstance().getTextureManager()).dragonSurvival$getResourceManager();

        if (manager.getResource(iconLocation).isEmpty()) {
            DragonSurvival.LOGGER.warn("Icon [{}] does not exist - using icon from body type [{}] as fallback", iconLocation, DragonBodies.center);
            iconLocation = ResourceLocation.fromNamespaceAndPath(DragonBodies.center.location().getNamespace(), LOCATION_PREFIX + DragonBodies.center.location().getPath() + iconSuffix);
        }

        this.iconLocation = iconLocation;
        this.screen = screen;
        this.dragonBody = dragonBody;
        this.locked = locked;
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
        if (screen instanceof DragonEditorScreen dragonEditorScreen) {
            active = visible = dragonEditorScreen.showUi;
        }

        int state = 0;

        if (isSelected()) {
            state = SELECTED;
        } else if (locked) {
            state = LOCKED;
        } else if (isHoveredOrFocused()) {
            state = HOVERED;
        }

        graphics.blit(iconLocation, getX(), getY(), 0, state * this.height, this.width, this.height, 32, 104);
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
