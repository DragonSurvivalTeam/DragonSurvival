package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.CopySettingsButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ExtendedCheckbox;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

// TODO :: Currently this only supports the built-in types
public class CopyEditorSettingsComponent extends AbstractContainerEventHandler implements Renderable {
    @Translation(type = Translation.Type.MISC, comments = "Copy to...")
    private static final String COPY_TO = Translation.Type.GUI.wrap("copy_to");

    private static final ResourceLocation CONFIRM_BUTTON = DragonSurvival.res("textures/gui/confirm_button.png");
    private static final ResourceLocation CANCEL_BUTTON = DragonSurvival.res("textures/gui/cancel_button.png");

    public boolean visible;

    private final ExtendedButton confirm;
    private final ExtendedButton cancel;
    private final ExtendedCheckbox newborn;
    private final ExtendedCheckbox young;
    private final ExtendedCheckbox adult;

    private final int x;
    private final int y;
    private final int xSize;
    private final int ySize;

    public CopyEditorSettingsComponent(DragonEditorScreen screen, CopySettingsButton btn, int x, int y, int xSize, int ySize) {
        this.x = x;
        this.y = y;
        this.xSize = xSize;
        this.ySize = ySize;

        confirm = new ExtendedButton(x + xSize / 2 - 18, y + ySize - 15, 15, 15, Component.empty(), action -> { /* Nothing to do */ }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partial);
                guiGraphics.blit(CONFIRM_BUTTON, getX() + 1, getY(), 0, 0, 15, 15, 15, 15);
            }

            @Override
            public void onPress() {
                Lazy<DragonStageCustomization> lazy = screen.preset.get(Objects.requireNonNull(screen.dragonStage.getKey()));

                if (lazy == null) {
                    lazy = Lazy.of(DragonStageCustomization::new);
                }

                DragonStageCustomization preset = lazy.get();
                RegistryAccess access = Objects.requireNonNull(Minecraft.getInstance().player).registryAccess();

                if (newborn.active && newborn.selected()) {
                    Holder<DragonStage> newborn = access.holderOrThrow(DragonStages.newborn);

                    screen.preset.put(Objects.requireNonNull(newborn.getKey()), Lazy.of(() -> {
                        DragonStageCustomization customization = new DragonStageCustomization();
                        customization.deserializeNBT(access, preset.serializeNBT(access));
                        return customization;
                    }));
                }

                if (young.active && young.selected()) {
                    Holder<DragonStage> young = access.holderOrThrow(DragonStages.young);

                    screen.preset.put(Objects.requireNonNull(young.getKey()), Lazy.of(() -> {
                        DragonStageCustomization customization = new DragonStageCustomization();
                        customization.deserializeNBT(access, preset.serializeNBT(access));
                        return customization;
                    }));
                }

                if (adult.active && adult.selected()) {
                    Holder<DragonStage> adult = access.holderOrThrow(DragonStages.adult);

                    screen.preset.put(Objects.requireNonNull(adult.getKey()), Lazy.of(() -> {
                        DragonStageCustomization customization = new DragonStageCustomization();
                        customization.deserializeNBT(access, preset.serializeNBT(access));
                        return customization;
                    }));
                }

                screen.update();
                btn.onPress();

                // Undoing this action is not supported at the moment
                screen.actionHistory.clear();
            }
        };

        confirm.setTooltip(Tooltip.create(Component.translatable(LangKey.GUI_CONFIRM)));

        cancel = new ExtendedButton(x + xSize / 2 + 3, y + ySize - 15, 15, 15, Component.empty(), action -> { /* Nothing to do */ }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
                setMessage(Component.empty());
                super.renderWidget(guiGraphics, mouseX, mouseY, partial);
                guiGraphics.blit(CANCEL_BUTTON, getX(), getY(), 0, 0, 15, 15, 15, 15);
            }

            @Override
            public void onPress() {
                btn.onPress();
            }
        };

        cancel.setTooltip(Tooltip.create(Component.translatable(LangKey.GUI_CANCEL)));

        newborn = new ExtendedCheckbox(x + 5, y + 12, xSize - 10, 10, 10, Component.literal("newborn"), false, action -> { /* Nothing to do */ }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
                super.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTicks);
                if (screen.dragonStage.getKey() == DragonStages.newborn) {
                    selected = true;
                    active = false;
                } else {
                    active = true;
                }
            }
        };

        young = new ExtendedCheckbox(x + 5, y + 27, xSize - 10, 10, 10, Component.literal("young"), false, action -> { /* Nothing to do */ }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
                super.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTicks);
                if (screen.dragonStage.getKey() == DragonStages.young) {
                    selected = true;
                    active = false;
                } else {
                    active = true;
                }
            }
        };

        adult = new ExtendedCheckbox(x + 5, y + 27 + 15, xSize - 10, 10, 10, Component.literal("adult"), false, action -> { /* Nothing to do */ }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
                super.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTicks);
                if (screen.dragonStage.getKey() == DragonStages.adult) {
                    selected = true;
                    active = false;
                } else {
                    active = true;
                }
            }
        };
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return visible && pMouseY >= (double) y - 3 && pMouseY <= (double) y + ySize + 3 && pMouseX >= (double) x && pMouseX <= (double) x + xSize;
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return ImmutableList.of(confirm, cancel, newborn, young, adult);
    }

    @Override
    public void render(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        // Render pop-up contents above the other elements
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);
        guiGraphics.blitWithBorder(DropdownList.BACKGROUND_TEXTURE, x, y - 3, 0, 0, xSize, ySize + 6, 32, 32, 10, 10, 10, 10);
        confirm.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        cancel.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        newborn.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        young.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        adult.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        guiGraphics.pose().popPose();
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable(COPY_TO), x + xSize / 2, y + 1, 14737632);
    }
}