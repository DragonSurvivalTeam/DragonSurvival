package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.magic.AbilityAndPenaltyTooltipRenderer;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncAbilityEnabled;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSlotAssignment;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.UpgradeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class AbilityButton extends ExtendedButton {
    public static final ResourceLocation ACTIVE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "ability_screen/skill_main");
    public static final ResourceLocation PASSIVE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "ability_screen/skill_other");
    public static final ResourceLocation DISABLED_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "ability_screen/skill_disabled");
    public static final ResourceLocation AUTO_UPGRADE_ORNAMENTATION = ResourceLocation.fromNamespaceAndPath(MODID, "ability_screen/skill_autoupgrade");

    private static final int SIZE = 34;
    private static final int ORNAMENTATION_SIZE = 38;

    private final DragonAbilityScreen screen;
    private @Nullable DragonAbilityInstance ability;
    private @Nullable LevelButton leftLevelButton;
    private @Nullable LevelButton rightLevelButton;
    private Vec3 offset = Vec3.ZERO;

    private float scale;
    private int slot = MagicData.NO_SLOT;
    private int scrollAmount;
    private boolean isHotbar;
    private boolean isDragging;
    private boolean isInteractable = true;

    public AbilityButton(int x, int y, @Nullable final DragonAbilityInstance ability, final DragonAbilityScreen screen, float scale) {
        // Don't actually change the scale of the button itself based on the scale value; this is because we only rescale the button when it is
        // on the sides of the column, in which case it can't be interacted with anyway. Minecraft's GUI doesn't offer a clean way to adjust
        // the button's bounds dynamically, so this is the best we can do.
        super(x, y, 34, 34, Component.empty(), button -> { /* Nothing to do */ }, DEFAULT_NARRATION);
        this.screen = screen;
        this.ability = ability;
        this.isHotbar = false;
        this.scale = scale;

        if (ability == null || UpgradeType.IS_MANUAL.negate().test(ability.value().upgrade())) {
            return;
        }

        leftLevelButton = new LevelButton(LevelButton.Type.DOWNGRADE, ability, x - width / 2 + 7, y + 10);
        rightLevelButton = new LevelButton(LevelButton.Type.UPGRADE, ability, x + width / 2 + 18, y + 10);
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(leftLevelButton);
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(rightLevelButton);
    }

    public AbilityButton(int x, int y, @Nullable final DragonAbilityInstance ability, final DragonAbilityScreen screen, boolean isHotbar, int slot) {
        this(x, y, ability, screen);
        this.isHotbar = isHotbar;
        this.slot = slot;

        //noinspection DataFlowIssue -> player is present
        MagicData data = MagicData.getData(Minecraft.getInstance().player);
        this.ability = data.fromSlot(slot);
    }

    public AbilityButton(int x, int y, @Nullable final DragonAbilityInstance ability, final DragonAbilityScreen screen) {
        this(x, y, ability, screen, 1.0f);
    }

    public void setOffset(Vec3 offset) {
        this.offset = offset;
    }

    public Vec3 getOffset() {
        return offset;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isInteractable && super.isMouseOver(mouseX, mouseY);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;

        if (leftLevelButton != null && rightLevelButton != null) {
            leftLevelButton.visible = visible;
            rightLevelButton.visible = visible;
        }
    }

    public void setInteractable(boolean interactable) {
        isInteractable = interactable;

        if (isInteractable) {
            width = SIZE;
            height = SIZE;

            if (leftLevelButton != null && rightLevelButton != null) {
                leftLevelButton.resetDimensions();
                rightLevelButton.resetDimensions();
            }
        } else {
            width = 0;
            height = 0;

            if (leftLevelButton != null && rightLevelButton != null) {
                leftLevelButton.setWidth(0);
                leftLevelButton.setHeight(0);
                rightLevelButton.setWidth(0);
                rightLevelButton.setHeight(0);
            }
        }
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);

        if (ability == null || Screen.hasControlDown()) {
            return;
        }

        if (!ability.isPassive()) {
            isDragging = true;
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (ability == null) {
            super.onClick(mouseX, mouseY);
        }

        if (ability.value().canBeManuallyDisabled() && Screen.hasControlDown()) {
            boolean newStatus = !ability.isDisabled(true);
            ability.setDisabled(Minecraft.getInstance().player, newStatus, true);
            PacketDistributor.sendToServer(new SyncAbilityEnabled(ability.key(), newStatus, true));
            return;
        }

        super.onClick(mouseX, mouseY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);

        if (!isDragging) {
            return;
        }

        isDragging = false;

        if (ability == null) {
            return;
        }

        if (!ability.isPassive()) {
            //noinspection DataFlowIssue -> player is present
            MagicData data = MagicData.getData(Minecraft.getInstance().player);

            boolean wasSwappedToASlot = false;
            for (Renderable renderable : screen.renderables) {
                if (renderable instanceof AbilityButton button && button.slot != MagicData.NO_SLOT) {
                    if (button.isMouseOver(mouseX, mouseY)) {
                        PacketDistributor.sendToServer(new SyncSlotAssignment(ability.key(), button.slot));
                        data.moveAbilityToSlot(ability.key(), button.slot);
                        wasSwappedToASlot = true;
                        break;
                    }
                }
            }

            if (isHotbar && !wasSwappedToASlot) {
                PacketDistributor.sendToServer(new SyncSlotAssignment(ability.key(), MagicData.NO_SLOT));
                data.moveAbilityToSlot(ability.key(), MagicData.NO_SLOT);
            }
        }
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHotbar) {
            // Currently the easiest way to track assignments (and not end up with duplicate icons)
            // Alternative would be to go through all buttons and remove the ability that matches the swapped ability (in 'onRelease')
            //noinspection DataFlowIssue -> player is present
            ability = MagicData.getData(Minecraft.getInstance().player).fromSlot(slot);
        }

        graphics.pose().pushPose();
        // Scale about the center of the button
        graphics.pose().translate(getX(), getY(), 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.pose().translate(-getX(), -getY(), 0);
        float scaleXDiff = (scale - 1) * SIZE / 2;
        float scaleYDiff = (scale - 1) * SIZE / 2;
        graphics.pose().translate(offset.x - scaleXDiff, offset.y - scaleYDiff, offset.z);

        if (ability == null) {
            blit(graphics, PASSIVE_BACKGROUND, getX() - 2, getY() - 2, ORNAMENTATION_SIZE);
            return;
        }

        if (!ability.isEnabled()) {
            blit(graphics, DISABLED_BACKGROUND, getX() - 2, getY() - 2, ORNAMENTATION_SIZE);
        } else {
            if (ability.isPassive()) {
                blit(graphics, PASSIVE_BACKGROUND, getX() - 2, getY() - 2, ORNAMENTATION_SIZE);
            } else {
                blit(graphics, ACTIVE_BACKGROUND, getX() - 2, getY() - 2, ORNAMENTATION_SIZE);
            }
        }

        // TODO :: abilities without any upgrade also get this ornament at the moment
        if (UpgradeType.IS_MANUAL.negate().test(ability.value().upgrade())) {
            blit(graphics, AUTO_UPGRADE_ORNAMENTATION, getX() - 2, getY() - 2, ORNAMENTATION_SIZE);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 50);

        if (isDragging) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 100);
            blit(graphics, ability.getIcon(), mouseX - SIZE / 2, mouseY - SIZE / 2, SIZE);
            graphics.pose().popPose();
        }

        if (!isHotbar || !isDragging) {
            blit(graphics, ability.getIcon(), getX(), getY(), SIZE);
        }

        graphics.pose().popPose();

        if (isHovered() && shouldShowDescription()) {
            graphics.pose().pushPose();
            // Render above the other UI elements
            graphics.pose().translate(0, 0, 150);
            AbilityAndPenaltyTooltipRenderer.drawAbilityTooltip(graphics, mouseX, mouseY, ability, scrollAmount);
            graphics.pose().popPose();
        }

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (Screen.hasShiftDown() && isHovered()) {
            // invert the value so that scrolling down shows further entries
            scrollAmount = Math.clamp(scrollAmount + (int) -scrollY, 0, AbilityAndPenaltyTooltipRenderer.maxScroll());
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isHovered() {
        boolean isHovered = super.isHovered();

        if (!isHovered) {
            scrollAmount = 0;
        }

        return isHovered;
    }

    private void blit(final GuiGraphics graphics, final ResourceLocation texture, int x, int y, int size) {
        graphics.blit(x, y, 0, size, size, Minecraft.getInstance().getGuiSprites().getSprite(texture), 1, 1, 1, alpha);
    }

    /** If the player is dragging any button the buttons shouldn't show their description */
    private boolean shouldShowDescription() {
        if (isDragging) {
            return false;
        }

        if (ability != null && !ability.isPassive()) {
            for (Renderable renderable : screen.renderables) {
                if (renderable instanceof AbilityButton button && button.isDragging) {
                    return false;
                }
            }
        }

        return true;
    }
}