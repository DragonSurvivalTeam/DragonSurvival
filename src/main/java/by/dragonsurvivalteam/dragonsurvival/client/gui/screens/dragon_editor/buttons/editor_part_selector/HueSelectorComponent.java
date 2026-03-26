package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.editor_part_selector;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DragonPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonPart;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class HueSelectorComponent extends AbstractContainerEventHandler implements Renderable {
    private static final Identifier RESET_SETTINGS_HOVER = Identifier.fromNamespaceAndPath(MODID, "textures/gui/editor/color_reset_hover.png");
    private static final Identifier RESET_SETTINGS_MAIN = Identifier.fromNamespaceAndPath(MODID, "textures/gui/editor/color_reset_main.png");

    private static final Identifier SLIDER_HOVER = Identifier.fromNamespaceAndPath(MODID, "textures/gui/editor/color_slider_hover.png");
    private static final Identifier SLIDER_MAIN = Identifier.fromNamespaceAndPath(MODID, "textures/gui/editor/color_slider_main.png");

    private static final Identifier GLOW_ON = Identifier.fromNamespaceAndPath(MODID, "textures/gui/editor/glow_on.png");
    private static final Identifier GLOW_OFF = Identifier.fromNamespaceAndPath(MODID, "textures/gui/editor/glow_off.png");

    public static final int BACKGROUND_COLOR = -14935012;
    public static final int INNER_BORDER_COLOR = new Color(0x78787880, true).getRGB();

    public boolean visible;

    private final ExtendedButton hueReset;
    private final ExtendedButton saturationReset;
    private final ExtendedButton brightnessReset;
    private final ExtendedButton glowing;
    private final ExtendedSlider hueSlider;
    private final ExtendedSlider saturationSlider;
    private final ExtendedSlider brightnessSlider;
    public final Supplier<LayerSettings> settingsSupplier;

    private final int x;
    private final int y;
    private final int xSize;
    private final int ySize;

    private static final int INITIAL_BAR_OFFSET = 5;
    private static final int GAP_BETWEEN_BARS = 25;

    private boolean hasModifiedColor(DragonPart dragonPart) {
        return dragonPart != null && (Float.compare(Math.round(settingsSupplier.get().hue * 360), Math.round(dragonPart.averageHue() * 360)) != 0 || !(Math.abs(settingsSupplier.get().saturation - 0.5f) < 0.05) || !(Math.abs(settingsSupplier.get().brightness - 0.5f) < 0.05));
    }

    public HueSelectorComponent(DragonEditorScreen screen, int x, int y, int xSize, int ySize, SkinLayer layer) {
        this.x = x;
        this.y = y;
        this.xSize = xSize;
        this.ySize = ySize;

        settingsSupplier = () -> screen.preset.get(screen.stage.getKey()).get().layerSettings.get(layer).get();
        LayerSettings settings = settingsSupplier.get();
        DragonPart dragonPart = DragonPartLoader.getDragonPart(layer, DragonEditorScreen.HANDLER.speciesKey(), DragonEditorScreen.HANDLER.body(), settings.partKey);

        glowing = new ExtendedButton(x + 4, y - 25, 27, 25, Component.empty(), button -> {
            final Function<Boolean, Boolean> setGlowingAction = value -> {
                LayerSettings supplier = settingsSupplier.get();
                supplier.isGlowing = value;
                supplier.isModified = true;
                DragonEditorScreen.HANDLER.recompileCurrentSkin();
                screen.update();
                return !value;
            };

            screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(setGlowingAction, !settingsSupplier.get().isGlowing));
        }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
                Identifier texture = settingsSupplier.get().isGlowing ? GLOW_ON : GLOW_OFF;
                GuiGraphicsExtractor.pose().pushMatrix();
                // FIXME :: UI GRAPHICS
                //GuiGraphicsExtractor.pose().translate(0, 0, 100);
                GuiGraphicsExtractor.blit(texture, getX(), getY(), 0, 0, 27, 25, 27, 25);
                GuiGraphicsExtractor.pose().popMatrix();
            }
        };

        float[] hsb = new float[]{settings.hue, settings.saturation, settings.brightness};

        if (dragonPart == null) {
            hsb[0] = 0.5f;
            hsb[1] = 0.5f;
            hsb[2] = 0.5f;
        } else if (!settings.isModified) {
            hsb[0] = dragonPart.averageHue();
            hsb[1] = 0.5f;
            hsb[2] = 0.5f;
        }

        hueSlider = new ExtendedSlider(x + 3, y + 5, xSize - 26, 20, Component.empty(), Component.empty(), 0, 360, hsb[0] * 360.0f, true) {
            private int previousHue = 0;

            private final Function<Integer, Integer> setHueAction = value -> {
                settingsSupplier.get().hue = value / 360f;
                settingsSupplier.get().isModified = hasModifiedColor(dragonPart);
                DragonEditorScreen.HANDLER.recompileCurrentSkin();
                screen.update();

                return previousHue;
            };

            @Override
            protected void applyValue() {
                super.applyValue();

                setHueAction.apply(this.getValueInt());
            }

            @Override
            public void setValue(double value) {
                super.setValue(value);
                this.applyValue();
            }

            @Override
            public void renderWidget(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partial) {
                if (visible) {
                    this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
                    // FIXME :: UI GRAPHICS
                    // RenderingUtils.renderPureColorSquare(GuiGraphicsExtractor.pose(), getX() + 1, getY() + 1, getWidth() - 1, getHeight() - 1);
                    GuiGraphicsExtractor.renderOutline(getX() + 1, getY() + 1, getWidth() - 1, getHeight() - 1, Color.black.getRGB());
                    if (this.isHovered) {
                        GuiGraphicsExtractor.blit(SLIDER_HOVER, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY() - 3, 0, 0, 12, 26, 26, 26);
                    } else {
                        GuiGraphicsExtractor.blit(SLIDER_MAIN, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY() - 3, 0, 0, 12, 26, 26, 26);
                    }
                }
            }

            @Override
            public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
                super.onClick(event, isDoubleClick);
                previousHue = this.getValueInt();
            }

            @Override
            public void onRelease(@NotNull MouseButtonEvent event) {
                super.onRelease(event);
                screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(setHueAction, this.getValueInt()));
            }
        };

        hueReset = new HoverButton(x + 3 + xSize - 26, y + INITIAL_BAR_OFFSET - 1, 24, 24, 24, 24, RESET_SETTINGS_MAIN, RESET_SETTINGS_HOVER, button -> hueSlider.setValue(dragonPart != null ? Math.round(dragonPart.averageHue() * 360f) : 180));

        saturationSlider = new ExtendedSlider(x + 3, y + INITIAL_BAR_OFFSET + GAP_BETWEEN_BARS, xSize - 26, 20, Component.empty(), Component.empty(), 0, 360, hsb[1] * 360, true) {
            private int previousSaturation = 0;

            private final Function<Integer, Integer> setSaturationAction = value -> {
                settingsSupplier.get().saturation = value / 360f;
                settingsSupplier.get().isModified = hasModifiedColor(dragonPart);
                DragonEditorScreen.HANDLER.recompileCurrentSkin();
                screen.update();

                return previousSaturation;
            };

            @Override
            protected void applyValue() {
                super.applyValue();

                setSaturationAction.apply(this.getValueInt());
            }

            @Override
            public void setValue(double value) {
                super.setValue(value);
                this.applyValue();
            }

            @Override
            public void renderWidget(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partial) {
                if (visible) {
                    this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
                    float value1 = (hueSlider.getValueInt()) / 360f;

                    int col1 = Color.getHSBColor(value1, 0f, 1f).getRGB();
                    int col2 = Color.getHSBColor(value1, 1f, 1f).getRGB();

                    // FIXME :: UI GRAPHICS
                    //RenderingUtils.drawGradientRect(GuiGraphicsExtractor.pose().last().pose(), 0, getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, new int[]{col2, col1, col1, col2});
                    GuiGraphicsExtractor.renderOutline(getX() + 1, getY() + 1, getWidth() - 1, getHeight() - 1, Color.black.getRGB());
                    if (this.isHovered) {
                        GuiGraphicsExtractor.blit(SLIDER_HOVER, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY() - 3, 0, 0, 12, 26, 26, 26);
                    } else {
                        GuiGraphicsExtractor.blit(SLIDER_MAIN, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY() - 3, 0, 0, 12, 26, 26, 26);
                    }
                }
            }

            @Override
            public void onRelease(@NotNull MouseButtonEvent event) {
                super.onRelease(event);
                screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(setSaturationAction, this.getValueInt()));
            }

            @Override
            public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
                super.onClick(event, isDoubleClick);
                previousSaturation = this.getValueInt();
            }
        };

        saturationReset = new HoverButton(x + 3 + xSize - 26, y + INITIAL_BAR_OFFSET + GAP_BETWEEN_BARS - 1, 24, 24, 24, 24, RESET_SETTINGS_MAIN, RESET_SETTINGS_HOVER, button -> saturationSlider.setValue(180));

        brightnessSlider = new ExtendedSlider(x + 3, y + INITIAL_BAR_OFFSET + GAP_BETWEEN_BARS * 2, xSize - 26, 20, Component.empty(), Component.empty(), 0, 360, hsb[2] * 360, true) {
            private int previousBrightness = 0;

            private final Function<Integer, Integer> setBrightnessAction = value -> {
                settingsSupplier.get().brightness = value / 360f;
                settingsSupplier.get().isModified = hasModifiedColor(dragonPart);
                DragonEditorScreen.HANDLER.recompileCurrentSkin();
                screen.update();

                return previousBrightness;
            };

            @Override
            protected void applyValue() {
                super.applyValue();

                setBrightnessAction.apply(this.getValueInt());
            }

            @Override
            public void setValue(double value) {
                super.setValue(value);
                this.applyValue();
            }

            @Override
            public void renderWidget(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int mouseX, int mouseY, float partial) {
                if (visible) {
                    this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
                    float value1 = (hueSlider.getValueInt()) / 360f;

                    int col1 = Color.getHSBColor(value1, 1f, 0f).getRGB();
                    int col2 = Color.getHSBColor(value1, 1f, 1f).getRGB();

                    // FIXME :: UI GRAPHICS
                    // RenderingUtils.drawGradientRect(GuiGraphicsExtractor.pose().last().pose(), 0, getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, new int[]{col2, col1, col1, col2});
                    GuiGraphicsExtractor.renderOutline(getX() + 1, getY() + 1, getWidth() - 1, getHeight() - 1, Color.black.getRGB());
                    if (this.isHovered) {
                        GuiGraphicsExtractor.blit(SLIDER_HOVER, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY() - 3, 0, 0, 12, 26, 26, 26);
                    } else {
                        GuiGraphicsExtractor.blit(SLIDER_MAIN, this.getX() + (int) (this.value * (double) (this.width - 8)), this.getY() - 3, 0, 0, 12, 26, 26, 26);
                    }
                }
            }

            @Override
            public void onRelease(@NotNull MouseButtonEvent event) {
                super.onRelease(event);
                screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(setBrightnessAction, this.getValueInt()));
            }

            @Override
            public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
                super.onClick(event, isDoubleClick);
                previousBrightness = this.getValueInt();
            }
        };

        brightnessReset = new HoverButton(x + 3 + xSize - 26, y + INITIAL_BAR_OFFSET + GAP_BETWEEN_BARS * 2 - 1, 24, 24, 24, 24, RESET_SETTINGS_MAIN, RESET_SETTINGS_HOVER, button -> brightnessSlider.setValue(180));
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return visible && pMouseY >= (double) y - 30 && pMouseY <= (double) y + ySize && pMouseX >= (double) x - 5 && pMouseX <= (double) x + xSize;
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return ImmutableList.of(hueSlider, saturationSlider, brightnessSlider, hueReset, saturationReset, brightnessReset, glowing);
    }

    @Override
    public void render(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTicks) {
        GuiGraphicsExtractor.pose().pushMatrix();
        // Render pop-up menu content above the other elements
        // FIXME :: UI GRAPHICS
        //GuiGraphicsExtractor.pose().translate(0, 0, 150);

        // Background for glow button
        GuiGraphicsExtractor.fill(x + 2, y - 25, x + 32, y + 5, BACKGROUND_COLOR);
        GuiGraphicsExtractor.renderOutline(x + 2, y - 26, 30, 31, Color.black.getRGB());
        GuiGraphicsExtractor.renderOutline(x + 3, y - 25, 28, 29, INNER_BORDER_COLOR);

        // Main background
        GuiGraphicsExtractor.fill(x, y, x + xSize + 2, y + ySize - 10, BACKGROUND_COLOR);
        GuiGraphicsExtractor.renderOutline(x, y, xSize + 2, ySize - 10, Color.black.getRGB());
        GuiGraphicsExtractor.renderOutline(x + 1, y + 1, xSize, ySize - 12, INNER_BORDER_COLOR);

        glowing.render(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTicks);

        hueReset.render(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTicks);
        saturationReset.render(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTicks);
        brightnessReset.render(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTicks);

        hueSlider.render(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTicks);
        saturationSlider.render(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTicks);
        brightnessSlider.render(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTicks);
        GuiGraphicsExtractor.pose().popMatrix();
    }
}