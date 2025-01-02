package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.client.skins.SkinObject;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.ibm.icu.impl.Pair;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Supplier;

public class DragonSkinsScreen extends Screen implements DragonBodyScreen {
    @Translation(comments = "Skin Settings")
    private static final String SETTINGS = Translation.Type.GUI.wrap("skin_screen.settings");

    @Translation(comments = "Enable / disable skins")
    private static final String TOGGLE = Translation.Type.GUI.wrap("skin_screen.toggle");

    @Translation(comments = "■ Join our §6discord server§r!§7 Read the Rules, FAQ and Wiki before you ask anything.")
    private static final String DISCORD = Translation.Type.GUI.wrap("skin_screen.discord");

    @Translation(comments = "■ This is a link to our §6Wiki§r dedicated to making your own skin!§7 Remember that this will be very difficult, and requires knowledge of graphic editors.")
    private static final String WIKI = Translation.Type.GUI.wrap("skin_screen.wiki");

    @Translation(comments = {
            "■ A §6Skin§r is a self-made texture for your dragon. In this tab, you can disable the display of your skin for your dragon at different stages, as well as skins of other players.",
            "■ Skins are not built in the Dragon Editor, but created using outside tools.",
            "§7■ If you are interested in how to make your own skin or take a commission, use the buttons on the right."
    })
    private static final String HELP = Translation.Type.GUI.wrap("skin_screen.help");

    @Translation(comments = "■ Shows a randomly selected §6other player§r§f who uploaded a skin. You §ccan't use§r§f their appearance for yourself!§7 Only look and admire! >:D")
    private static final String RANDOM_INFO = Translation.Type.GUI.wrap("skin_screen.random_info");

    @Translation(comments = "If you are using a §6texture pack§r to test your custom skin before submitting it, check this box.")
    private static final String DEFAULT_SKIN_INFO = Translation.Type.GUI.wrap("dragon_editor.default_skin_info");

    @Translation(comments = "Show custom player skins")
    private static final String SHOW_OTHER_CUSTOM_SKINS = Translation.Type.GUI.wrap("skin_screen.show_other_custom_skins");

    @Translation(comments = "Show your custom skin")
    private static final String SHOW_CUSTOM_SKIN = Translation.Type.GUI.wrap("skin_screen.show_custom_skin");

    @Translation(comments = "Open Editor")
    private static final String OPEN_EDITOR = Translation.Type.GUI.wrap("skin_screen.open_editor");

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin_interface.png");
    private static final ResourceLocation DISCORD_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/discord_button.png");
    private static final ResourceLocation WIKI_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/wiki_button.png");
    private static final ResourceLocation UNCHECKED = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/unchecked.png");
    private static final ResourceLocation CHECKED = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/dragon_claws_checked.png");

    private static final ResourceLocation BUTTON_BACKGROUND_BLACK = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/background_black.png");
    private static final ResourceLocation BUTTON_BACKGROUND_WHITE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/background_white.png");

    private static final ResourceLocation STAGE_BACKGROUND = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/stage_background.png");

    private static final ResourceLocation SKIN_ON = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/skin_on.png");
    private static final ResourceLocation SKIN_OFF = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/skin_off.png");

    private static final ResourceLocation RANDOM_SKIN_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/random_skin_main.png");
    private static final ResourceLocation RANDOM_SKIN_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/random_skin_hover.png");

    private static final ResourceLocation RESET_SKIN_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/reset_skin_hover.png");
    private static final ResourceLocation RESET_SKIN_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/reset_skin_main.png");

    private static final ResourceLocation STAGE_ARROW_LEFT_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/stage_arrow_left_main.png");
    private static final ResourceLocation STAGE_ARROW_LEFT_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/stage_arrow_left_hover.png");

    private static final ResourceLocation STAGE_ARROW_RIGHT_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/stage_arrow_right_main.png");
    private static final ResourceLocation STAGE_ARROW_RIGHT_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/stage_arrow_right_hover.png");

    private static final ResourceLocation OPEN_EDITOR_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/open_editor_main.png");
    private static final ResourceLocation OPEN_EDITOR_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/open_editor_hover.png");

    private static final ResourceLocation OLD_TEXTURE_ON = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/texturepack_on.png");
    private static final ResourceLocation OLD_TEXTURE_OFF = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/texturepack_off.png");

    private static final ResourceLocation INFO_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/info_hover.png");
    private static final ResourceLocation INFO_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/info_main.png");

    private static final ResourceLocation WIKI_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/wiki_main.png");
    private static final ResourceLocation WIKI_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/wiki_hover.png");

    private static final ResourceLocation DISCORD_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/discord_main.png");
    private static final ResourceLocation DISCORD_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin/discord_hover.png");

    private static final String DISCORD_URL = "https://discord.gg/8SsB8ar";
    private static final String WIKI_URL = "https://github.com/DragonSurvivalTeam/DragonSurvival/wiki/3.-Customization";
    private static final ArrayList<String> SEEN_SKINS = new ArrayList<>();

    private static Holder<DragonStage> dragonStage;
    private static ResourceLocation skinTexture;
    private static ResourceLocation glowTexture;
    private static String playerName;
    private static String lastPlayerName;
    private static boolean noSkin;
    private static boolean loading;

    public final DragonStateHandler handler = new DragonStateHandler();
    public Screen sourceScreen;

    /**
     * Widgets which belong to the dragon body logic <br>
     * (they are stored to properly reference (and remove) them when using the arrow buttons to navigate through the bodies)
     */
    private final List<AbstractWidget> dragonBodyWidgets = new ArrayList<>();
    private int dragonBodySelectionOffset;

    private HoverButton playerNameDisplay;
    private HoverButton playerStageDisplay;

    private final int imageWidth = 164;
    private final int imageHeight = 128;
    private int guiLeft;
    private int guiTop;
    private float yRot = -3;
    private float xRot = -5;
    private float zoom;
    private URI clickedLink;

    public DragonSkinsScreen(Screen sourceScreen) {
        super(Component.empty());
        this.sourceScreen = sourceScreen;

        LocalPlayer localPlayer = sourceScreen.getMinecraft().player;
        if (dragonStage == null) {
            //noinspection DataFlowIssue -> player should not be null
            dragonStage = DragonStage.get(localPlayer.registryAccess(), Double.MAX_VALUE);
        }
    }

    @Override
    public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        this.renderBlurredBackground(partialTick);

        int startX = guiLeft;
        int startY = guiTop;

        final GeoBone neckandHead = ClientDragonRenderer.dragonModel.getAnimationProcessor().getBone("Neck");

        if (neckandHead != null) {
            neckandHead.setHidden(false);
        }

        DragonEntity dragon = FakeClientPlayerUtils.getFakeDragon(0, handler);
        EntityRenderer<? super DragonEntity> dragonRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon);

        if (noSkin && Objects.equals(playerName, minecraft.player.getGameProfile().getName())) {
            ClientDragonRenderer.dragonModel.setOverrideTexture(null);
            ((DragonRenderer) dragonRenderer).glowTexture = null;
        } else {
            ClientDragonRenderer.dragonModel.setOverrideTexture(skinTexture);
            ((DragonRenderer) dragonRenderer).glowTexture = glowTexture;
        }

        float scale = zoom;

        if (!loading) {
            DragonStateHandler playerData = DragonStateProvider.getData(minecraft.player);

            if (!DragonUtils.isType(handler.species(), playerData.species())) {
                handler.setType(null, playerData.species());
            }

            if (handler.stage() == null) {
                Holder<DragonStage> dragonStage = minecraft.player.registryAccess().holderOrThrow(DragonStages.newborn);
                boolean alreadyUsingDefaults = handler.getCurrentSkinPreset().isStageUsingDefaultSkin(dragonStage.getKey());
                handler.setStage(null, dragonStage);
                handler.setCurrentStageCustomization(DragonStateProvider.getData(minecraft.player).getCustomizationForStage(dragonStage.getKey()));
                handler.getCurrentSkinPreset().setAllStagesToUseDefaultSkin(alreadyUsingDefaults);
                DragonSkinsScreen.dragonStage = handler.stage();
            }

            FakeClientPlayerUtils.getFakePlayer(0, handler).animationSupplier = () -> "fly_animation_magic";

            Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
            quaternion.mul(Axis.XP.rotationDegrees(yRot * 10.0F));
            quaternion.rotateY((float) Math.toRadians(180 - xRot * 10));
            InventoryScreen.renderEntityInInventory(guiGraphics, startX + 15, startY + 70, (int) scale, new Vector3f(0, 0, 100), quaternion, null, dragon);
        }

        ((DragonRenderer) dragonRenderer).glowTexture = null;

        guiGraphics.blit(BACKGROUND_TEXTURE, startX + 128, startY, 0, 0, 164, 256);
        drawNonShadowString(guiGraphics, minecraft.font, Component.translatable(SETTINGS).withStyle(ChatFormatting.BLACK), startX + 128 + imageWidth / 2, startY + 7, -1);
        playerNameDisplay.setMessage(Component.literal(playerName));
        playerStageDisplay.setMessage(DragonStage.translatableName(dragonStage.getKey()));

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override // We override this to not blur the background
    public void renderBackground(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Don't render the vanilla background, it darkens the UI in an undesirable way
    }

    public static void drawNonShadowString(@NotNull final GuiGraphics guiGraphics, final Font font, final Component component, int x, int y, int color) {
        guiGraphics.drawString(font, Language.getInstance().getVisualOrder(component), x - font.width(component) / 2, y, color, false);
    }

    @Override
    public void init() {
        super.init();

        Minecraft minecraft = getMinecraft();
        LocalPlayer player = minecraft.player;

        guiLeft = (width - 256) / 2;
        guiTop = (height - 128) / 2;

        int startX = guiLeft;
        int startY = guiTop;

        if (playerName == null) {
            playerName = Objects.requireNonNull(player).getGameProfile().getName();
        }

        handler.deserializeNBT(minecraft.player.registryAccess(), DragonStateProvider.getData(minecraft.player).serializeNBT(minecraft.player.registryAccess()));
        handler.setStage(null, dragonStage);
        handler.setCurrentStageCustomization(DragonStateProvider.getData(player).getCustomizationForStage(dragonStage.getKey()));
        handler.getCurrentSkinPreset().setAllStagesToUseDefaultSkin(false);
        setTextures();

        TabButton.addTabButtonsToScreen(this, startX + 138, startY - 26, TabButton.Type.SKINS_TAB);

        addDragonBodyWidgets();

        playerNameDisplay = new HoverButton(startX - 62 , startY - 50 , 165, 22, 165, 22, BUTTON_BACKGROUND_WHITE, BUTTON_BACKGROUND_WHITE, button -> {});
        playerNameDisplay.setMessage(Component.literal(Objects.requireNonNull(player).getGameProfile().getName()));
        addRenderableOnly(playerNameDisplay);

        playerStageDisplay = new HoverButton(startX - 55, startY + 150, 149, 22, 149, 22, STAGE_BACKGROUND, STAGE_BACKGROUND, button -> {});
        playerStageDisplay.setMessage(DragonStage.translatableName(dragonStage.getKey()));
        addRenderableOnly(playerStageDisplay);

        HoverButton leftArrowButton = new HoverButton(startX - 62, startY + 153, 9, 16, 18, 18, STAGE_ARROW_LEFT_MAIN, STAGE_ARROW_LEFT_HOVER, button -> {
            ResourceKey<DragonStage> nextLevel = dragonStage.getKey();

            if (dragonStage.is(DragonStages.adult)) {
                nextLevel = DragonStages.young;
            } else if (dragonStage.is(DragonStages.young)) {
                nextLevel = DragonStages.newborn;
            } else if (dragonStage.is(DragonStages.newborn)) {
                nextLevel = DragonStages.adult;
            }

            boolean alreadyUsingDefaults = handler.getCurrentSkinPreset().isStageUsingDefaultSkin(dragonStage.getKey());
            dragonStage = Objects.requireNonNull(player).registryAccess().holderOrThrow(Objects.requireNonNull(nextLevel));
            handler.setStage(null, dragonStage);
            handler.setCurrentStageCustomization(DragonStateProvider.getData(player).getCustomizationForStage(dragonStage.getKey()));
            handler.getCurrentSkinPreset().setAllStagesToUseDefaultSkin(alreadyUsingDefaults);

            setTextures();
        });
        addRenderableWidget(leftArrowButton);

        HoverButton rightArrowButton = new HoverButton(startX + 92, startY + 153, 9, 16, 18, 18, STAGE_ARROW_RIGHT_MAIN, STAGE_ARROW_RIGHT_HOVER, button -> {
            ResourceKey<DragonStage> nextLevel = dragonStage.getKey();

            if (dragonStage.is(DragonStages.newborn)) {
                nextLevel = DragonStages.young;
            } else if (dragonStage.is(DragonStages.young)) {
                nextLevel = DragonStages.adult;
            } else if (dragonStage.is(DragonStages.adult)) {
                nextLevel = DragonStages.newborn;
            }

            boolean alreadyUsingDefaults = handler.getCurrentSkinPreset().isStageUsingDefaultSkin(dragonStage.getKey());
            dragonStage = Objects.requireNonNull(player).registryAccess().holderOrThrow(Objects.requireNonNull(nextLevel));
            handler.setStage(null, dragonStage);
            handler.setCurrentStageCustomization(DragonStateProvider.getData(player).getCustomizationForStage(dragonStage.getKey()));
            handler.getCurrentSkinPreset().setAllStagesToUseDefaultSkin(alreadyUsingDefaults);

            setTextures();
        });
        addRenderableWidget(rightArrowButton);

        // Button to enable / disable the rendering of the custom dragon skin
        HoverButton toggleRenderingOwnSkin = new HoverButton(startX + 128, startY + 26, 165, 22, 165, 22, BUTTON_BACKGROUND_BLACK, BUTTON_BACKGROUND_BLACK, button -> {
            DragonStateHandler handler = DragonStateProvider.getData(Objects.requireNonNull(player));
            handler.getSkinData().renderCustomSkin = !handler.getSkinData().renderCustomSkin;
            ConfigHandler.updateConfigValue("render_custom_skin", handler.getSkinData().renderCustomSkin);
            PacketDistributor.sendToServer(new SyncDragonSkinSettings(player.getId(), handler.getSkinData().renderCustomSkin));
            setTextures();
        }){
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.blit(DragonStateProvider.getData(Objects.requireNonNull(player)).getSkinData().renderCustomSkin ? SKIN_ON : SKIN_OFF, getX() + 3, getY() + 5, 0, 0, 14, 14, 14, 14);
            }
        };
        toggleRenderingOwnSkin.setMessage(Component.translatable(SHOW_CUSTOM_SKIN));
        addRenderableWidget(toggleRenderingOwnSkin);


        // Button to enable / disable the rendering of custom dragon skin of other players
        HoverButton toggleRenderingOtherSkins = new HoverButton(startX + 128, startY + 26 + 26, 165, 22, 165, 22, BUTTON_BACKGROUND_BLACK, BUTTON_BACKGROUND_BLACK, button -> {
            ClientDragonRenderer.renderOtherPlayerSkins = !ClientDragonRenderer.renderOtherPlayerSkins;
            ConfigHandler.updateConfigValue("render_other_players_custom_skins", ClientDragonRenderer.renderOtherPlayerSkins);
            setTextures();
        }){
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.blit(ClientDragonRenderer.renderOtherPlayerSkins ? SKIN_ON : SKIN_OFF, getX() + 3, getY() + 5, 0, 0, 14, 14, 14, 14);
            }
        };
        toggleRenderingOtherSkins.setMessage(Component.translatable(SHOW_OTHER_CUSTOM_SKINS));
        addRenderableWidget(toggleRenderingOtherSkins);

        HoverButton resetSkinButton = new HoverButton(startX + 20, startY + 128, 22, 21, 22, 21, RESET_SKIN_MAIN, RESET_SKIN_HOVER, button -> {
            playerName = Objects.requireNonNull(player).getGameProfile().getName();
            setTextures();
        });
        addRenderableWidget(resetSkinButton);

        HoverButton randomSkinButton = new HoverButton(startX - 6, startY + 128, 22, 21, 22, 21, RANDOM_SKIN_MAIN, RANDOM_SKIN_HOVER, button -> {
            ArrayList<Pair<ResourceKey<DragonStage>, String>> skins = new ArrayList<>();
            HashSet<String> users = new HashSet<>();
            Random random = new Random();

            for (Map.Entry<ResourceKey<DragonStage>, HashMap<String, SkinObject>> ent : DragonSkins.SKIN_USERS.entrySet()) {
                for (Map.Entry<String, SkinObject> user : ent.getValue().entrySet()) {
                    if (!user.getValue().glow) {
                        skins.add(Pair.of(ent.getKey(), user.getKey()));
                        users.add(user.getKey());
                    }
                }
            }

            skins.removeIf(pair -> SEEN_SKINS.contains(pair.second));

            if (!skins.isEmpty()) {
                Pair<ResourceKey<DragonStage>, String> skin = skins.get(random.nextInt(skins.size()));

                if (skin != null) {
                    dragonStage = Objects.requireNonNull(player).registryAccess().holderOrThrow(skin.first);
                    playerName = skin.second;

                    SEEN_SKINS.add(skin.second);

                    if (SEEN_SKINS.size() >= users.size() / 2) {
                        SEEN_SKINS.removeFirst();
                    }

                    setTextures();
                }
            }
        });
        randomSkinButton.setTooltip(Tooltip.create(Component.translatable(RANDOM_INFO)));
        addRenderableWidget(randomSkinButton);

        HoverButton openEditorButton = new HoverButton(startX + 128, startY + 115, 165, 22, 165, 22, OPEN_EDITOR_MAIN, OPEN_EDITOR_HOVER, button -> {
            minecraft.setScreen(new DragonEditorScreen(this));
        });
        openEditorButton.setMessage(Component.translatable(OPEN_EDITOR));
        addRenderableWidget(openEditorButton);

        ExtendedButton oldTextureButton = new ExtendedButton(startX + 176, startY + 128 + 20, 14, 14, Component.empty(), button -> {
            handler.getCurrentSkinPreset().setAllStagesToUseDefaultSkin(!handler.getCurrentSkinPreset().isAnyStageUsingDefaultSkin());
        }, Supplier::get) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(handler.getCurrentSkinPreset().isAnyStageUsingDefaultSkin() ? OLD_TEXTURE_ON : OLD_TEXTURE_OFF, getX(), getY(), 0, 0, 14, 14, 14, 14);
            }
        };
        oldTextureButton.setTooltip(Tooltip.create(Component.translatable(DEFAULT_SKIN_INFO)));
        addRenderableWidget(oldTextureButton);

        HoverButton helpButton = new HoverButton(startX + 176 + 16, startY + 128 + 20, 14, 14, 14, 14, INFO_MAIN, INFO_HOVER, button -> {});
        helpButton.setTooltip(Tooltip.create(Component.translatable(HELP)));
        addRenderableWidget(helpButton);

        HoverButton discordButton = new HoverButton(startX + 176 + 32, startY + 128 + 20, 14, 14, 14, 14, DISCORD_MAIN, DISCORD_HOVER, button -> {
            try {
                clickedLink = new URI(DISCORD_URL);
                minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, DISCORD_URL, false));
            } catch (URISyntaxException exception) {
                DragonSurvival.LOGGER.error(exception);
            }
        });
        discordButton.setTooltip(Tooltip.create(Component.translatable(DISCORD)));
        addRenderableWidget(discordButton);

        HoverButton wikiButton = new HoverButton(startX + 176 + 48, startY + 128 + 20, 14, 14, 14, 14, WIKI_MAIN, WIKI_HOVER, button -> {
            try {
                clickedLink = new URI(WIKI_URL);
                minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, WIKI_URL, false));
            } catch (URISyntaxException exception) {
                DragonSurvival.LOGGER.error(exception);
            }
        });
        wikiButton.setTooltip(Tooltip.create(Component.translatable(WIKI)));
        addRenderableWidget(wikiButton);
    }

    @Override
    public DragonBodyButton createButton(final Holder<DragonBody> dragonBody, int x, int y) {
        return new DragonBodyButton(this, x, y, 25, 25, dragonBody, false, button -> handler.setBody(null, dragonBody));
    }

    @Override
    public List<AbstractWidget> getDragonBodyWidgets() {
        return dragonBodyWidgets;
    }

    @Override
    public int getDragonBodyButtonXOffset() {
        return -150;
    }

    @Override
    public int getDragonBodyButtonYOffset() {
        return 15;
    }

    @Override
    public void setDragonBodyButtonOffset(int dragonBodySelectionOffset) {
        this.dragonBodySelectionOffset = dragonBodySelectionOffset;
    }

    @Override
    public int getDragonBodySelectionOffset() {
        return dragonBodySelectionOffset;
    }

    public void setTextures() {
        loading = true;

        ResourceLocation skinTexture = DragonSkins.getPlayerSkin(playerName, Objects.requireNonNull(dragonStage.getKey()));
        ResourceLocation glowTexture = null;
        boolean defaultSkin = false;

        if (!DragonSkins.renderCustomSkin(Objects.requireNonNull(minecraft).player) && playerName.equals(Objects.requireNonNull(minecraft.player).getGameProfile().getName()) || skinTexture == null) {
            skinTexture = null;
            defaultSkin = true;
        }

        if (skinTexture != null) {
            glowTexture = DragonSkins.getPlayerGlow(playerName, dragonStage.getKey());
        }

        DragonSkinsScreen.glowTexture = glowTexture;
        DragonSkinsScreen.skinTexture = skinTexture;

        if (Objects.equals(lastPlayerName, playerName) || lastPlayerName == null) {
            zoom = DragonEditorScreen.setZoom(dragonStage);
        }

        noSkin = defaultSkin;
        loading = false;
        lastPlayerName = playerName;
    }

    private void confirmLink(boolean confirmed) {
        if (confirmed) {
            openLink(clickedLink);
        }

        clickedLink = null;
        minecraft.setScreen(this);
    }

    private void openLink(URI uri) {
        Util.getPlatform().openUri(uri);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseDragged(double x1, double y1, int p_231045_5_, double x2, double y2) {
        xRot -= (float) (x2 / 5);
        yRot -= (float) (y2 / 5);

        return super.mouseDragged(x1, y1, p_231045_5_, x2, y2);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        zoom += (float) scrollY;
        zoom = Mth.clamp(zoom, 10, 80);

        return true;
    }
}