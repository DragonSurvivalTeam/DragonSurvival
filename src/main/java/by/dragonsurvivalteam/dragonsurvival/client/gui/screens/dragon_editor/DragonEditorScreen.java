package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonBodyScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.BackgroundColorButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonEditorSlotButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.editor_part_selector.EditorPartComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.editor_part_selector.HueSelectorComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverDisableable;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.DragonEditorConfirmComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.DragonUIRenderComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.ScrollableComponent;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.CustomizationFileHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonPart;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncAltarCooldown;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@EventBusSubscriber(Dist.CLIENT)
public class DragonEditorScreen extends Screen implements DragonBodyScreen {
    @Translation(comments = "Randomize")
    private static final String RANDOMIZE = Translation.Type.GUI.wrap("dragon_editor.randomize");

    @Translation(comments = "Undo changes")
    private static final String UNDO = Translation.Type.GUI.wrap("dragon_editor.undo");

    @Translation(comments = "Redo changes")
    private static final String REDO = Translation.Type.GUI.wrap("dragon_editor.redo");

    @Translation(comments = {
            "■ You can select any §6slot§r here and click the §6load/save button§r to save your current settings to that slot or load the settings from that slot.",
            "■ Your exports are stored here: %s"
    })
    private static final String SAVING_INFO = Translation.Type.GUI.wrap("dragon_editor.save_slot_info");

    @Translation(comments = "Save to current slot")
    private static final String SAVE = Translation.Type.GUI.wrap("dragon_editor.save");

    @Translation(comments = "Load from current slot")
    private static final String LOAD = Translation.Type.GUI.wrap("dragon_editor.load");

    @Translation(comments = "■ Click here to §6copy§r your current settings to the other growth stages.")
    private static final String COPY = Translation.Type.GUI.wrap("dragon_editor.copy");

    @Translation(comments = "Show/Hide UI")
    private static final String SHOW_UI = Translation.Type.GUI.wrap("dragon_editor.show_ui");

    @Translation(comments = "Reset to default")
    private static final String RESET = Translation.Type.GUI.wrap("dragon_editor.reset");

    @Translation(comments = {
            "■ The texture from this editor is only visible if your §6custom§r skins are turned off in Skin Tab (dragon inventory). You can learn how to create your own custom skins on the §6Github Wiki§r or Dragon Survival discord.",
            "§r-§7 Dragon Survival works with shaders, but they can affect the appearance of glowing textures.§r"
    })
    private static final String CUSTOMIZATION = Translation.Type.GUI.wrap("dragon_editor.customization");

    @Translation(comments = "Save data invalid for this dragon species")
    private static final String INVALID_FOR_TYPE = Translation.Type.GUI.wrap("dragon_editor.invalid_for_type");

    @Translation(comments = "Save data invalid for this model")
    private static final String INVALID_FOR_MODEL = Translation.Type.GUI.wrap("dragon_editor.invalid_for_model");

    @Translation(comments = "No save data for this slot")
    private static final String NO_DATA = Translation.Type.GUI.wrap("dragon_editor.no_data");

    @Translation(comments = "Slot saved")
    private static final String SLOT_SAVED = Translation.Type.GUI.wrap("dragon_editor.slot_saved");

    public static final DragonStateHandler HANDLER = new DragonStateHandler();

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png");

    private static final ResourceLocation COPY_ALL_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/copy_off.png");
    private static final ResourceLocation COPY_ALL_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/copy_on.png");

    private static final ResourceLocation INFO_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/info_main.png");
    private static final ResourceLocation INFO_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/info_hover.png");

    private static final ResourceLocation ALTERNATIVE_ON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/alternative_on.png");
    private static final ResourceLocation ALTERNATIVE_OFF = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/alternative_off.png");

    private static final ResourceLocation CONFIRM_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/accept_background_hover.png");
    private static final ResourceLocation CONFIRM_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/accept_background_main.png");

    private static final ResourceLocation CANCEL_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/cancel_background_hover.png");
    private static final ResourceLocation CANCEL_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/cancel_background_main.png");

    private static final ResourceLocation SHOW_UI_ON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/gui_on.png");
    private static final ResourceLocation SHOW_UI_OFF = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/gui_off.png");

    private static final ResourceLocation RESET_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/reset_main.png");
    private static final ResourceLocation RESET_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/reset_hover.png");

    private static final ResourceLocation ANIMATION_NAME_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/animation_name_background.png");

    private static final ResourceLocation SMALL_LEFT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_left_arrow_hover.png");
    private static final ResourceLocation SMALL_LEFT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_left_arrow_main.png");
    private static final ResourceLocation SMALL_RIGHT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_right_arrow_hover.png");
    private static final ResourceLocation SMALL_RIGHT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_right_arrow_main.png");

    private static final ResourceLocation LEFT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/left_arrow_hover.png");
    private static final ResourceLocation LEFT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/left_arrow_main.png");
    private static final ResourceLocation RIGHT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/right_arrow_hover.png");
    private static final ResourceLocation RIGHT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/right_arrow_main.png");

    private static final ResourceLocation REDO_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/redo_main.png");
    private static final ResourceLocation REDO_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/redo_hover.png");
    private static final ResourceLocation UNDO_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/undo_main.png");
    private static final ResourceLocation UNDO_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/undo_hover.png");

    private static final ResourceLocation RANDOM_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/random_hover.png");
    private static final ResourceLocation RANDOM_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/random_main.png");

    private static final ResourceLocation SAVE_SLOT_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/slot_background.png");

    private static final ResourceLocation SLOT_LOAD_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/slot_load_hover.png");
    private static final ResourceLocation SLOT_LOAD_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/slot_load_main.png");
    private static final ResourceLocation SLOT_SAVE_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/slot_save_hover.png");
    private static final ResourceLocation SLOT_SAVE_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/slot_save_main.png");
    private static final ResourceLocation SLOT_INFO_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/slot_info_hover.png");
    private static final ResourceLocation SLOT_INFO_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/slot_info_main.png");

    public int guiTop;
    public boolean confirmation;
    public boolean showUi = true;

    /** Dragon type of {@link DragonEditorScreen#HANDLER} */
    public Holder<DragonSpecies> dragonSpecies;

    /** Dragon body of {@link DragonEditorScreen#HANDLER} */
    public Holder<DragonBody> dragonBody;

    /** Dragon level of {@link DragonEditorScreen#HANDLER} */
    public Holder<DragonStage> dragonStage;

    public SkinPreset preset;
    public int selectedSaveSlot = CustomizationFileHandler.STARTING_SLOT;

    public int backgroundColor = -804253680;

    private final Screen source;
    private int guiLeft;

    private final String[] animations = {"sit_dentist", "sit_animation", "idle_animation", "fly_animation", "swim_animation", "run_animation", "spinning_on_back"};

    private DragonUIRenderComponent dragonRender;
    private ExtendedButton uiButton;
    private DragonEditorConfirmComponent confirmComponent;
    private ExtendedButton wingsButton;
    private HoverButton animationNameButton;
    private BackgroundColorButton backgroundColorButton;

    /**
     * Widgets which belong to the dragon body logic <br>
     * (they are stored to properly reference (and remove) them when using the arrow buttons to navigate through the bodies)
     */
    private final List<AbstractWidget> dragonBodyWidgets = new ArrayList<>();
    private int dragonBodySelectionOffset;

    private final List<ScrollableComponent> scrollableComponents = new ArrayList<>();
    private final Map<EnumSkinLayer, EditorPartComponent> partComponents = new HashMap<>();

    private float tick;
    private int curAnimation;
    private int lastSelected;
    private int selectedDragonStage;
    private boolean hasInit;
    private boolean isEditor;

    private enum SlotDisplayMessage {
        INVALID_FOR_TYPE,
        INVALID_FOR_MODEL,
        NO_DATA,
        SLOT_SAVED,
        NONE
    }

    private Component loadSlotDisplayMessage(SlotDisplayMessage reason) {
        return switch (reason) {
            case INVALID_FOR_TYPE -> Component.translatable(INVALID_FOR_TYPE);
            case INVALID_FOR_MODEL -> Component.translatable(INVALID_FOR_MODEL);
            case NO_DATA -> Component.translatable(NO_DATA);
            case SLOT_SAVED -> Component.translatable(SLOT_SAVED);
            default -> Component.empty();
        };
    }

    private float tickWhenSlotDisplayMessageSet = -1;
    private SlotDisplayMessage slotDisplayMessage = SlotDisplayMessage.NONE;

    public DragonEditorScreen(Screen source) {
        this(source, null);
        this.isEditor = true;
    }

    public DragonEditorScreen(Screen source, Holder<DragonSpecies> dragonSpecies) {
        super(Component.translatable(LangKey.GUI_DRAGON_EDITOR));
        this.source = source;
        this.dragonSpecies = dragonSpecies;
    }

    public record EditorAction<T>(Function<T, T> action, T value) {
        public T run() {
            return action.apply(value);
        }

        @Override
        public boolean equals(Object object) {
            //noinspection DeconstructionCanBeUsed -> not valid
            if (object instanceof EditorAction<?> editorAction) {
                if (editorAction.action != null && editorAction.value != null) {
                    return editorAction.action.equals(action) && editorAction.value.equals(this.value);
                }
            }

            return false;
        }
    }

    public static float setZoom(final Holder<DragonStage> dragonStage) {
        return (float) (0.4 * dragonStage.value().sizeRange().min() + 20);
    }

    public final Function<Holder<DragonStage>, Holder<DragonStage>> selectStageAction = newStage -> {
        Holder<DragonStage> previousLevel = dragonStage;
        dragonStage = newStage;
        dragonRender.zoom = setZoom(dragonStage);
        HANDLER.setStage(null, dragonStage);
        HANDLER.recompileCurrentSkin();
        update();

        return previousLevel;
    };

    // setHueAction, setSaturationAction, setBrightnessAction in HueSelectorComponent.Java
    // setDragonSlotAction in DragonEditorSlotButton.Java

    // FIXME :: Known issue: if I switch to a body type that invalidates my preset, my preset data will be lost even if I undo
    public final Function<CompoundTag, CompoundTag> setSkinPresetAction = tag -> {
        CompoundTag prevTag = HANDLER.getCurrentSkinPreset().serializeNBT(Objects.requireNonNull(Minecraft.getInstance().player).registryAccess());
        HANDLER.getCurrentSkinPreset().deserializeNBT(Minecraft.getInstance().player.registryAccess(), tag);
        HashMap<EnumSkinLayer, Lazy<LayerSettings>> layerSettingsMap = HANDLER.getCurrentStageCustomization().layerSettings;
        for(EnumSkinLayer layer : layerSettingsMap.keySet()) {
            partComponents.get(layer).setSelectedPart(layerSettingsMap.get(layer).get().partKey);
        }
        HANDLER.recompileCurrentSkin();
        update();
        return prevTag;
    };

    public final Function<Holder<DragonBody>, Holder<DragonBody>> dragonBodySelectAction = dragonBody -> {
        Holder<DragonBody> previousBody = this.dragonBody;
        this.dragonBody = dragonBody;
        update();
        return previousBody;
    };

    public List<String> getPartsFromLayer(EnumSkinLayer layer) {
        return DragonEditorHandler.getDragonPartKeys(dragonSpecies, dragonBody, layer);
    }

    public final Function<Pair<EnumSkinLayer, String>, Pair<EnumSkinLayer, String>> dragonPartSelectAction = pair -> {
        Pair<EnumSkinLayer, String> previousPair = new Pair<>(pair.getFirst(), preset.get(Objects.requireNonNull(dragonStage.getKey())).get().layerSettings.get(pair.getFirst()).get().partKey);

        EnumSkinLayer layer = pair.getFirst();
        String value = pair.getSecond();
        partComponents.get(layer).setSelectedPart(value);
        preset.get(dragonStage.getKey()).get().layerSettings.get(layer).get().partKey = value;

        // Make sure that when we change a part, the color is properly updated to the default color of the new part
        LayerSettings settings = preset.get(dragonStage.getKey()).get().layerSettings.get(layer).get();

        DragonPart part = DragonEditorHandler.getDragonPart(layer, settings.partKey, dragonSpecies.getKey());
        if (part != null && !settings.modifiedColor) {
            settings.hue = part.averageHue();
        }

        HANDLER.recompileCurrentSkin();
        update();

        return previousPair;
    };

    public final Function<Boolean, Boolean> checkWingsButtonAction = (selected) -> {
        boolean prevSelected = preset.get(Objects.requireNonNull(dragonStage.getKey())).get().wings;
        preset.get(Objects.requireNonNull(dragonStage.getKey())).get().wings = selected;
        HANDLER.recompileCurrentSkin();
        update();
        return prevSelected;
    };

    public final Function<DragonStageCustomization, DragonStageCustomization> loadStageCustomizationAction = newStageCustomization -> {
        DragonStageCustomization previousStageCustomization = HANDLER.getCurrentStageCustomization();
        HANDLER.setCurrentStageCustomization(newStageCustomization);
        HANDLER.recompileCurrentSkin();
        update();
        return previousStageCustomization;
    };

    public static class UndoRedoList {
        private record UndoRedoPair(EditorAction<?> undo, EditorAction<?> redo) { /* Nothing to do */ }

        private final List<UndoRedoPair> delegate = new ArrayList<>();
        private final int maxSize;
        private int selectedIndex = 0;

        public UndoRedoList(int maxSize) {
            this.maxSize = maxSize;
        }

        public <T> void add(EditorAction<T> action) {
            // Run the action here instead of elsewhere, so that we make sure whatever is being undone is actually done
            T previousState = action.run();

            if (selectedIndex > 0 && action.equals(delegate.get(selectedIndex - 1).redo)) {
                return;
            }

            delegate.subList(selectedIndex, delegate.size()).clear();

            EditorAction<T> undoAction = new EditorAction<>(action.action, previousState);

            delegate.add(new UndoRedoPair(undoAction, action));

            if (delegate.size() > maxSize) {
                delegate.removeFirst();
            } else {
                selectedIndex++;
            }
        }

        public void undo() {
            if (selectedIndex > 0) {
                selectedIndex--;
                delegate.get(selectedIndex).undo.run();
            }
        }

        public void redo() {
            if (selectedIndex < delegate.size()) {
                delegate.get(selectedIndex).redo.run();
                selectedIndex++;
            }
        }

        public void clear() {
            delegate.clear();
            selectedIndex = 0;
        }
    }

    public final UndoRedoList actionHistory = new UndoRedoList(200);

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (ScrollableComponent component : scrollableComponents) {
            component.scroll(mouseX, mouseY, scrollX, scrollY);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean onlyCheckForColorSelectorComponentButtons() {
        boolean onlyCheckForColorSelectorComponentButtons = false;
        for(EditorPartComponent partComponent : partComponents.values()) {
            if(partComponent.colorSelectorIsToggled()) {
                onlyCheckForColorSelectorComponentButtons = true;
                break;
            }
        }

        return onlyCheckForColorSelectorComponentButtons;
    }

    // Ignore clicks on elements that are not in a popup menu, if one is open
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(onlyCheckForColorSelectorComponentButtons()) {
            for(EditorPartComponent partComponent : partComponents.values()) {
                HueSelectorComponent hueComponent = partComponent.getColorSelectorButton().getHueComponent();
                if(hueComponent != null && partComponent.getColorSelectorButton().toggled) {
                    List<GuiEventListener> hueComponentChildrenAndColorButton = new ArrayList<>(hueComponent.children());
                    hueComponentChildrenAndColorButton.add(partComponent.getColorSelectorButton());
                    for(GuiEventListener guieventlistener : hueComponentChildrenAndColorButton) {
                        if (guieventlistener.mouseClicked(mouseX, mouseY, button)) {
                            this.setFocused(guieventlistener);
                            if (button == 0) {
                                this.setDragging(true);
                            }

                            return true;
                        }
                    }
                }
            }

            return false;
        }

        if(confirmComponent != null && confirmation) {
            for (GuiEventListener guieventlistener : confirmComponent.children()) {
                if (guieventlistener.mouseClicked(mouseX, mouseY, button)) {
                    this.setFocused(guieventlistener);
                    if (button == 0) {
                        this.setDragging(true);
                    }

                    return true;
                }
            }

            return false;
        }

        if(backgroundColorButton != null && backgroundColorButton.toggled) {
            for (GuiEventListener guieventlistener : backgroundColorButton.childrenAndSelf()) {
                if (guieventlistener.mouseClicked(mouseX, mouseY, button)) {
                    this.setFocused(guieventlistener);
                    if (button == 0) {
                        this.setDragging(true);
                    }

                    return true;
                }
            }

            return false;
        }

        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(guieventlistener);
                if (button == 0) {
                    this.setDragging(true);
                }

                // Do this after we have processed the click event, so that we can properly disable the other buttons immediately
                if(onlyCheckForColorSelectorComponentButtons()) {
                    for (GuiEventListener guieventlistener2 : this.children()) {
                        // Don't disable the button that opened the panel in the first place
                        if(guieventlistener2 instanceof HoverDisableable hoverDisableable && guieventlistener != guieventlistener2) {
                            hoverDisableable.disableHover();
                        }
                    }
                }

                if(confirmComponent != null && confirmation) {
                    for (GuiEventListener guieventlistener2 : this.children()) {
                        if(guieventlistener2 instanceof HoverDisableable hoverDisableable) {
                            hoverDisableable.disableHover();
                        }
                    }
                }

                if(backgroundColorButton != null && backgroundColorButton.toggled) {
                    for (GuiEventListener guieventlistener2 : this.children()) {
                        if(guieventlistener2 instanceof HoverDisableable hoverDisableable && !backgroundColorButton.childrenAndSelf().contains(guieventlistener2)) {
                            hoverDisableable.disableHover();
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (dragonRender == null) { // In the past this could occur when using the dragon editor command before using the dragon altar first
            init();
        }

        tick += partialTick;
        if (tick >= 60 * 20) {
            tick = 0;
        }

        if (showUi) {
            dragonRender.x = width / 2 - 70;
            dragonRender.y = guiTop;
            dragonRender.width = 140;
            dragonRender.height = 125;
        } else {
            dragonRender.x = 0;
            dragonRender.width = width;
        }

        FakeClientPlayerUtils.getFakePlayer(0, HANDLER).animationSupplier = () -> animations[curAnimation];
        renderBackground(graphics, mouseX, mouseY, partialTick);
        children().stream().filter(DragonUIRenderComponent.class::isInstance).toList().forEach(s -> ((DragonUIRenderComponent) s).render(graphics, mouseX, mouseY, partialTick));
        DragonAltarScreen.renderBorders(graphics, BACKGROUND_TEXTURE, 0, width, 32, height - 32, width, height);
        TextRenderUtil.drawCenteredScaledText(graphics, width / 2, 10, 2f, DragonStage.translatableName(Objects.requireNonNull(dragonStage.getKey())).getString().toUpperCase(), DyeColor.WHITE.getTextColor());

        if(slotDisplayMessage != SlotDisplayMessage.NONE) {
            int color;
            if(slotDisplayMessage == SlotDisplayMessage.SLOT_SAVED) {
                color = DyeColor.GREEN.getTextColor();
            } else {
                color = DyeColor.RED.getTextColor();
            }
            if(tickWhenSlotDisplayMessageSet + 200 - tick > 0) {
                TextRenderUtil.drawCenteredScaledText(graphics, width / 2, height / 2 + 20, 0.5f, loadSlotDisplayMessage(slotDisplayMessage).getString(), color);
            } else {
                slotDisplayMessage = SlotDisplayMessage.NONE;
            }
        }

        for (Renderable renderable : new CopyOnWriteArrayList<>(renderables)) {
            if(!onlyCheckForColorSelectorComponentButtons() && !confirmation && (backgroundColorButton == null || !backgroundColorButton.toggled)) {
                if(renderable instanceof HoverDisableable hoverDisableable) {
                    hoverDisableable.enableHover();
                }
            }

            if (renderable instanceof AbstractWidget widget && widget != uiButton) {
                if(widget == wingsButton) {
                    if(dragonBody != null && dragonBody.value().canHideWings() && widget == wingsButton) {
                        wingsButton.visible = showUi;
                        wingsButton.setTooltip(Tooltip.create(Component.translatable(DragonBody.getWingButtonDescription(dragonBody))));
                        wingsButton.setMessage(Component.translatable(DragonBody.getWingButtonName(dragonBody)));
                    } else {
                        wingsButton.visible = false;
                    }
                } else {
                    widget.visible = showUi;
                }
            }

            renderable.render(graphics, mouseX, mouseY, partialTick);
        }

        uiButton.visible = true;
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.fill(0, 0, width, height, -350, backgroundColor);
    }

    private void initDummyDragon(final DragonStateHandler localHandler) {
        if (dragonSpecies == null && localHandler.isDragon()) {
            dragonSpecies = localHandler.species();
            dragonStage = localHandler.stage();
            dragonBody = localHandler.body();
        } else if (dragonSpecies != null) {
            if (dragonStage == null) {
                dragonStage = ResourceHelper.get(null, DragonStages.newborn).orElseThrow();
            }

            if (dragonBody == null) {
                dragonBody = DragonBody.random(null);
            }
        } else {
            return;
        }

        HANDLER.setType(null, dragonSpecies);
        HANDLER.setDesiredSize(null, dragonStage.value().sizeRange().min());
        HANDLER.setBody(null, dragonBody);

        preset = new SkinPreset();
        preset.initDefaults(dragonSpecies.getKey(), dragonBody.value().customModel());
        HANDLER.setCurrentSkinPreset(preset);

        dragonRender.zoom = setZoom(dragonStage);
    }

    private boolean dragonSpeciesWouldChange(DragonStateHandler handler) {
        return handler.species() != null && !handler.species().equals(dragonSpecies);
    }

    private boolean dragonBodyWouldChange(DragonStateHandler handler) {
        return handler.body() != null && !handler.body().equals(dragonBody);
    }

    public boolean dragonWouldChange(DragonStateHandler handler) {
        return (handler.species() != null && !handler.species().equals(dragonSpecies)) || (handler.body() != null && !handler.body().equals(dragonBody));
    }

    @Override
    public void init() {
        super.init();
        assert minecraft != null && minecraft.player != null;

        guiLeft = (width - 256) / 2;
        guiTop = (height - 120) / 2;
        slotDisplayMessage = SlotDisplayMessage.NONE;

        confirmComponent = new DragonEditorConfirmComponent(this, width / 2 - 130 / 2, height / 2 - 181 / 2, 130, 154);
        confirmation = false;
        initDragonRender();

        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);

        if (!hasInit) {
            initDummyDragon(data);
            update();
            hasInit = true;
        }

        selectedDragonStage = DragonStage.allStages(minecraft.player.registryAccess()).indexOf(dragonStage);
        HoverButton leftArrow = new HoverButton(width / 2 - 100, 10, 18, 20, 20, 20, LEFT_ARROW_MAIN, LEFT_ARROW_HOVER, button -> {
            List<Holder<DragonStage>> allStages = DragonStage.allStages(minecraft.player.registryAccess());
            selectedDragonStage = Functions.wrap(selectedDragonStage - 1, 0, allStages.size() - 1);
            actionHistory.add(new EditorAction<>(selectStageAction, allStages.get(selectedDragonStage)));
        });
        addRenderableWidget(leftArrow);

        HoverButton rightArrow = new HoverButton(width / 2 + 83, 10, 18, 20, 20, 20, RIGHT_ARROW_MAIN, RIGHT_ARROW_HOVER, button -> {
            List<Holder<DragonStage>> allStages = DragonStage.allStages(minecraft.player.registryAccess());
            selectedDragonStage = Functions.wrap(selectedDragonStage + 1, 0, allStages.size() - 1);
            actionHistory.add(new EditorAction<>(selectStageAction, allStages.get(selectedDragonStage)));
        });
        addRenderableWidget(rightArrow);

        addDragonBodyWidgets();

        int maxWidth = -1;

        for (EnumSkinLayer layer : EnumSkinLayer.values()) {
            String name = layer.getNameUpperCase().charAt(0) + layer.getNameLowerCase().substring(1).replace("_", " ");
            maxWidth = (int) Math.max(maxWidth, font.width(name) * 1.45F);
        }

        int row = 0;
        for (EnumSkinLayer layer : EnumSkinLayer.values()) {
            ArrayList<String> valueList = DragonEditorHandler.getDragonPartKeys(dragonSpecies, dragonBody, layer);

            if (layer != EnumSkinLayer.BASE) {
                valueList.addFirst(DefaultPartLoader.NO_PART);
            }

            //noinspection DataFlowIssue -> key is present
            String partKey = preset.get(dragonStage.getKey()).get().layerSettings.get(layer).get().partKey;
            EditorPartComponent editorPartComponent = new EditorPartComponent(this, row < 8 ? width / 2 - 184 : width / 2 + 74, guiTop - 24 + (row >= 8 ? (row - 8) * 21 : row * 21), partKey, layer, row < 8, (row / 4 % 2) == 0);
            scrollableComponents.add(editorPartComponent);
            partComponents.put(layer, editorPartComponent);
            row++;
        }

        animationNameButton = new HoverButton(width / 2 - 50, height / 2 + 60, 100, 20, 100, 20, ANIMATION_NAME_BACKGROUND, ANIMATION_NAME_BACKGROUND, btn -> { /* Nothing to do */ });
        animationNameButton.setMessage(Component.empty().append(WordUtils.capitalize(animations[curAnimation].replace("_", " "))));
        addRenderableWidget(animationNameButton);

        HoverButton leftAnimationArrow = new HoverButton(width / 2 - 57, height / 2 + 62, 9, 16, 20, 20, SMALL_LEFT_ARROW_MAIN, SMALL_LEFT_ARROW_HOVER, button -> {
            curAnimation -= 1;

            if (curAnimation < 0) {
                curAnimation = animations.length - 1;
            }
            animationNameButton.setMessage(Component.empty().append(WordUtils.capitalize(animations[curAnimation].replace("_", " "))));
        });
        addRenderableWidget(leftAnimationArrow);

        HoverButton rightAnimationArrow = new HoverButton(width / 2 + 48, height / 2 + 62, 9, 16, 20, 20, SMALL_RIGHT_ARROW_MAIN, SMALL_RIGHT_ARROW_HOVER, button -> {
            curAnimation += 1;

            if (curAnimation >= animations.length) {
                curAnimation = 0;
            }
            animationNameButton.setMessage(Component.empty().append(WordUtils.capitalize(animations[curAnimation].replace("_", " "))));
        });
        addRenderableWidget(rightAnimationArrow);

        ExtendedButton saveButton = new HoverButton(width / 2 - 60, height - 30, 60, 20, 60, 20, CONFIRM_MAIN, CONFIRM_HOVER, button -> { /* Nothing to do */ }) {
            Renderable renderButton;
            boolean toggled;

            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partial);
                if (toggled && (!visible || !confirmation)) {
                    toggled = false;
                    Screen screen = Minecraft.getInstance().screen;
                    Objects.requireNonNull(screen).children().removeIf(s -> s == confirmComponent);
                    screen.renderables.removeIf(s -> s == renderButton);
                }
            }

            @Override
            public void onPress() {
                DragonStateHandler handler = DragonStateProvider.getData(minecraft.player);
                minecraft.player.level().playSound(minecraft.player, minecraft.player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);
                boolean dragonDataIsPreserved = ServerConfig.saveAllAbilities && ServerConfig.saveGrowthStage;

                if (handler.isDragon() && dragonWouldChange(handler) && !dragonDataIsPreserved) {
                    confirmation = true;
                    confirmComponent.isBodyTypeChange = dragonBodyWouldChange(handler) && !dragonSpeciesWouldChange(handler);
                } else {
                    confirm();
                }

                if (confirmation) {
                    if (!toggled) {
                        renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), b -> {
                        }) {
                            @Override
                            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                                if (confirmComponent != null && confirmation) {
                                    confirmComponent.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
                                }

                                super.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTick);
                            }
                        };
                        ((ScreenAccessor) DragonEditorScreen.this).dragonSurvival$children().add(confirmComponent);
                        renderables.add(renderButton);
                    }
                    toggled = !toggled;
                } else {
                    children().removeIf(s -> s == confirmComponent);
                    renderables.removeIf(s -> s == renderButton);
                }
            }
        };
        saveButton.setMessage(Component.translatable(LangKey.GUI_CONFIRM));
        addRenderableWidget(saveButton);

        ExtendedButton discardButton = new HoverButton(width / 2 + 1, height - 30, 60, 20, 60, 20, CANCEL_MAIN, CANCEL_HOVER, btn -> Minecraft.getInstance().setScreen(source));
        discardButton.setMessage(Component.translatable(LangKey.GUI_CANCEL));
        addRenderableWidget(discardButton);

        RegistryAccess access = Objects.requireNonNull(Minecraft.getInstance().player).registryAccess();

        HoverButton randomButton = new HoverButton(width / 2 - 8, 40, 16, 17, 20, 20, RANDOM_MAIN, RANDOM_HOVER, btn -> {
            ArrayList<String> extraKeys = DragonEditorHandler.getDragonPartKeys(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), EnumSkinLayer.EXTRA);

            extraKeys.removeIf(partKey -> {
                DragonPart text = DragonEditorHandler.getDragonPart(EnumSkinLayer.EXTRA, partKey, dragonSpecies.getKey());
                if (text == null) {
                    DragonSurvival.LOGGER.error("Key {} not found!", partKey);
                    return true;
                }
                return !text.isRandom();
            });

            // Don't actually modify the skin preset here, do it inside setSkinPresetAction
            SkinPreset preset = new SkinPreset();
            preset.deserializeNBT(access, this.preset.serializeNBT(access));

            for (EnumSkinLayer layer : EnumSkinLayer.values()) {
                ArrayList<String> keys = DragonEditorHandler.getDragonPartKeys(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), layer);

                if (Objects.equals(layer.name, "Extra")) {
                    keys = extraKeys;
                }

                if (layer != EnumSkinLayer.BASE) {
                    keys.add(DefaultPartLoader.NO_PART);
                }

                if (!keys.isEmpty()) {
                    String key = keys.get(Objects.requireNonNull(minecraft.player).getRandom().nextInt(keys.size()));

                    if (Objects.equals(layer.name, "Extra")) {
                        extraKeys.remove(key);
                    }

                    LayerSettings settings = preset.get(dragonStage.getKey()).get().layerSettings.get(layer).get();
                    settings.partKey = key;
                    DragonPart text = DragonEditorHandler.getDragonPart(layer, key, dragonSpecies.getKey());

                    if (text != null && text.isHueRandom()) {
                        settings.hue = minecraft.player.getRandom().nextFloat();
                        settings.saturation = 0.25f + minecraft.player.getRandom().nextFloat() * 0.5f;
                        settings.brightness = 0.3f + minecraft.player.getRandom().nextFloat() * 0.3f;
                        settings.modifiedColor = true;
                    } else {
                        if (text != null) {
                            settings.hue = text.averageHue();
                        } else {
                            settings.hue = 0.0f;
                        }
                        settings.saturation = 0.5f;
                        settings.brightness = 0.5f;
                        settings.modifiedColor = true;
                    }
                }
            }

            actionHistory.add(new EditorAction<>(setSkinPresetAction, preset.serializeNBT(access)));
        });
        randomButton.setTooltip(Tooltip.create(Component.translatable(RANDOMIZE)));
        addRenderableWidget(randomButton);

        HoverButton undoButton = new HoverButton(width / 2 - 27, 40, 15, 14, 20, 20,  UNDO_MAIN, UNDO_HOVER, button -> actionHistory.undo());
        undoButton.setTooltip(Tooltip.create(Component.translatable(UNDO)));
        addRenderableWidget(undoButton);

        HoverButton redoButton = new HoverButton(width / 2 + 12, 40, 15, 14, 20, 20, REDO_MAIN, REDO_HOVER, button -> actionHistory.redo());
        redoButton.setTooltip(Tooltip.create(Component.translatable(REDO)));
        addRenderableWidget(redoButton);

        // Copy to all stages button
        HoverButton copyToAllStagesButton = new HoverButton(guiLeft - 75, 10, 18, 18, 18, 18, COPY_ALL_MAIN, COPY_ALL_HOVER, button -> {
            Lazy<DragonStageCustomization> lazy = preset.get(Objects.requireNonNull(dragonStage.getKey()));
            CompoundTag storedPresetData = lazy.get().serializeNBT(access);

            for (Holder<DragonStage> stage : DragonStage.allStages(access)) {
                this.preset.put(Objects.requireNonNull(stage.getKey()), Lazy.of(() -> {
                    DragonStageCustomization customization = new DragonStageCustomization();
                    customization.deserializeNBT(access, storedPresetData);
                    return customization;
                }));
            }

            // Undoing this action is not supported at the moment
            actionHistory.clear();
        });
        copyToAllStagesButton.setTooltip(Tooltip.create(Component.translatable(COPY)));
        addRenderableWidget(copyToAllStagesButton);

        // Help button
        HoverButton helpButton = new HoverButton(guiLeft - 75, height - 30, 20, 20, 20, 20, INFO_MAIN, INFO_HOVER, button -> {});
        helpButton.setTooltip(Tooltip.create(Component.translatable(CUSTOMIZATION)));
        addRenderableWidget(helpButton);

        // Wings button
        wingsButton = new ExtendedButton(guiLeft - 35, height - 30, 20, 20,  Component.translatable(DragonBody.getWingButtonDescription(dragonBody)), button -> {
            actionHistory.add(new EditorAction<>(checkWingsButtonAction, !preset.get(Objects.requireNonNull(dragonStage.getKey())).get().wings));
        }){
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                ResourceLocation texture = preset.get(Objects.requireNonNull(dragonStage.getKey())).get().wings ? ALTERNATIVE_ON : ALTERNATIVE_OFF;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 100);
                guiGraphics.blit(texture, getX(), getY(), 0, 0, 20, 20, 20, 20);
                guiGraphics.pose().popPose();
            }
        };
        addRenderableWidget(wingsButton);

        // Show UI button
        uiButton = new ExtendedButton(guiLeft - 13, height - 30, 20, 20,  Component.translatable(SHOW_UI), button -> {
            showUi = !showUi;
        }){
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                ResourceLocation texture = showUi ? SHOW_UI_ON : SHOW_UI_OFF;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 100);
                guiGraphics.blit(texture, getX(), getY(), 0, 0, 20, 20, 20, 20);
                guiGraphics.pose().popPose();
            }
        };
        uiButton.setTooltip(Tooltip.create(Component.translatable(SHOW_UI)));
        addRenderableWidget(uiButton);

        // Reset button
        HoverButton resetButton = new HoverButton(guiLeft + 9, height - 30, 20, 20, 20, 20, RESET_MAIN, RESET_HOVER, button -> {
            // Don't actually modify the skin preset here, do it inside setSkinPresetAction
            SkinPreset preset = new SkinPreset();

            preset.deserializeNBT(access, this.preset.serializeNBT(access));
            preset.put(dragonStage.getKey(), Lazy.of(() -> new DragonStageCustomization(dragonStage.getKey(), dragonSpecies.getKey())));
            actionHistory.add(new EditorAction<>(setSkinPresetAction, preset.serializeNBT(access)));
        });
        resetButton.setTooltip(Tooltip.create(Component.translatable(RESET)));
        addRenderableWidget(resetButton);

        // Background color button
        backgroundColorButton = new BackgroundColorButton(guiLeft + 31, height - 30, 20, 20, Component.empty(), action -> { /* Nothing to do */ }, this);
        addRenderableWidget(backgroundColorButton);

        // Save slots
        HoverButton slotBackground = new HoverButton(width / 2 + 85, height - 28, 121, 18, 121, 18, SAVE_SLOT_BACKGROUND, SAVE_SLOT_BACKGROUND, button -> { /* Nothing to do */ });
        addRenderableOnly(slotBackground);

        HoverButton slotInfoButton = new HoverButton(width / 2 + 74, height - 28, 17, 18, 20, 20, SLOT_INFO_MAIN, SLOT_INFO_HOVER, button -> { /* Nothing to do */ });
        slotInfoButton.setTooltip(createSlotInfoTooltip());
        addRenderableWidget(slotInfoButton);

        for (int slot = CustomizationFileHandler.STARTING_SLOT; slot <= CustomizationFileHandler.MAX_SAVE_SLOTS; slot++) {
            addRenderableWidget(new DragonEditorSlotButton(width / 2 + 95 + 12 * (slot - 1), height - 26, slot, this, () -> slotInfoButton.setTooltip(createSlotInfoTooltip())));
        }

        HoverButton loadSlotButton = new HoverButton(width / 2 + 182, height - 28, 17, 18, 20, 20, SLOT_LOAD_MAIN, SLOT_LOAD_HOVER, button -> {
            CustomizationFileHandler.SavedCustomization savedCustomization = CustomizationFileHandler.load(selectedSaveSlot);

            if (savedCustomization == null) {
                slotDisplayMessage = SlotDisplayMessage.NO_DATA;
                tickWhenSlotDisplayMessageSet = tick;
                return;
            }

            if (!savedCustomization.getDragonModel().equals(dragonBody.value().customModel())) {
                slotDisplayMessage = SlotDisplayMessage.INVALID_FOR_MODEL;
                tickWhenSlotDisplayMessageSet = tick;
                return;
            }

            if (savedCustomization.getDragonSpecies() != dragonSpecies.getKey()) {
                slotDisplayMessage = SlotDisplayMessage.INVALID_FOR_TYPE;
                tickWhenSlotDisplayMessageSet = tick;
                return;
            }

            actionHistory.add(new EditorAction<>(loadStageCustomizationAction, savedCustomization.getCustomization()));
        });
        loadSlotButton.setTooltip(Tooltip.create(Component.translatable(LOAD)));
        addRenderableWidget(loadSlotButton);

        HoverButton saveSlotButton = new HoverButton(width / 2 + 160, height - 28, 17, 18, 20, 20, SLOT_SAVE_MAIN, SLOT_SAVE_HOVER, button -> {
            CustomizationFileHandler.save(HANDLER, selectedSaveSlot);
            slotDisplayMessage = SlotDisplayMessage.SLOT_SAVED;
            tickWhenSlotDisplayMessageSet = tick;
        });
        saveSlotButton.setTooltip(Tooltip.create(Component.translatable(SAVE)));
        addRenderableWidget(saveSlotButton);

    }

    @Override
    public DragonBodyButton createButton(Holder<DragonBody> dragonBody, int x, int y) {
        return new DragonBodyButton(this, x, y, 25, 25, dragonBody, isEditor, button -> {
            if (!((DragonBodyButton) button).isLocked()) {
                if(dragonBody.value() != this.dragonBody.value()) {
                    if(dragonBody.value().customModel() != this.dragonBody.value().customModel()) {
                        // We don't support undo-redo behavior when swapping between body types that have different custom models
                        actionHistory.clear();
                    } else {
                        actionHistory.add(new DragonEditorScreen.EditorAction<>(dragonBodySelectAction, dragonBody));
                    }
                }
            }
        });
    }

    @Override
    public List<AbstractWidget> getDragonBodyWidgets() {
        return dragonBodyWidgets;
    }

    @Override
    public int getDragonBodyButtonXOffset() {
        return 0;
    }

    @Override
    public int getDragonBodyButtonYOffset() {
        return 28;
    }

    @Override
    public void setDragonBodyButtonOffset(int dragonBodySelectionOffset) {
        this.dragonBodySelectionOffset = dragonBodySelectionOffset;
    }

    @Override
    public int getDragonBodySelectionOffset() {
        return dragonBodySelectionOffset;
    }

    public void update() {
        if (dragonSpecies != null) {
            HANDLER.setType(null, dragonSpecies);
        }

        HANDLER.setBody(null, dragonBody);
        HANDLER.setCurrentSkinPreset(preset);
        HANDLER.setDesiredSize(null, dragonStage.value().sizeRange().min());

        lastSelected = selectedSaveSlot;
    }

    private void initDragonRender() {
        children().removeIf(DragonUIRenderComponent.class::isInstance);

        float yRot = -3, xRot = -5, zoom = 0, xOffset = 0, yOffset = -20;
        if (dragonRender != null) {
            yRot = dragonRender.yRot;
            xRot = dragonRender.xRot;
            zoom = dragonRender.zoom;
            xOffset = dragonRender.xOffset;
            yOffset = dragonRender.yOffset;
        }

        dragonRender = new DragonUIRenderComponent(this, width / 2 - 70, guiTop, 140, 125, () -> FakeClientPlayerUtils.getFakeDragon(0, HANDLER));
        dragonRender.xRot = xRot;
        dragonRender.yRot = yRot;
        dragonRender.zoom = zoom;
        dragonRender.xOffset = xOffset;
        dragonRender.yOffset = yOffset;

        ((ScreenAccessor) this).dragonSurvival$children().addFirst(dragonRender);
    }

    public void confirm() {
        //noinspection DataFlowIssue -> player should be present
        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);

        minecraft.player.level().playSound(minecraft.player, minecraft.player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);

        if (!data.isDragon() || dragonWouldChange(data)) {
            if (dragonSpecies == null && data.species() != null) {
                DragonCommand.reInsertClawTools(minecraft.player);
            }

            data.setBody(minecraft.player, dragonBody);
            data.setType(minecraft.player, dragonSpecies);

            double savedSize = data.getSavedDragonSize(data.speciesKey());
            if (!ServerConfig.saveGrowthStage || savedSize == DragonStateHandler.NO_SIZE) {
                Holder<DragonStage> dragonStage = minecraft.player.registryAccess().holderOrThrow(DragonStages.newborn);
                data.setSize(minecraft.player, dragonStage.value().sizeRange().min());
            } else {
                data.setDesiredSize(minecraft.player, savedSize);
            }

            FlightData.getData(minecraft.player).hasSpin = ServerConfig.saveGrowthStage && FlightData.getData(minecraft.player).hasSpin;

            data.setCurrentSkinPreset(preset);
            data.getSkinData().renderCustomSkin = ClientDragonRenderer.renderCustomSkin;

            AltarData altarData = AltarData.getData(minecraft.player);
            altarData.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
            altarData.hasUsedAltar = true;
            altarData.isInAltar = false;

            PacketDistributor.sendToServer(new SyncAltarCooldown.Data(minecraft.player.getId(), altarData.altarCooldown));
            PacketDistributor.sendToServer(new SyncComplete.Data(minecraft.player.getId(), data.serializeNBT(minecraft.player.registryAccess())));
        } else {
            data.setCurrentSkinPreset(preset);
            PacketDistributor.sendToServer(new SyncPlayerSkinPreset(minecraft.player.getId(), HANDLER.speciesKey(), HANDLER.getCurrentSkinPreset().serializeNBT(minecraft.player.registryAccess())));
        }

        minecraft.player.closeContainer();
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (dragonRender != null && dragonRender.isMouseOver(pMouseX, pMouseY) && (backgroundColorButton == null || !backgroundColorButton.toggled)) {
            return dragonRender.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    public static String partToTranslation(final String part) { // TODO :: the part should be able to provide its translation by itself
        return Translation.Type.SKIN_PART.wrap(DragonEditorScreen.HANDLER.speciesId().getPath() + "." + part.toLowerCase(Locale.ENGLISH));
    }

    @SubscribeEvent
    public static void undoRedoKeybinds(ScreenEvent.KeyPressed.Post event) {
        if (event.getScreen() instanceof DragonEditorScreen screen) {
            if (event.getKeyCode() == GLFW.GLFW_KEY_Z && event.getModifiers() == GLFW.GLFW_MOD_CONTROL) {
                screen.actionHistory.undo();
            } else if (event.getKeyCode() == GLFW.GLFW_KEY_Y && event.getModifiers() == GLFW.GLFW_MOD_CONTROL) {
                screen.actionHistory.redo();
            }
        }
    }

    private Tooltip createSlotInfoTooltip() {
        return Tooltip.create(Component.translatable(SAVING_INFO, Component.literal(CustomizationFileHandler.DIRECTORY + "/" + CustomizationFileHandler.FILE_NAME.apply(selectedSaveSlot)).withColor(DSColors.GOLD)));
    }
}
