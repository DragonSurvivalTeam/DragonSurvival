package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.AltarTypeButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverDisableable;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.BarComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.DragonEditorConfirmComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.ScrollableComponent;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.UnlockableBehavior;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.mixins.HolderSet$NamedAccess;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonSpeciesTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonAltarScreen extends Screen implements ConfirmableScreen {
    @Translation(comments = "Choose a Species")
    private static final String CHOOSE_SPECIES = Translation.Type.GUI.wrap("altar.choose_species");

    @Translation(comments = {
            "You didn't make your initial choice in the Dragon Altar!",
            "You may still open the altar in your inventory."
    })
    private static final String NO_CHOICE = Translation.Type.GUI.wrap("altar.no_choice");

    @Translation(comments = {
            "Â§6â–  Welcome to Dragon Survival!Â§r",
            "â–  You can choose which Â§6speciesÂ§rÂ§r you want to become. You can change your selection using the Altar, but you may lose progress.",
            "â– Â§7 Don't forget to read patch notes if you update our mod to avoid bugs!",
            "â– Â§7 If you want to play as other species (griffins, eastern dragons, bees, and others), install add-ons or create them yourself via datapacks!",
            "â– Â§7 Enjoy the game! :3"
    })
    private static final String HELP = Translation.Type.GUI.wrap("altar.help");

    @Translation(comments = "Dragon Survival")
    private static final String TITLE = Translation.Type.GUI.wrap("altar.title");

    private static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/block/black_concrete.png");

    private final DragonStateHandler handler1 = new DragonStateHandler();
    private final DragonStateHandler handler2 = new DragonStateHandler();
    private final String[] animations = {"sit", "idle", "fly", "swim", "run", "dig", "vibing_sitting", "shy_sitting", "rocking_on_back"};
    private final List<ScrollableComponent> scrollableComponents = new ArrayList<>();

    private static final Identifier ALTAR_ARROW_LEFT_HOVER = Identifier.fromNamespaceAndPath(MODID, "textures/gui/altar/arrow_left_hover.png");
    private static final Identifier ALTAR_ARROW_LEFT_MAIN = Identifier.fromNamespaceAndPath(MODID, "textures/gui/altar/arrow_left_main.png");
    private static final Identifier ALTAR_ARROW_RIGHT_HOVER = Identifier.fromNamespaceAndPath(MODID, "textures/gui/altar/arrow_right_hover.png");
    private static final Identifier ALTAR_ARROW_RIGHT_MAIN = Identifier.fromNamespaceAndPath(MODID, "textures/gui/altar/arrow_right_main.png");

    private static final Identifier INFO_HOVER = Identifier.fromNamespaceAndPath(MODID, "textures/gui/altar/info_hover.png");
    private static final Identifier INFO_MAIN = Identifier.fromNamespaceAndPath(MODID, "textures/gui/altar/info_main.png");

    private final List<UnlockableBehavior.SpeciesEntry> entries;

    private boolean hasInit = false;
    private int animation1 = 1;
    private int animation2 = 0;
    private int tick;

    private DragonEditorConfirmComponent confirmComponent;
    private AltarTypeButton humanButton;
    private Renderable renderButton;
    private boolean confirmation;

    public DragonAltarScreen(final List<UnlockableBehavior.SpeciesEntry> entries) {
        super(Component.translatable(CHOOSE_SPECIES));

        Minecraft.getInstance().player.registryAccess().lookupOrThrow(DragonSpecies.REGISTRY).get(DSDragonSpeciesTags.ORDER).ifPresent(order -> {
            //noinspection unchecked
            List<Holder<DragonSpecies>> list = ((HolderSet$NamedAccess<DragonSpecies>) order).dragonSurvival$contents();

            Comparator<UnlockableBehavior.SpeciesEntry> comparator = Comparator.comparingInt(entry -> {
                int index = list.indexOf(entry.species());
                return index == -1 ? Integer.MAX_VALUE : index;
            });

            entries.sort(comparator);
        });

        this.entries = entries;
    }

    @Override
    public void onClose() {
        super.onClose();
        LocalPlayer player = Minecraft.getInstance().player;
        AltarData data = AltarData.getData(player);
        data.isInAltar = false;

        if (!data.hasUsedAltar) {
            data.hasUsedAltar = DragonStateProvider.isDragon(player);
        }

        if (!data.hasUsedAltar) {
            player.displayClientMessage(Component.translatable(NO_CHOICE), false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (ScrollableComponent component : scrollableComponents) {
            component.scroll(mouseX, mouseY, scrollX, scrollY);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(@NotNull final MouseButtonEvent event, final boolean isDoubleClick) {
        if (confirmComponent != null && confirmation) {
            for (GuiEventListener listener : confirmComponent.children()) {
                if (listener.mouseClicked(event, isDoubleClick)) {
                    setFocused(listener);
                    if (event.button() == 0) {
                        setDragging(true);
                    }

                    return true;
                }
            }

            return false;
        }

        for (GuiEventListener listener : children()) {
            if (listener.mouseClicked(event, isDoubleClick)) {
                setFocused(listener);
                if (event.button() == 0) {
                    setDragging(true);
                }

                if (confirmComponent != null && confirmation) {
                    for (GuiEventListener child : children()) {
                        if (child instanceof HoverDisableable hoverDisableable) {
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
        renderBackground(graphics, mouseX, mouseY, partialTick);

        tick++;

        if (tick % (200 * 20) == 0) {
            animation1++;
            animation2++;

            if (handler1.species() != null && handler2.species() != null) {
                RegistryAccess access = getMinecraft().player.registryAccess();

                if (handler1.body() == null) {
                    handler1.setBody(null, DragonBody.getRandom(access, handler1.species()));
                }

                handler2.setBody(null, handler1.body());
                handler1.setBody(null, DragonBody.getRandom(access, handler1.species()));

                if (animation1 >= animations.length) {
                    animation1 = 0;
                }

                if (animation2 >= animations.length) {
                    animation2 = 0;
                }
            }
        }

        if (!confirmation) {
            children().removeIf(child -> child == confirmComponent);
            renderables.removeIf(renderable -> renderable == renderButton);
        }

        // Pass 1: update hovered preview handlers before we draw any world/entity previews.
        for (Renderable btn : renderables) {
            if (btn instanceof AltarTypeButton button && button.isHovered()) {
                Holder<DragonSpecies> handler1PreviousSpecies = handler1.species();
                Holder<DragonSpecies> handler2PreviousSpecies = handler2.species();

                Holder<DragonSpecies> species = button.speciesEntry != null ? button.speciesEntry.species() : null;
                handler1.setSpecies(null, species);
                handler2.setSpecies(null, species);

                if (handler1.species() != null && !DragonUtils.isSpecies(handler1.species(), handler1PreviousSpecies)) {
                    initializeHandler(handler1);
                }

                if (handler2.species() != null && !DragonUtils.isSpecies(handler2.species(), handler2PreviousSpecies)) {
                    initializeHandler(handler2);
                }

                FakeClientPlayerUtils.getFakePlayer(0, handler1).animationSupplier = () -> animations[Math.min(animation1, animations.length - 1)];
                FakeClientPlayerUtils.getFakePlayer(1, handler2).animationSupplier = () -> animations[Math.min(animation2, animations.length - 1)];

                LivingEntity entity1;
                int entity1Scale = Math.clamp((int) handler1.getGrowth(), 20, 50);

                if (handler1.isDragon()) {
                    entity1 = FakeClientPlayerUtils.getFakeDragon(0, handler1);
                    DragonEntity dragon = (DragonEntity) entity1;
                    dragon.neckLocked = true;
                    dragon.tailLocked = true;
                } else {
                    entity1 = FakeClientPlayerUtils.getFakePlayer(0, handler1);
                    entity1Scale = 40;
                }

                LivingEntity entity2;
                int entity2Scale = Math.clamp((int) handler2.getGrowth(), 20, 50);

                if (handler2.isDragon()) {
                    entity2 = FakeClientPlayerUtils.getFakeDragon(1, handler2);
                    DragonEntity dragon = (DragonEntity) entity2;
                    dragon.neckLocked = true;
                    dragon.tailLocked = true;
                } else {
                    entity2 = FakeClientPlayerUtils.getFakePlayer(1, handler2);
                    entity2Scale = 40;
                }

                // Pass 2: draw previews after the background, but before widgets/text.
                renderPreviewEntity(graphics, entity1, (int) ((width / 2f) - 180), button.getY() + button.getHeight(), entity1Scale, true);
                renderPreviewEntity(graphics, entity2, (int) ((width / 2f) + 180), button.getY() + button.getHeight(), entity2Scale, false);
            }

            if (!confirmation && btn instanceof HoverDisableable hoverDisableable) {
                hoverDisableable.enableHover();
            }
        }

        // Pass 3: render standard widgets and tooltips on top of the background/previews.
        super.render(graphics, mouseX, mouseY, partialTick);

        // Pass 4: title/overlay text last so it stays above widget chrome and preview entities.
        TextRenderUtil.drawCenteredScaledText(graphics, width / 2 + 7, 10, 2f, Component.translatable(TITLE).getString(), DyeColor.WHITE.getTextColor());

        // Pass 5: modal confirmation overlay always renders above the rest of the altar UI.
        if (confirmation && confirmComponent != null) {
            confirmComponent.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderPreviewEntity(final GuiGraphics graphics, final LivingEntity entity, final int centerX, final int bottomY, final int scale, final boolean leftSide) {
        int halfWidth = Math.max(24, scale);
        int topY = bottomY - Math.max(70, scale * 3);
        int leftX = centerX - halfWidth;
        int rightX = centerX + halfWidth;
        float lookX = leftSide ? centerX - 40.0F : centerX + 40.0F;
        float lookY = bottomY - 30.0F;

        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, leftX, topY, rightX, bottomY, scale, 0, lookX, lookY, entity);
        } catch (final IllegalArgumentException exception) {
            if (entity instanceof DragonEntity dragon && dragon.getPlayer() instanceof LivingEntity fallbackEntity) {
                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, leftX, topY, rightX, bottomY, scale, 0, lookX, lookY, fallbackEntity);
            }
        }
    }

    private void initializeHandler(final DragonStateHandler handler) {
        if (handler.species() == null) {
            return;
        }

        handler.setGrowth(null, handler.species().value().getStartingStage(null).value().growthRange().max() - Shapes.EPSILON);

        if (handler.body() == null) {
            handler.setBody(null, DragonBody.getRandom(null, handler.species()));
        }

        handler.setRandomValidStage(null);
        handler.getCurrentStageCustomization().defaultSkin = true;
    }

    @Override
    public void renderBackground(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, -1072689136, -804253680);
        renderBorders(graphics, BACKGROUND_TEXTURE, 0, width, 25, height - 25, width, height);
    }

    public static void renderBorders(@NotNull final GuiGraphics graphics, final Identifier texture, final int x0, final int x1, final int y0, final int y1, final int width, final int height) {
        graphics.blit(texture, x0, 0, 0, 0, width, y0, 32, 32);
        graphics.blit(texture, x0, y1, 0, y1 % 32, width, height - y1, 32, 32);
        graphics.fillGradient(x0, y0, x1, y0 + 4, 0xFF000000, 0x00000000);
        graphics.fillGradient(x0, y1 - 4, x1, y1, 0x00000000, 0xFF000000);
    }

    @Override
    protected void init() {
        super.init();

        if (!hasInit) {
            hasInit = true;
        }

        int guiTop = (height - 190) / 2;
        int xPos = width / 2 - 104;

        HoverButton helpButton = new HoverButton(width / 2 - 29, 31, 65, 18, 65, 18, INFO_MAIN, INFO_HOVER, button -> { });
        helpButton.setTooltip(Tooltip.create(Component.translatable(HELP)));
        addRenderableWidget(helpButton);

        List<AltarTypeButton> altarButtons = entries.stream().map(species -> new AltarTypeButton(this, species, 0, 0)).collect(Collectors.toList());
        humanButton = new AltarTypeButton(this, null, 0, 0) {
            boolean toggled;

            @Override
            public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partial) {
                super.renderWidget(graphics, mouseX, mouseY, partial);
                if (toggled && (!visible || !confirmation)) {
                    toggled = false;
                    Screen screen = Minecraft.getInstance().screen;
                    Objects.requireNonNull(screen).children().removeIf(child -> child == confirmComponent);
                    screen.renderables.removeIf(renderable -> renderable == renderButton);
                }
            }

            @Override
            public void onPress(@NotNull final InputWithModifiers inputWithModifiers) {
                DragonStateHandler handler = DragonStateProvider.getData(minecraft.player);
                boolean dragonDataIsPreserved = ServerConfig.saveAllAbilities && ServerConfig.saveGrowthStage;
                if (handler.isDragon() && !dragonDataIsPreserved) {
                    confirmation = true;
                }

                if (confirmation) {
                    if (!toggled) {
                        renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), button -> { }) {
                            @Override
                            public void renderWidget(@NotNull final GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
                                if (confirmComponent != null && confirmation) {
                                    confirmComponent.render(graphics, pMouseX, pMouseY, pPartialTick);
                                }

                                super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
                            }
                        };
                        ((ScreenAccessor) DragonAltarScreen.this).dragonSurvival$children().add(confirmComponent);
                        renderables.add(renderButton);
                        confirmation = true;
                    } else {
                        confirmation = false;
                    }
                } else {
                    initiateDragonForm(null);
                }

                toggled = !toggled;
            }
        };

        if (!ServerConfig.noHumansAllowed) {
            altarButtons.add(humanButton);
        }

        scrollableComponents.add(new BarComponent(this,
                xPos, guiTop + 30, 4,
                altarButtons, 5,
                -13, 215, 60, 12, 19,
                ALTAR_ARROW_LEFT_HOVER, ALTAR_ARROW_LEFT_MAIN, ALTAR_ARROW_RIGHT_HOVER, ALTAR_ARROW_RIGHT_MAIN));

        DragonStateHandler handler = DragonStateProvider.getData(minecraft.player);

        if (handler.isDragon()) {
            ResourceKey<DragonSpecies> species = handler.speciesKey();
            addRenderableWidget(new ExtendedButton(xPos + 32, height - 25, 150, 20, Component.translatable(LangKey.GUI_DRAGON_EDITOR), action -> ClientProxy.openDragonEditor(species, true)));
        }

        confirmComponent = new DragonEditorConfirmComponent(this, width / 2 - 130 / 2, height / 2 - 181 / 2, 130, 154);
        confirmation = false;
    }

    @Override
    public void confirm() {
        humanButton.initiateDragonForm(null);
    }

    @Override
    public void cancel() {
        confirmation = false;
    }
}
