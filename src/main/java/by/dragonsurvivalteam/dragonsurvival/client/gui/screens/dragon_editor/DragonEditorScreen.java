package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonBodyScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.SkinsScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.*;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.ColorSelectorButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.UndoRedoButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.*;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.DragonEditorConfirmComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.DragonUIRenderComponent;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.*;
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
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
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
    @Translation(type = Translation.Type.MISC, comments = "In the depths of your soul, lava has begun to roar. You have chosen to become a §cCave dragon.§r")
    private static final String CAVE_DRAGON_CHOICE = Translation.Type.GUI.wrap("dragon_editor.cave_dragon_choice");

    @Translation(type = Translation.Type.MISC, comments = "You feel the need for raw meat, and are covered in grassy scales. You have chosen to become a §aForest dragon.§r")
    private static final String SEA_DRAGON_CHOICE = Translation.Type.GUI.wrap("dragon_editor.sea_dragon_choice");

    @Translation(type = Translation.Type.MISC, comments = "The stormy sea beckons you. You have chosen to become a §3Sea dragon.§r")
    private static final String FOREST_DRAGON_CHOICE = Translation.Type.GUI.wrap("dragon_editor.forest_dragon_choice");

    @Translation(type = Translation.Type.MISC, comments = "Randomize")
    private static final String RANDOMIZE = Translation.Type.GUI.wrap("dragon_editor.randomize");

    @Translation(type = Translation.Type.MISC, comments = "Undo changes")
    private static final String UNDO = Translation.Type.GUI.wrap("dragon_editor.undo");

    @Translation(type = Translation.Type.MISC, comments = "Redo changes")
    private static final String REDO = Translation.Type.GUI.wrap("dragon_editor.redo");

    @Translation(type = Translation.Type.MISC, comments = "You can select any slot here and the result will be automatically saved.")
    private static final String SAVE_SLOT = Translation.Type.GUI.wrap("dragon_editor.save_slot");

    @Translation(type = Translation.Type.MISC, comments = "Click here to copy your current settings to the other growth stages.")
    private static final String COPY = Translation.Type.GUI.wrap("dragon_editor.copy");

    @Translation(type = Translation.Type.MISC, comments = "UI")
    private static final String SHOW_UI = Translation.Type.GUI.wrap("dragon_editor.show_ui");

    @Translation(type = Translation.Type.MISC, comments = "Reset to default")
    private static final String RESET = Translation.Type.GUI.wrap("dragon_editor.reset");

    @Translation(type = Translation.Type.MISC, comments = "Old texture")
    private static final String DEFAULT_SKIN = Translation.Type.GUI.wrap("dragon_editor.default_skin");

    @Translation(type = Translation.Type.MISC, comments = "If you are using a §6texture pack§r to test your custom skin before submitting it, check this box.")
    private static final String DEFAULT_SKIN_INFO = Translation.Type.GUI.wrap("dragon_editor.default_skin_info");

    @Translation(type = Translation.Type.MISC, comments = {
            "■ You chose a dragon species! Now it's time to §6customize§r your dragon. You can select different parts, and change their color freely.",
            "■ You can use §6Preset slots§r to save different appearances. Don't forget to apply your looks to all stages of growth!",
            "■ If you don't know where to start, use the \"§6randomize§r\" button on the top right.§r",
            "§r-§7 Shaders can affect the result. This is especially noticeable on glowing textures.§r",
            "§r-§7 The texture from this editor is only visible if your custom skins are turned off in Skin Tab (dragon inventory). You can learn how to create your own custom skins on the Wiki or Dragon Survival discord."
    })
    private static final String CUSTOMIZATION = Translation.Type.GUI.wrap("dragon_editor.customization");

    public static final DragonStateHandler HANDLER = new DragonStateHandler();

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png");
    private static final ResourceLocation SAVE_ICON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/save_icon.png");
    private static final ResourceLocation RANDOM_ICON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/random_icon.png");
    private static final ResourceLocation RESET_ICON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/reset_button.png");

    public int guiTop;
    public boolean confirmation;
    public boolean showUi = true;

    /** Dragon type of {@link DragonEditorScreen#HANDLER} */
    public Holder<DragonType> dragonType;

    /** Dragon body of {@link DragonEditorScreen#HANDLER} */
    public Holder<DragonBody> dragonBody;

    /** Dragon level of {@link DragonEditorScreen#HANDLER} */
    public Holder<DragonStage> dragonStage;

    public SkinPreset preset;
    public int selectedSaveSlot;

    public int backgroundColor = -804253680;

    private final Screen source;
    private int guiLeft;

    private final String[] animations = {"sit_dentist", "sit_animation", "idle_animation", "fly_animation", "swim_animation", "run_animation", "spinning_on_back"};
    private final HashMap<Holder<DragonStage>, Integer> presetSelections = new HashMap<>();
    private final Map<EnumSkinLayer, DropDownButton> dropdownButtons = new HashMap<>();
    private final Map<EnumSkinLayer, ColorSelectorButton> colorSelectorButtons = new HashMap<>();

    private DragonUIRenderComponent dragonRender;
    private ExtendedCheckbox defaultSkinCheckbox;
    private ExtendedCheckbox showUiCheckbox;
    private DragonEditorConfirmComponent confirmComponent;
    private ExtendedCheckbox wingsCheckbox;

    /**
     * Widgets which belong to the dragon body logic <br>
     * (they are stored to properly reference (and remove) them when using the arrow buttons to navigate through the bodies)
     */
    private final List<AbstractWidget> dragonBodyWidgets = new ArrayList<>();
    private int dragonBodySelectionOffset;

    private float tick;
    private int curAnimation;
    private int lastSelected;
    private int selectedDragonStage;
    private DragonStageButton savedStageButton;
    private boolean hasInit;
    private boolean isEditor;

    public DragonEditorScreen(Screen source) {
        this(source, null);
        this.isEditor = true;
    }

    public DragonEditorScreen(Screen source, Holder<DragonType> dragonType) {
        super(Component.translatable(LangKey.GUI_DRAGON_EDITOR));
        this.source = source;
        this.dragonType = dragonType;
    }

    public record EditorAction<T>(Function<T, T> action, T value) {
        public T run() {
            return action.apply(value);
        }

        @Override
        public boolean equals(Object object) {
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
        HANDLER.getSkinData().compileSkin(dragonStage);
        if(savedStageButton != null) {
            savedStageButton.setMessage(DragonStage.translatableName(dragonStage.getKey()));
        }
        update();

        return previousLevel;
    };

    // setHueAction, setSaturationAction, setBrightnessAction in HueSelectorComponent.Java
    // setDragonSlotAction in DragonEditorSlotButton.Java

    public final Function<CompoundTag, CompoundTag> setSkinPresetAction = tag -> {
        CompoundTag prevTag = HANDLER.getSkinData().skinPreset.serializeNBT(Objects.requireNonNull(Minecraft.getInstance().player).registryAccess());
        HANDLER.getSkinData().skinPreset.deserializeNBT(Minecraft.getInstance().player.registryAccess(), tag);
        HANDLER.getSkinData().compileSkin(dragonStage);
        update();
        return prevTag;
    };

    public final Function<Holder<DragonBody>, Holder<DragonBody>> dragonBodySelectAction = dragonBody -> {
        Holder<DragonBody> previousBody = this.dragonBody;
        this.dragonBody = dragonBody;
        update();
        return previousBody;
    };

    public final Function<Pair<EnumSkinLayer, String>, Pair<EnumSkinLayer, String>> dragonPartSelectAction = pair -> {
        Pair<EnumSkinLayer, String> previousPair = new Pair<>(pair.getFirst(), preset.get(Objects.requireNonNull(dragonStage.getKey())).get().layerSettings.get(pair.getFirst()).get().selectedSkin);

        EnumSkinLayer layer = pair.getFirst();
        String value = pair.getSecond();
        dropdownButtons.get(layer).current = value;
        dropdownButtons.get(layer).updateMessage();
        preset.get(dragonStage.getKey()).get().layerSettings.get(layer).get().selectedSkin = DragonEditorScreen.partToTechnical(value);

        // Make sure that when we change a part, the color is properly updated to the default color of the new part
        LayerSettings settings = preset.get(dragonStage.getKey()).get().layerSettings.get(layer).get();

        DragonPart text = DragonEditorHandler.getDragonPart(layer, settings.selectedSkin, dragonType.getKey());
        if (text != null && !settings.modifiedColor) {
            settings.hue = text.averageHue();
        }

        HANDLER.getSkinData().compileSkin(dragonStage);
        update();

        return previousPair;
    };

    public final Function<Boolean, Boolean> checkWingsButtonAction = (selected) -> {
        boolean prevSelected = !wingsCheckbox.selected;
        wingsCheckbox.selected = selected;
        preset.get(Objects.requireNonNull(dragonStage.getKey())).get().wings = selected;
        HANDLER.getSkinData().compileSkin(dragonStage);
        update();
        return prevSelected;
    };

    public final Function<Boolean, Boolean> checkDefaultSkinAction = (selected) -> {
        boolean prevSelected = !defaultSkinCheckbox.selected;
        defaultSkinCheckbox.selected = selected;
        preset.get(Objects.requireNonNull(dragonStage.getKey())).get().defaultSkin = selected;
        HANDLER.getSkinData().compileSkin(dragonStage);
        update();
        return prevSelected;
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
    public void render(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (dragonRender == null) { // In the past this could occur when using the dragon editor command before using the dragon altar first
            init();
        }

        tick += partialTick;
        if (tick >= 60 * 20) {
            save();
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
        TextRenderUtil.drawCenteredScaledText(graphics, width / 2, 10, 2f, title.getString(), DyeColor.WHITE.getTextColor());

        if (showUi) {
            int i = 0;

            for (EnumSkinLayer layers : EnumSkinLayer.values()) {
                String name = layers.name;
                SkinsScreen.drawNonShadowLineBreak(graphics, font, Component.translatable(Translation.Type.SKIN_PART.wrap(name.toLowerCase(Locale.ENGLISH))), (i < 5 ? width / 2 - 100 - 100 : width / 2 + 83) + 45, guiTop + 10 + (i >= 5 ? (i - 5) * 30 : i * 30) - 12, DyeColor.WHITE.getTextColor());
                i++;
            }

            SkinsScreen.drawNonShadowLineBreak(graphics, font, Component.empty().append(WordUtils.capitalize(animations[curAnimation].replace("_", " "))), width / 2, height / 2 + 75 - 22, DyeColor.GRAY.getTextColor());
        }

        for (Renderable renderable : new CopyOnWriteArrayList<>(renderables)) {
            if (renderable instanceof AbstractWidget widget && widget != showUiCheckbox) {
                if(widget == wingsCheckbox) {
                    if(dragonBody != null && dragonBody.value().canHideWings() && widget == wingsCheckbox) {
                        wingsCheckbox.visible = showUi;
                        wingsCheckbox.setTooltip(Tooltip.create(Component.translatable(DragonBody.getWingButtonDescription(dragonBody))));
                        wingsCheckbox.setMessage(Component.translatable(DragonBody.getWingButtonName(dragonBody)));
                    } else {
                        wingsCheckbox.visible = false;
                    }
                } else {
                    widget.visible = showUi;
                }
            }

            renderable.render(graphics, mouseX, mouseY, partialTick);
        }

        if (showUi) {
            for (ColorSelectorButton colorSelectorButton : colorSelectorButtons.values()) {
                DragonPart text = DragonEditorHandler.getDragonPart(colorSelectorButton.layer, preset.get(Objects.requireNonNull(dragonStage.getKey())).get().layerSettings.get(colorSelectorButton.layer).get().selectedSkin, HANDLER.getType().getKey());
                colorSelectorButton.visible = (text != null && text.isColorable()) && !defaultSkinCheckbox.selected;
            }
        }


        defaultSkinCheckbox.selected = preset.get(Objects.requireNonNull(dragonStage.getKey())).get().defaultSkin;
        showUiCheckbox.visible = true;
    }

    public SkinPreset save() {
        RegistryAccess access = Objects.requireNonNull(Minecraft.getInstance().player).registryAccess();

        SkinPreset newPreset = new SkinPreset();
        newPreset.deserializeNBT(access, preset.serializeNBT(access));

        SavedSkinPresets savedCustomizations = DragonEditorRegistry.getSavedCustomizations(access);

        savedCustomizations.skinPresets.computeIfAbsent(dragonType.getKey(), key -> new HashMap<>());
        savedCustomizations.skinPresets.get(dragonType.getKey()).put(selectedSaveSlot, newPreset);

        for (Holder<DragonStage> dragonStage : presetSelections.keySet()) {
            savedCustomizations.current.get(dragonType.getKey()).put(Objects.requireNonNull(dragonStage.getKey()).location().toString(), presetSelections.get(dragonStage));
        }

        DragonEditorRegistry.save(savedCustomizations);
        return newPreset;
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.fill(0, 0, width, height, -350, backgroundColor);
    }

    private void initDummyDragon(final DragonStateHandler localHandler) {
        if (dragonType == null && localHandler.isDragon()) {
            dragonType = localHandler.getType();
            dragonStage = localHandler.getStage();
            dragonBody = localHandler.getBody();
        } else if (dragonType != null) {
            if (dragonStage == null) {
                dragonStage = ResourceHelper.get(null, DragonStages.newborn).orElseThrow();
            }

            if (dragonBody == null) {
                dragonBody = DragonBody.random(null);
            }
        } else {
            return;
        }

        SavedSkinPresets savedCustomizations = DragonEditorRegistry.getSavedCustomizations(null);
        String stageLocation = Objects.requireNonNull(dragonStage.getKey()).location().toString();

        savedCustomizations.current.computeIfAbsent(dragonType.getKey(), key -> new HashMap<>());
        savedCustomizations.current.get(dragonType.getKey()).putIfAbsent(stageLocation, 0);

        selectedSaveSlot = savedCustomizations.current.get(dragonType.getKey()).get(stageLocation);

        savedCustomizations.skinPresets.computeIfAbsent(dragonType.getKey(), key -> new HashMap<>());
        savedCustomizations.skinPresets.get(dragonType.getKey()).computeIfAbsent(selectedSaveSlot, key -> {
            SkinPreset newPreset = new SkinPreset();
            newPreset.initDefaults(dragonType.getKey());
            return newPreset;
        });

        // TODO :: there could be a desync between what the player actually has? noticed it with base being no-part here but player having one
        //  (maybe only related to base because it forcefully gets set to a non no-part value)
        SkinPreset currentPreset = savedCustomizations.skinPresets.get(dragonType.getKey()).get(selectedSaveSlot);
        preset = new SkinPreset();
        preset.deserializeNBT(Objects.requireNonNull(Minecraft.getInstance().player).registryAccess(), currentPreset.serializeNBT(Minecraft.getInstance().player.registryAccess()));

        HANDLER.setType(dragonType);
        HANDLER.setSize(null, dragonStage.value().sizeRange().min());
        HANDLER.setBody(dragonBody);

        HANDLER.getSkinData().skinPreset = preset;
        HANDLER.getSkinData().compileSkin(dragonStage);

        dragonRender.zoom = setZoom(dragonStage);
    }

    private boolean dragonTypeWouldChange(DragonStateHandler handler) {
        return handler.getType() != null && !handler.getType().equals(dragonType);
    }

    private boolean dragonBodyWouldChange(DragonStateHandler handler) {
        return handler.getBody() != null && !handler.getBody().equals(dragonBody);
    }

    public boolean dragonWouldChange(DragonStateHandler handler) {
        return (handler.getType() != null && !handler.getType().equals(dragonType)) || (handler.getBody() != null && !handler.getBody().equals(dragonBody));
    }

    @Override
    public void init() {
        super.init();
        assert minecraft != null && minecraft.player != null;

        guiLeft = (width - 256) / 2;
        guiTop = (height - 120) / 2;

        confirmComponent = new DragonEditorConfirmComponent(this, width / 2 - 130 / 2, height / 2 - 181 / 2, 130, 154);
        initDragonRender();

        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);

        if (!hasInit) {
            initDummyDragon(data);
            update();
            hasInit = true;
        }

        if (DragonStage.onlyBuiltInLevelsAreLoaded(minecraft.player.registryAccess())) {
            addRenderableWidget(new DragonStageButton(this, DragonStages.newborn, -180));
            addRenderableWidget(new DragonStageButton(this, DragonStages.young, -60));
            addRenderableWidget(new DragonStageButton(this, DragonStages.adult, 60));
        } else {
            selectedDragonStage = DragonStage.allStages(minecraft.player.registryAccess()).indexOf(dragonStage);
            addRenderableWidget(new ArrowButton(ArrowButton.Type.PREVIOUS, width / 2 - 45 - 20, height / 2 - 90, 15, 15, action -> {
                List<Holder<DragonStage>> allStages = DragonStage.allStages(minecraft.player.registryAccess());
                selectedDragonStage = Functions.wrap(selectedDragonStage - 1, 0, allStages.size() - 1);
                actionHistory.add(new EditorAction<>(selectStageAction, allStages.get(selectedDragonStage)));
            }));

            savedStageButton = new DragonStageButton(this, dragonStage.getKey(), -60, false);
            addRenderableWidget(savedStageButton);

            addRenderableWidget(new ArrowButton(ArrowButton.Type.NEXT, width / 2 + 45, height / 2 - 90, 15, 15, action -> {
                List<Holder<DragonStage>> allStages = DragonStage.allStages(minecraft.player.registryAccess());
                selectedDragonStage = Functions.wrap(selectedDragonStage + 1, 0, allStages.size() - 1);
                actionHistory.add(new EditorAction<>(selectStageAction, allStages.get(selectedDragonStage)));
            }));
        }

        addDragonBodyWidgets();

        int maxWidth = -1;

        for (EnumSkinLayer layer : EnumSkinLayer.values()) {
            String name = layer.getNameUpperCase().charAt(0) + layer.getNameLowerCase().substring(1).replace("_", " ");
            maxWidth = (int) Math.max(maxWidth, font.width(name) * 1.45F);
        }

        int row = 0;
        for (EnumSkinLayer layers : EnumSkinLayer.values()) {
            ArrayList<String> valueList = DragonEditorHandler.getDragonPartKeys(dragonType, dragonBody, layers);

            if (layers != EnumSkinLayer.BASE) {
                valueList.addFirst(DefaultPartLoader.NO_PART);
            }

            String[] values = valueList.toArray(new String[0]);
            String curValue = partToTranslation(preset.get(Objects.requireNonNull(dragonStage.getKey())).get().layerSettings.get(layers).get().selectedSkin);

            DropDownButton btn = new DragonEditorDropdownButton(this, row < 8 ? width / 2 - 210 : width / 2 + 80, guiTop - 5 + (row >= 8 ? (row - 8) * 20 : row * 20), 100, 15, curValue, values, layers);
            dropdownButtons.put(layers, btn);
            addRenderableWidget(btn);
            addRenderableWidget(new PlusMinusButton(btn.getX() - 15, btn.getY() + 1, 16, 16, false, action -> {
                int index = 0;

                for (int i1 = 0; i1 < btn.values.length; i1++) {
                    if (Objects.equals(btn.values[i1], btn.current)) {
                        index = i1;
                        break;
                    }
                }

                index = Functions.wrap(index - 1, 0, btn.values.length - 1);
                btn.current = btn.values[index];
                btn.setter.accept(btn.current);
                btn.updateMessage();

                LayerSettings settings = preset.get(dragonStage.getKey()).get().layerSettings.get(layers).get();
                DragonPart text = DragonEditorHandler.getDragonPart(layers, settings.selectedSkin, dragonType.getKey());
                if (text != null && !settings.modifiedColor) {
                    settings.hue = text.averageHue();
                }
            }));

            addRenderableWidget(new PlusMinusButton(btn.getX() + btn.getWidth() - 1, btn.getY() + 1, 16, 16, true, action -> {
                int index = 0;

                for (int i1 = 0; i1 < btn.values.length; i1++) {
                    if (Objects.equals(btn.values[i1], btn.current)) {
                        index = i1;
                        break;
                    }
                }

                index = Functions.wrap(index + 1, 0, btn.values.length - 1);
                btn.current = btn.values[index];
                btn.setter.accept(btn.current);
                btn.updateMessage();

                LayerSettings settings = preset.get(dragonStage.getKey()).get().layerSettings.get(layers).get();
                DragonPart text = DragonEditorHandler.getDragonPart(layers, settings.selectedSkin, dragonType.getKey());
                if (text != null && !settings.modifiedColor) {
                    settings.hue = text.averageHue();
                }
            }));

            ColorSelectorButton colorButton = new ColorSelectorButton(this, layers, btn.getX() + 14 + btn.getWidth() + 2, btn.getY(), btn.getHeight(), btn.getHeight());
            colorSelectorButtons.put(layers, colorButton);
            addRenderableWidget(colorButton);
            row++;
        }

        addRenderableWidget(new ArrowButton(ArrowButton.Type.PREVIOUS, width / 2 - 45 - 20, height / 2 + 75 - 27, 15, 15, action -> {
            curAnimation -= 1;

            if (curAnimation < 0) {
                curAnimation = animations.length - 1;
            }
        }));

        addRenderableWidget(new ArrowButton(ArrowButton.Type.NEXT, width / 2 + 45, height / 2 + 75 - 27, 15, 15, action -> {
            curAnimation += 1;

            if (curAnimation >= animations.length) {
                curAnimation = 0;
            }
        }));

        for (int num = 1; num <= 9; num++) {
            addRenderableWidget(new DragonEditorSlotButton(width / 2 + 200 + 15, guiTop + (num - 1) * 12 + 5 + 30, num, this));
        }

        wingsCheckbox = new ExtendedCheckbox(width / 2 - 220, height - 25, 120, 17, 17, Component.translatable(DragonBody.getWingButtonName(dragonBody)), preset.get(dragonStage.getKey()).get().wings, p -> actionHistory.add(new EditorAction<>(checkWingsButtonAction, p.selected())));
        wingsCheckbox.setTooltip(Tooltip.create(Component.translatable(DragonBody.getWingButtonDescription(dragonBody))));
        wingsCheckbox.selected = preset.get(dragonStage.getKey()).get().wings;
        addRenderableWidget(wingsCheckbox);

        defaultSkinCheckbox = new ExtendedCheckbox(width / 2 + 100, height - 25, 120, 17, 17, Component.translatable(DEFAULT_SKIN), preset.get(dragonStage.getKey()).get().defaultSkin, p -> actionHistory.add(new EditorAction<>(checkDefaultSkinAction, p.selected())));
        defaultSkinCheckbox.setTooltip(Tooltip.create(Component.translatable(DEFAULT_SKIN_INFO)));
        addRenderableWidget(defaultSkinCheckbox);

        ExtendedButton saveButton = new ExtendedButton(width / 2 - 75 - 10, height - 25, 75, 20, Component.translatable(LangKey.GUI_CONFIRM), action -> { /* Nothing to do */ }) {
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
                assert minecraft != null && minecraft.player != null;
                DragonStateHandler handler = DragonStateProvider.getData(minecraft.player);
                minecraft.player.level().playSound(minecraft.player, minecraft.player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);
                boolean dragonDataIsPreserved = ServerConfig.saveAllAbilities && ServerConfig.saveGrowthStage;

                if (handler.isDragon() && dragonWouldChange(handler) && !dragonDataIsPreserved) {
                    confirmation = true;
                    confirmComponent.isBodyTypeChange = dragonBodyWouldChange(handler) && !dragonTypeWouldChange(handler);
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

        addRenderableWidget(saveButton);

        ExtendedButton discardButton = new ExtendedButton(width / 2 + 10, height - 25, 75, 20, Component.translatable(LangKey.GUI_CANCEL), btn -> Minecraft.getInstance().setScreen(source));
        addRenderableWidget(discardButton);

        RegistryAccess access = Objects.requireNonNull(Minecraft.getInstance().player).registryAccess();

        ExtendedButton resetButton = new ExtendedButton(guiLeft + 290, 11, 18, 18, Component.empty(), button -> {
            // Don't actually modify the skin preset here, do it inside setSkinPresetAction
            SkinPreset preset = new SkinPreset();

            preset.deserializeNBT(access, this.preset.serializeNBT(access));
            preset.put(dragonStage.getKey(), Lazy.of(() -> new DragonStageCustomization(dragonStage.getKey().location(), dragonType.getKey())));
            wingsCheckbox.selected = true;
            actionHistory.add(new EditorAction<>(setSkinPresetAction, preset.serializeNBT(access)));
        }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                guiGraphics.blit(RESET_ICON, getX(), getY(), 0, 0, width, height, width, height);
            }
        };
        resetButton.setTooltip(Tooltip.create(Component.translatable(RESET)));
        addRenderableWidget(resetButton);

        ExtendedButton randomButton = new ExtendedButton(guiLeft + 260, 11, 18, 18, Component.empty(), btn -> {
            ArrayList<String> extraKeys = DragonEditorHandler.getDragonPartKeys(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), EnumSkinLayer.EXTRA);

            extraKeys.removeIf(partKey -> {
                DragonPart text = DragonEditorHandler.getDragonPart(EnumSkinLayer.EXTRA, partKey, dragonType.getKey());
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
                    settings.selectedSkin = key;
                    DragonPart text = DragonEditorHandler.getDragonPart(layer, key, dragonType.getKey());

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
        }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                guiGraphics.blit(RANDOM_ICON, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }
        };
        randomButton.setTooltip(Tooltip.create(Component.translatable(RANDOMIZE)));
        addRenderableWidget(randomButton);

        UndoRedoButton undoButton = new UndoRedoButton(guiLeft + 318, 11, 18, 18, false, button -> actionHistory.undo());
        undoButton.setTooltip(Tooltip.create(Component.translatable(UNDO)));
        addRenderableWidget(undoButton);

        UndoRedoButton redoButton = new UndoRedoButton(guiLeft + 340, 11, 18, 18, true, button -> actionHistory.redo());
        redoButton.setTooltip(Tooltip.create(Component.translatable(REDO)));
        addRenderableWidget(redoButton);

        ExtendedButton saveSlotButton = new ExtendedButton(width / 2 + 213, guiTop + 10, 18, 18, Component.empty(), button -> { /* Nothing to do */ }) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(SAVE_ICON, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }
        };
        saveSlotButton.setTooltip(Tooltip.create(Component.translatable(SAVE_SLOT)));
        addRenderableWidget(saveSlotButton);

        addRenderableWidget(new CopySettingsButton(this, guiLeft + 230, 11, 18, 18, Component.translatable(COPY), button -> { /* Nothing to do */ }));

        showUiCheckbox = new ExtendedCheckbox(guiLeft - 15, 11, 40, 18, 18, Component.translatable(SHOW_UI), showUi, p -> showUi = p.selected());
        addRenderableWidget(showUiCheckbox);
        addRenderableWidget(new BackgroundColorButton(guiLeft - 45, 11, 18, 18, Component.empty(), action -> { /* Nothing to do */ }, this));
        addRenderableWidget(new HelpButton(guiLeft - 75, 11, 15, 15, CUSTOMIZATION));
    }

    @Override
    public DragonBodyButton createButton(Holder<DragonBody> dragonBody, int x, int y) {
        return new DragonBodyButton(this, x, y, 25, 25, dragonBody, isEditor, button -> {
            if (!((DragonBodyButton) button).isLocked()) {
                actionHistory.add(new DragonEditorScreen.EditorAction<>(dragonBodySelectAction, dragonBody));
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
        return 69;
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
        if (dragonType != null) {
            HANDLER.setType(dragonType);
        }

        HANDLER.setBody(dragonBody);
        HANDLER.getSkinData().skinPreset = preset;
        HANDLER.setSize(null, dragonStage.value().sizeRange().min());

        if (selectedSaveSlot != lastSelected) {
            RegistryAccess access = Objects.requireNonNull(Minecraft.getInstance().player).registryAccess();

            preset = new SkinPreset();
            SavedSkinPresets savedCustomizations = DragonEditorRegistry.getSavedCustomizations(access);

            if (savedCustomizations.skinPresets.containsKey(dragonType.getKey())) {
                preset.deserializeNBT(access, savedCustomizations.skinPresets.get(dragonType.getKey()).get(selectedSaveSlot).serializeNBT(access));
            }

            HANDLER.getSkinData().skinPreset = preset;
        }

        presetSelections.put(dragonStage, selectedSaveSlot);
        lastSelected = selectedSaveSlot;
    }

    private void initDragonRender() {
        children().removeIf(DragonUIRenderComponent.class::isInstance);

        float yRot = -3, xRot = -5, zoom = 0, xOffset = 0, yOffset = 0;
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
            // FIXME
            /*String translationKey = switch (dragonType) {
                case CaveDragonType ignored -> CAVE_DRAGON_CHOICE;
                case SeaDragonType ignored -> SEA_DRAGON_CHOICE;
                case ForestDragonType ignored -> FOREST_DRAGON_CHOICE;
                default -> throw new IllegalStateException("Invalid dragon type [" + dragonType + "]");
            };*/

            minecraft.player.sendSystemMessage(Component.translatable(CAVE_DRAGON_CHOICE));

            if (dragonType == null && data.getType() != null) {
                DragonCommand.reInsertClawTools(minecraft.player);
            }

            data.setType(dragonType, minecraft.player);
            data.setBody(dragonBody, minecraft.player);

            double savedSize = data.getSavedDragonSize(data.getType().getKey());
            if (!ServerConfig.saveGrowthStage || savedSize == DragonStateHandler.NO_SIZE) {
                Holder<DragonStage> dragonStage = minecraft.player.registryAccess().holderOrThrow(DragonStages.newborn);
                data.setStage(minecraft.player, dragonStage);
            } else {
                data.setSize(minecraft.player, savedSize);
            }

            FlightData.getData(minecraft.player).hasSpin = ServerConfig.saveGrowthStage && FlightData.getData(minecraft.player).hasSpin;

            HANDLER.getSkinData().skinPreset = save();
            data.getSkinData().renderCustomSkin = ClientDragonRenderer.renderCustomSkin;

            AltarData altarData = AltarData.getData(minecraft.player);
            altarData.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
            altarData.hasUsedAltar = true;
            altarData.isInAltar = false;

            PacketDistributor.sendToServer(new SyncAltarCooldown.Data(minecraft.player.getId(), altarData.altarCooldown));
            PacketDistributor.sendToServer(new SyncComplete.Data(minecraft.player.getId(), data.serializeNBT(minecraft.player.registryAccess())));
        } else {
            PacketDistributor.sendToServer(new SyncPlayerSkinPreset.Data(minecraft.player.getId(), save().serializeNBT(minecraft.player.registryAccess())));
        }

        minecraft.player.closeContainer();
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (dragonRender != null && dragonRender.isMouseOver(pMouseX, pMouseY)) {
            return dragonRender.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    public static String partToTranslation(final String part) {
        return Translation.Type.SKIN_PART.wrap(DragonEditorScreen.HANDLER.getTypeNameLowerCase() + "." + part.toLowerCase(Locale.ENGLISH));
    }

    public static String partToTechnical(final String key) {
        return Translation.Type.SKIN_PART.unwrap(key).replace(DragonEditorScreen.HANDLER.getTypeNameLowerCase() + ".", "");
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
}
