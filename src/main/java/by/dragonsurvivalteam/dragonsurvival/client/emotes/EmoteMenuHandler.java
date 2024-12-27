package by.dragonsurvivalteam.dragonsurvival.client.emotes;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEmoteKeybindings;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmote;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@EventBusSubscriber(Dist.CLIENT)
public class EmoteMenuHandler {
    @Translation(type = Translation.Type.MISC, comments = " ■ §6Emotes§r ■")
    private static final String TOGGLE = Translation.Type.GUI.wrap("emotes.toggle");

    @Translation(type = Translation.Type.MISC, comments = "Keybinds")
    private static final String KEYBINDS = Translation.Type.GUI.wrap("emotes.keybinds");

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "emote_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the emote button")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "emotes"}, key = "emote_x_offset")
    public static Integer emoteXOffset = 0;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "emote_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the y position of the emote button")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "emotes"}, key = "emote_y_offset")
    public static Integer emoteYOffset = 0;

    private static final ResourceLocation EMPTY_SLOT = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/emote/empty_slot.png");
    private static final ResourceLocation PLAY_ONCE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/emote/play_once.png");
    private static final ResourceLocation PLAY_LOOPED = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/emote/play_looped.png");
    private static final ResourceLocation SOUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/emote/sound.png");
    private static final ResourceLocation NO_SOUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/emote/no_sound.png");
    private static final ResourceLocation BUTTON_UP = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/emote/button_up.png");
    private static final ResourceLocation BUTTON_DOWN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/emote/button_down.png");
    private static final ResourceLocation BUTTON_LEFT = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/emote/button_left.png");
    private static final ResourceLocation BUTTON_RIGHT = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/emote/button_right.png");
    private static final ResourceLocation RESET_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/reset_icon.png");

    private static final int PER_PAGE = 10;

    private static int emotePage = 0;
    private static boolean keybinding = false;
    private static String currentlyKeybinding = null;
    private static final List<ExtendedButton> emoteButtons = new ArrayList<>();
    private static final List<ExtendedButton> keybindingButtons = new ArrayList<>();
    private static boolean emoteMenuOpen = false;

    @SubscribeEvent
    public static void toggleEmoteButtons(ScreenEvent.Render.Pre renderGuiEvent) {
        if (renderGuiEvent.getScreen() instanceof ChatScreen && DragonStateProvider.isDragon(Minecraft.getInstance().player)) {
            for (ExtendedButton button : emoteButtons) {
                button.visible = emoteMenuOpen;
            }

            for (ExtendedButton button : keybindingButtons) {
                button.visible = emoteMenuOpen && keybinding;
            }
        }
    }

    @SubscribeEvent
    public static void addEmoteButton(ScreenEvent.Init.Post initGuiEvent) {
        Screen screen = initGuiEvent.getScreen();
        currentlyKeybinding = null;

        if (screen instanceof ChatScreen chatScreen && DragonStateProvider.isDragon(Minecraft.getInstance().player)) {
            emoteButtons.clear();
            DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
            List<DragonEmote> emotes = handler.getBody().value().emotes().value().emotes();
            emotePage = Mth.clamp(emotePage, 0, maxPages(emotes) - 1);

            if (emotes.isEmpty()) {
                return;
            }

            int width = 160;
            int height = 10;

            int startX = chatScreen.width - width;
            int startY = chatScreen.height - 55;

            startX += emoteXOffset;
            startY += emoteYOffset;

            // Emote page count
            ExtendedButton emotePages = new ExtendedButton(startX, startY - (PER_PAGE + 2) * height - 5, width, height, Component.empty().append(">"), button -> {
            }, Supplier::get) {
                @Override
                public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    int color = new Color(0.15F, 0.15F, 0.15F, 0.75F).getRGB();
                    guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);

                    int j = getFGColor();
                    guiGraphics.drawCenteredString(Minecraft.getInstance().font, (emotePage + 1) + "/" + maxPages(emotes), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, j | Mth.ceil(alpha * 255.0F) << 24);
                }

                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    return false;
                }
            };
            emoteButtons.add(emotePages);
            initGuiEvent.addListener(emotePages);

            // Emote left scroll button
            ExtendedButton leftScroll = new ExtendedButton(startX + width / 4 - 10, startY - (PER_PAGE + 2) * height - 5, 15, height, Component.empty(), button -> {
                if (emotePage > 0) {
                    emotePage = Mth.clamp(emotePage - 1, 0, maxPages(emotes) - 1);
                    emotes.clear();
                    emotes.addAll(getEmotes());
                }
                currentlyKeybinding = null;
            }, Supplier::get) {
                @Override
                public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    if (isHovered) {
                        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), new Color(0.35F, 0.35F, 0.35F, 0.75F).getRGB());
                    }

                    guiGraphics.blit(BUTTON_LEFT, getX() + (getWidth() - 9) / 2, getY() + (getHeight() - 9) / 2, 0, 0, 9, 9, 9, 9);
                }
            };
            emoteButtons.add(leftScroll);
            initGuiEvent.addListener(leftScroll);

            // Emote right scroll button
            ExtendedButton rightScroll = new ExtendedButton(startX + width - (width / 4 + 5), startY - (PER_PAGE + 2) * height - 5, 15, height, Component.empty(), button -> {
                if (emotePage < maxPages(emotes) - 1) {
                    emotePage = Mth.clamp(emotePage + 1, 0, maxPages(emotes) - 1);
                    emotes.clear();
                    emotes.addAll(getEmotes());
                }
                currentlyKeybinding = null;
            }, Supplier::get) {
                @Override
                public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    if (isHovered) {
                        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), new Color(0.35F, 0.35F, 0.35F, 0.75F).getRGB());
                    }

                    guiGraphics.blit(BUTTON_RIGHT, getX() + (getWidth() - 9) / 2, getY() + (getHeight() - 9) / 2, 0, 0, 9, 9, 9, 9);
                }
            };
            emoteButtons.add(rightScroll);
            initGuiEvent.addListener(rightScroll);

            // Button to open / close the Emote menu
            ExtendedButton toggleButton = new ExtendedButton(startX, startY, width, height, Component.empty().append(">"), btn -> {
                emoteMenuOpen = !emoteMenuOpen;
                currentlyKeybinding = null;
            }, Supplier::get) {
                @Override
                public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    int color = isHovered ? new Color(0.35F, 0.35F, 0.35F, 0.75F).getRGB() : new Color(0.15F, 0.15F, 0.15F, 0.75F).getRGB();
                    guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, color);

                    int j = getFGColor();
                    guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable(TOGGLE), getX() + width / 2, getY() + (height - 8) / 2, j | Mth.ceil(alpha * 255.0F) << 24);

                    if (emoteMenuOpen) {
                        guiGraphics.blit(BUTTON_UP, getX(), getY(), 0, 0, 9, 9, 9, 9);
                    } else {
                        guiGraphics.blit(BUTTON_DOWN, getX(), getY(), 0, 0, 9, 9, 9, 9);
                    }
                }
            };
            initGuiEvent.addListener(toggleButton);

            // Emote entries
            for (int index = 0; index < PER_PAGE; index++) {
                int finalIndex = index;

                // Emote buttons (Loop | Sound | Emote)
                ExtendedButton loop = new ExtendedButton(startX, startY - 20 - height * (PER_PAGE - 1 - finalIndex), width, height, Component.empty(), btn -> {
                    DragonEmote emote = emotes.size() > finalIndex ? emotes.get(finalIndex) : null;
                    if(emote != null) {
                        addEmote(emote);
                    }
                }, Supplier::get) {
                    @Override
                    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        int color = isHovered && emotes.size() > finalIndex ? new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB() : new Color(0.1F, 0.1F, 0.1F, 0.5F).getRGB();
                        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);

                        DragonEmote emote = emotes.size() > finalIndex ? emotes.get(finalIndex) : null;

                        if (emote != null) {
                            guiGraphics.drawString(Minecraft.getInstance().font, emote.name(), getX() + 22, getY() + (getHeight() - 8) / 2, Color.lightGray.getRGB());
                            guiGraphics.blit(emote.loops() ? PLAY_LOOPED : PLAY_ONCE, getX(), getY(), 0, 0, 10, 10, 10, 10);
                            guiGraphics.blit(emote.sound().isPresent() ? SOUND : NO_SOUND, getX() + 10, getY(), 0, 0, 10, 10, 10, 10);
                        }
                    }
                };

                emoteButtons.add(loop);
                initGuiEvent.addListener(loop);

                // Emote keybind menu
                ExtendedButton emoteKeybindMenu = new ExtendedButton(startX - 65, startY - 20 - height * (PER_PAGE - 1 - finalIndex), 60, height, Component.empty(), btn -> {
                    DragonEmote emote = emotes.size() > finalIndex ? emotes.get(finalIndex) : null;

                    if (emote != null) {
                        currentlyKeybinding = emote.key();
                    }
                }, Supplier::get) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        int color = isHovered && emotes.size() > finalIndex ? new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB() : new Color(0.1F, 0.1F, 0.1F, 0.5F).getRGB();
                        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
                        DragonEmote emote = emotes.size() > finalIndex ? emotes.get(finalIndex) : null;

                        DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
                        if (emote != null) {
                            if (Objects.equals(currentlyKeybinding, emote.key())) {
                                RenderingUtils.drawRect(guiGraphics, getX(), getY(), getWidth() - 1, getHeight(), new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB());
                                TextRenderUtil.drawCenteredScaledText(guiGraphics, getX() + width / 2, getY() + 1, 1f, "...", -1);
                            } else if (DSEmoteKeybindings.EMOTE_KEYBINDS.contains(emote.key())) {
                                int id = DSEmoteKeybindings.EMOTE_KEYBINDS.getKey(emote.key());
                                if (id != 0) {
                                    Key input = Type.KEYSYM.getOrCreate(id);
                                    TextRenderUtil.drawCenteredScaledText(guiGraphics, getX() + getWidth() / 2, getY() + 1, 1f, input.getDisplayName().getString(), -1);
                                }
                            }
                        }
                    }

                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                            DragonEmote emote = emotes.size() > finalIndex ? emotes.get(finalIndex) : null;

                            if (emote != null) {
                                DSEmoteKeybindings.EMOTE_KEYBINDS.remove(emote.key());
                                return true;
                            }
                        }

                        return super.mouseClicked(mouseX, mouseY, button);
                    }
                };
                keybindingButtons.add(emoteKeybindMenu);
                initGuiEvent.addListener(emoteKeybindMenu);

                // Reset Emote keybind button
                ExtendedButton resetEmoteKeybind = new ExtendedButton(startX - 70 - height, startY - 20 - height * (PER_PAGE - 1 - finalIndex), height, height, Component.empty(), btn -> {
                    DragonEmote emote = emotes.size() > finalIndex ? emotes.get(finalIndex) : null;

                    if (emote != null) {
                        currentlyKeybinding = null;
                        DSEmoteKeybindings.EMOTE_KEYBINDS.remove(emote.key());
                    }
                }, Supplier::get) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        DragonEmote emote = emotes.size() > finalIndex ? emotes.get(finalIndex) : null;
                        if(emote == null) {
                            return;
                        }

                        visible = DSEmoteKeybindings.EMOTE_KEYBINDS.contains(emote.key());

                        if (!emoteMenuOpen || !keybinding || !visible) {
                            return;
                        }

                        int color = isHovered && emotes.size() > finalIndex ? new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB() : new Color(0.1F, 0.1F, 0.1F, 0.5F).getRGB();
                        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
                        guiGraphics.blit(RESET_TEXTURE, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
                    }
                };
                keybindingButtons.add(resetEmoteKeybind);
                initGuiEvent.addListener(resetEmoteKeybind);
            }

            // Button to open / close Emote keybinds
            ExtendedButton toggleKeybinds = new ExtendedButton(startX + width / 2 - width / 4, startY - height, width / 2, height, Component.empty(), button -> {
                keybinding = !keybinding;
                currentlyKeybinding = null;
            }, Supplier::get) {
                @Override
                public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
                    active = visible = emoteMenuOpen;
                    isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();

                    if (!emoteMenuOpen) {
                        return;
                    }

                    int color = isHovered ? new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB() : new Color(0.1F, 0.1F, 0.1F, 0.5F).getRGB();
                    guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);

                    int foregroundColor = getFGColor();
                    guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable(KEYBINDS), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, foregroundColor | Mth.ceil(alpha * 255.0F) << 24);
                }
            };
            emoteButtons.add(toggleKeybinds);
            initGuiEvent.addListener(toggleKeybinds);
        }
    }

    public static void focusChatBox(final ChatScreen screen) {
        for (GuiEventListener element : screen.children()) {
            if (element instanceof EditBox) {
                screen.setFocused(element);
                break;
            }
        }
    }

    public static void addEmote(String key) {
        DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
        AtomicReference<DragonEntity> atomicDragon = ClientDragonRenderer.playerDragonHashMap.get(Minecraft.getInstance().player.getId());
        if(atomicDragon == null) {
            return;
        }

        DragonEntity dragon = atomicDragon.get();
        DragonEmote emote = handler.getBody().value().emotes().value().getEmote(key);
        dragon.beginPlayingEmote(emote);
        PacketDistributor.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), emote, false));
    }

    public static void addEmote(DragonEmote emote) {
        AtomicReference<DragonEntity> atomicDragon = ClientDragonRenderer.playerDragonHashMap.get(Minecraft.getInstance().player.getId());
        if(atomicDragon == null) {
            return;
        }

        DragonEntity dragon = atomicDragon.get();
        dragon.beginPlayingEmote(emote);
        PacketDistributor.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), emote, false));
    }

    public static List<DragonEmote> getEmotes() {
        DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
        HashMap<Integer, ArrayList<DragonEmote>> list = new HashMap<>();

        int num = 0;
        for (DragonEmote emote : handler.getBody().value().emotes().value().emotes()) {
            num = createMap(num, list, emote);
        }

        return list.size() > emotePage ? list.get(emotePage) : new ArrayList<>();
    }

    public static int maxPages(List<DragonEmote> emotes) {
        int num = 0;
        HashMap<Integer, ArrayList<DragonEmote>> list = new HashMap<>();

        for (DragonEmote emote : emotes) {
            num = createMap(num, list, emote);
        }

        return list.keySet().size();
    }

    private static int createMap(int num, HashMap<Integer, ArrayList<DragonEmote>> list, DragonEmote emote) {
        if (!list.containsKey(num)) {
            list.put(num, new ArrayList<>());
        }

        if (list.get(num).size() >= PER_PAGE) {
            num++;
            list.put(num, new ArrayList<>());
        }

        list.get(num).add(emote);
        return num;
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key keyInputEvent) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        Screen sc = Minecraft.getInstance().screen;
        int pKeyCode = keyInputEvent.getKey();

        if (pKeyCode == -1) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.isDragon()) {
            if (sc instanceof ChatScreen) {
                if (currentlyKeybinding != null) {
                    if (pKeyCode == 256) {
                        DSEmoteKeybindings.EMOTE_KEYBINDS.remove(currentlyKeybinding);
                    } else {
                        DSEmoteKeybindings.EMOTE_KEYBINDS.put(pKeyCode, currentlyKeybinding);
                    }
                    currentlyKeybinding = null;
                }
            } else {
                String selectedEmote = DSEmoteKeybindings.EMOTE_KEYBINDS.get(pKeyCode);
                if (DSEmoteKeybindings.EMOTE_KEYBINDS.get(pKeyCode) != null) {
                    addEmote(selectedEmote);
                }
            }
        }
    }
}