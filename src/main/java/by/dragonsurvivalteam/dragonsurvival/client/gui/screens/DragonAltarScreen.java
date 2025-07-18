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
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
            "§6■ Welcome to Dragon Survival!§r",
            "■ You can choose which dragon §6species§r§r you want to become. This decision is not permanent, but you may lose progress if you change your mind.",
            "■§7 Don't forget to read patch notes and delete old configs if you update our mod to avoid bugs! Enjoy the game! :3"
    })
    private static final String HELP = Translation.Type.GUI.wrap("altar.help");

    @Translation(comments = "Dragon Survival")
    private static final String TITLE = Translation.Type.GUI.wrap("altar.title");

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png");

    private final DragonStateHandler handler1 = new DragonStateHandler();
    private final DragonStateHandler handler2 = new DragonStateHandler();
    private final String[] animations = {"sit", "idle", "fly", "swim", "run", "dig", "vibing_sitting", "shy_sitting", "rocking_on_back"};
    private final List<ScrollableComponent> scrollableComponents = new ArrayList<>();

    private static final ResourceLocation ALTAR_ARROW_LEFT_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/altar/arrow_left_hover.png");
    private static final ResourceLocation ALTAR_ARROW_LEFT_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/altar/arrow_left_main.png");
    private static final ResourceLocation ALTAR_ARROW_RIGHT_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/altar/arrow_right_hover.png");
    private static final ResourceLocation ALTAR_ARROW_RIGHT_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/altar/arrow_right_main.png");

    private static final ResourceLocation INFO_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/altar/info_hover.png");
    private static final ResourceLocation INFO_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/altar/info_main.png");

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

        //noinspection DataFlowIssue -> 'minecraft' (from 'Screen') is null at this point because it gets set in 'init'
        Minecraft.getInstance().player.registryAccess().registryOrThrow(DragonSpecies.REGISTRY).getTag(DSDragonSpeciesTags.ORDER).ifPresent(order -> {
            //noinspection unchecked -> cast is valid
            List<Holder<DragonSpecies>> list = ((HolderSet$NamedAccess<DragonSpecies>) order).dragonSurvival$contents();

            Comparator<UnlockableBehavior.SpeciesEntry> comparator = Comparator.comparingInt(entry -> {
                int index = list.indexOf(entry.species());
                // Sort entries that are not present to the end
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
        //noinspection DataFlowIssue -> player should not be null
        AltarData data = AltarData.getData(player);
        data.isInAltar = false; // TODO :: should maybe also be sent to the server

        if (!data.hasUsedAltar) {
            // In case the altar was closed without making a choice
            // But the player is already somehow a dragon
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (confirmComponent != null && confirmation) {
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

        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(guieventlistener);
                if (button == 0) {
                    this.setDragging(true);
                }

                if (confirmComponent != null && confirmation) {
                    for (GuiEventListener guieventlistener2 : this.children()) {
                        if (guieventlistener2 instanceof HoverDisableable hoverDisableable) {
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
        if (minecraft == null) {
            return;
        }

        renderBackground(graphics, mouseX, mouseY, partialTick);

        tick++;

        if (tick % 200 * 20 == 0) {
            animation1++;
            animation2++;

            // FIXME :: for some reason at this point the species may not be set
            if (handler1.species() != null && handler2.species() != null) {
                if (handler1.body() == null) {
                    handler1.setBody(null, DragonBody.getRandom(null, handler1.species()));
                }

                handler2.setBody(null, handler1.body());
                handler1.setBody(null, DragonBody.getRandom(null, handler1.species()));

                if (animation1 >= animations.length) {
                    animation1 = 0;
                }

                if (animation2 >= animations.length) {
                    animation2 = 0;
                }
            }
        }

        if (!confirmation) {
            children().removeIf(s -> s == confirmComponent);
            renderables.removeIf(s -> s == renderButton);
        }

        for (Renderable btn : renderables) {
            if (btn instanceof AltarTypeButton button) {
                if (button.isHovered()) {
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

                    // Without clamping the value it may run into 'IndexOutOfBoundsException' for some reason
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

                    // Left side
                    Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
                    quaternion.rotateY((float) Math.toRadians(210));
                    InventoryScreen.renderEntityInInventory(graphics, (width / 2f) - 180, button.getY() + button.getHeight(), entity1Scale, new Vector3f(), quaternion, null, entity1);

                    // Right side
                    Quaternionf quaternion2 = Axis.ZP.rotationDegrees(180.0F);
                    quaternion2.rotateY((float) Math.toRadians(150));
                    InventoryScreen.renderEntityInInventory(graphics, (width / 2f) + 180, button.getY() + button.getHeight(), entity2Scale, new Vector3f(), quaternion2, null, entity2);
                }
            }

            if (!confirmation) {
                if (btn instanceof HoverDisableable hoverDisableable) {
                    hoverDisableable.enableHover();
                }
            }
        }

        TextRenderUtil.drawCenteredScaledText(graphics, width / 2 + 7, 10, 2f, Component.translatable(TITLE).getString(), DyeColor.WHITE.getTextColor());
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.pose().popPose();
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
    public void renderBackground(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -300, -1072689136, -804253680);
        renderBorders(guiGraphics, BACKGROUND_TEXTURE, 0, width, 25, height - 25, width, height);
    }

    public static void renderBorders(@NotNull final GuiGraphics guiGraphics, ResourceLocation texture, int x0, int x1, int y0, int y1, int width, int height) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, texture);
        float zLevel = 0;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        bufferbuilder.addVertex(x0, y0, zLevel).setUv(0.0F, (float) y0 / 32.0F).setColor(64, 64, 64, 55);
        bufferbuilder.addVertex(x0 + width, y0, zLevel).setUv((float) width / 32.0F, (float) y0 / 32.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0 + width, 0.0F, zLevel).setUv((float) width / 32.0F, 0.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0, 0.0F, zLevel).setUv(0.0F, 0.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0, height, zLevel).setUv(0.0F, (float) height / 32.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0 + width, height, zLevel).setUv((float) width / 32.0F, (float) height / 32.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0 + width, y1, zLevel).setUv((float) width / 32.0F, (float) y1 / 32.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0, y1, zLevel).setUv(0.0F, (float) y1 / 32.0F).setColor(64, 64, 64, 255);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex(x0, y0 + 4, zLevel).setUv(0.0F, 1.0F).setColor(0, 0, 0, 0);
        bufferbuilder.addVertex(x1, y0 + 4, zLevel).setUv(1.0F, 1.0F).setColor(0, 0, 0, 0);
        bufferbuilder.addVertex(x1, y0, zLevel).setUv(1.0F, 0.0F).setColor(0, 0, 0, 255);
        bufferbuilder.addVertex(x0, y0, zLevel).setUv(0.0F, 0.0F).setColor(0, 0, 0, 255);
        bufferbuilder.addVertex(x0, y1, zLevel).setUv(0.0F, 1.0F).setColor(0, 0, 0, 255);
        bufferbuilder.addVertex(x1, y1, zLevel).setUv(1.0F, 1.0F).setColor(0, 0, 0, 255);
        bufferbuilder.addVertex(x1, y1 - 4, zLevel).setUv(1.0F, 0.0F).setColor(0, 0, 0, 0);
        bufferbuilder.addVertex(x0, y1 - 4, zLevel).setUv(0.0F, 0.0F).setColor(0, 0, 0, 0);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    @Override
    protected void init() {
        super.init();

        if (!hasInit) {
            hasInit = true;
        }

        int guiTop = (height - 190) / 2;
        int xPos = width / 2 - 104;

        HoverButton helpButton = new HoverButton(width / 2 - 29, 31, 65, 18, 65, 18, INFO_MAIN, INFO_HOVER, button -> { /* Nothing to do */ });
        helpButton.setTooltip(Tooltip.create(Component.translatable(HELP)));
        addRenderableWidget(helpButton);

        List<AltarTypeButton> altarButtons = entries.stream().map(species -> new AltarTypeButton(this, species, 0, 0)).collect(Collectors.toList());
        humanButton = new AltarTypeButton(this, null, 0, 0) {
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
                //noinspection DataFlowIssue -> player is present
                DragonStateHandler handler = DragonStateProvider.getData(minecraft.player);
                boolean dragonDataIsPreserved = ServerConfig.saveAllAbilities && ServerConfig.saveGrowthStage;
                if (handler.isDragon() && !dragonDataIsPreserved) {
                    confirmation = true;
                }

                if (confirmation) {
                    if (!toggled) {
                        renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), button -> { /* Nothing to do */ }) {
                            @Override
                            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                                if (confirmComponent != null && confirmation) {
                                    confirmComponent.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
                                }

                                super.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTick);
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

        // TODO :: display message if no unlocked are present
        scrollableComponents.add(new BarComponent(this,
                xPos, guiTop + 30, 4,
                altarButtons, 5,
                -13, 215, 60, 12, 19,
                ALTAR_ARROW_LEFT_HOVER, ALTAR_ARROW_LEFT_MAIN, ALTAR_ARROW_RIGHT_HOVER, ALTAR_ARROW_RIGHT_MAIN));

        //noinspection DataFlowIssue -> player is present
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