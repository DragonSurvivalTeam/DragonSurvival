package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ArrowButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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

    @Translation(comments = "This author does not have a skin for this stage.")
    private static final String NO_SKIN = Translation.Type.GUI.wrap("skin_screen.no_skin");

    @Translation(comments = "You do not have a skin for this stage.")
    private static final String NO_SKIN_SELF = Translation.Type.GUI.wrap("skin_screen.no_skin_self");

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

    @Translation(comments = "Your dragon")
    private static final String SELF = Translation.Type.GUI.wrap("skin_screen.self");

    @Translation(comments = "■ This is what §6you§r look like now.")
    private static final String SELF_INFO = Translation.Type.GUI.wrap("skin_screen.self_info");

    @Translation(comments = "Random")
    private static final String RANDOM = Translation.Type.GUI.wrap("skin_screen.random");

    @Translation(comments = "■ Shows a randomly selected §6other player§r§f who uploaded a skin. You §ccan't use§r§f their appearance for yourself!§7 Only look and admire! >:D")
    private static final String RANDOM_INFO = Translation.Type.GUI.wrap("skin_screen.random_info");

    @Translation(comments = "Show custom player skins")
    private static final String SHOW_OTHER_CUSTOM_SKINS = Translation.Type.GUI.wrap("skin_screen.show_other_custom_skins");

    @Translation(comments = "Show your custom skin")
    private static final String SHOW_CUSTOM_SKIN = Translation.Type.GUI.wrap("skin_screen.show_custom_skin");

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin_interface.png");
    private static final ResourceLocation DISCORD_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/discord_button.png");
    private static final ResourceLocation WIKI_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/wiki_button.png");
    private static final ResourceLocation UNCHECKED = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/unchecked.png");
    private static final ResourceLocation CHECKED = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/dragon_claws_checked.png");

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

            handler.setStage(null, dragonStage);

            if (handler.body() == null) {
                handler.setBody(null, playerData.body());
            }

            if (handler.stage() == null) {
                Holder<DragonStage> dragonStage = minecraft.player.registryAccess().holderOrThrow(DragonStages.newborn);
                handler.setStage(null, dragonStage);
            }

            handler.initializeSkinDataToDefaults();

            if (noSkin && Objects.equals(playerName, minecraft.player.getGameProfile().getName())) {
                handler.getCurrentSkinPreset().deserializeNBT(minecraft.player.registryAccess(), playerData.getCurrentSkinPreset().serializeNBT(minecraft.player.registryAccess()));
            } else {
                handler.getCurrentStageCustomization().defaultSkin = true;
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
        guiGraphics.drawCenteredString(minecraft.font, Component.translatable(TOGGLE), startX + 128 + imageWidth / 2, startY + 30, -1);
        drawNonShadowString(guiGraphics, minecraft.font, Component.empty().append(playerName + " - ").append(DragonStage.translatableName(Objects.requireNonNull(dragonStage.getKey()))).withStyle(ChatFormatting.GRAY), startX + 15, startY - 15, -1);

        if (!loading && noSkin) {
            if (playerName.equals(minecraft.player.getGameProfile().getName())) {
                drawNonShadowLineBreak(guiGraphics, minecraft.font, Component.translatable(NO_SKIN_SELF).withStyle(ChatFormatting.WHITE), startX + 40, startY + imageHeight - 20, -1);
            } else {
                drawNonShadowLineBreak(guiGraphics, minecraft.font, Component.translatable(NO_SKIN).withStyle(ChatFormatting.WHITE), startX + 65, startY + imageHeight - 20, -1);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override // We override this to not blur the background
    public void renderBackground(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Don't render the vanilla background, it darkens the UI in an undesirable way
    }

    public static void drawNonShadowString(@NotNull final GuiGraphics guiGraphics, final Font font, final Component component, int x, int y, int color) {
        guiGraphics.drawString(font, Language.getInstance().getVisualOrder(component), x - font.width(component) / 2, y, color, false);
    }

    public static void drawNonShadowLineBreak(@NotNull final GuiGraphics guiGraphics, final Font font, final Component component, int x, int y, int color) {
        List<FormattedText> wrappedLine = font.getSplitter().splitLines(component, 150, Style.EMPTY);

        for (int i = 0; i < wrappedLine.size(); i++) {
            FormattedText properties = wrappedLine.get(i);
            guiGraphics.drawString(font, Language.getInstance().getVisualOrder(properties), x - font.width(component.getVisualOrderText()) / 2, y + i * 9, color, false);
        }
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

        setTextures();

        TabButton.addTabButtonsToScreen(this, startX + 138, startY - 26, TabButton.Type.SKINS_TAB);

        addDragonBodyWidgets();

        // Button to enable / disable the rendering of the custom dragon skin
        addRenderableWidget(new Button(startX + 128, startY + 45 + 23, imageWidth, 20, Component.translatable(SHOW_CUSTOM_SKIN), button -> {
            DragonStateHandler handler = DragonStateProvider.getData(Objects.requireNonNull(player));
            handler.getSkinData().renderCustomSkin = !handler.getSkinData().renderCustomSkin;
            ConfigHandler.updateConfigValue("render_custom_skin", handler.getSkinData().renderCustomSkin);
            PacketDistributor.sendToServer(new SyncDragonSkinSettings(player.getId(), handler.getSkinData().renderCustomSkin));
            setTextures();
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.blit(DragonStateProvider.getData(Objects.requireNonNull(player)).getSkinData().renderCustomSkin ? CHECKED : UNCHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13);
            }
        });

        // Button to enable / disable the rendering of custom dragon skin of other players
        addRenderableWidget(new Button(startX + 128, startY + 45 + 48, imageWidth, 20, Component.translatable(SHOW_OTHER_CUSTOM_SKINS), button -> {
            ClientDragonRenderer.renderOtherPlayerSkins = !ClientDragonRenderer.renderOtherPlayerSkins;
            ConfigHandler.updateConfigValue("render_other_players_custom_skins", ClientDragonRenderer.renderOtherPlayerSkins);
            setTextures();
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.blit(ClientDragonRenderer.renderOtherPlayerSkins ? CHECKED : UNCHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13);
            }
        });

        Button discordURLButton = new Button(startX + 128 + imageWidth / 2 - 8 - 25, startY + 128 + 30, 16, 16, Component.empty(), button -> {
            try {
                clickedLink = new URI(DISCORD_URL);
                minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, DISCORD_URL, false));
            } catch (URISyntaxException exception) {
                DragonSurvival.LOGGER.error(exception);
            }
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(DISCORD_TEXTURE, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }
        };
        discordURLButton.setTooltip(Tooltip.create(Component.translatable(DISCORD)));
        addRenderableWidget(discordURLButton);

        Button wikiButton = new Button(startX + 128 + imageWidth / 2 - 8 + 25, startY + 128 + 30, 16, 16, Component.empty(), button -> {
            try {
                clickedLink = new URI(WIKI_URL);
                minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, WIKI_URL, false));
            } catch (URISyntaxException exception) {
                DragonSurvival.LOGGER.error(exception);
            }
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(WIKI_TEXTURE, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }
        };
        wikiButton.setTooltip(Tooltip.create(Component.translatable(WIKI)));
        addRenderableWidget(wikiButton);

        addRenderableWidget(new HelpButton(startX + 128 + imageWidth / 2 - 8, startY + 128 + 30, 16, 16, HELP));

        addRenderableWidget(Button.builder(Component.translatable(SELF), button -> {
            playerName = Objects.requireNonNull(player).getGameProfile().getName();
            setTextures();
        }).bounds(startX - 60, startY + 128, 90, 20).tooltip(Tooltip.create(Component.translatable(SELF_INFO))).build());

        addRenderableWidget(Button.builder(Component.translatable(RANDOM), button -> {
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
        }).bounds(startX + 35, startY + 128, 60, 20).tooltip(Tooltip.create(Component.translatable(RANDOM_INFO))).build());

        addRenderableWidget(new ArrowButton(ArrowButton.Type.PREVIOUS, startX - 70, startY - 20, 11, 17, button -> {
            ResourceKey<DragonStage> nextLevel = dragonStage.getKey();

            if (dragonStage.is(DragonStages.adult)) {
                nextLevel = DragonStages.young;
            } else if (dragonStage.is(DragonStages.young)) {
                nextLevel = DragonStages.newborn;
            } else if (dragonStage.is(DragonStages.newborn)) {
                nextLevel = DragonStages.adult;
            }

            dragonStage = Objects.requireNonNull(player).registryAccess().holderOrThrow(Objects.requireNonNull(nextLevel));

            setTextures();
        }));

        addRenderableWidget(new ArrowButton(ArrowButton.Type.NEXT, startX + 90, startY - 20, 11, 17, button -> {
            ResourceKey<DragonStage> nextLevel = dragonStage.getKey();

            if (dragonStage.is(DragonStages.newborn)) {
                nextLevel = DragonStages.young;
            } else if (dragonStage.is(DragonStages.young)) {
                nextLevel = DragonStages.adult;
            } else if (dragonStage.is(DragonStages.adult)) {
                nextLevel = DragonStages.newborn;
            }

            dragonStage = Objects.requireNonNull(player).registryAccess().holderOrThrow(Objects.requireNonNull(nextLevel));

            setTextures();
        }));
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
        return 210;
    }

    @Override
    public int getDragonBodyButtonYOffset() {
        return 90;
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