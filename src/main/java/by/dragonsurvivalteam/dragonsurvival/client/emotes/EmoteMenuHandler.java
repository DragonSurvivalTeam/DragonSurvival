package by.dragonsurvivalteam.dragonsurvival.client.emotes;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.EmoteCap;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mod.EventBusSubscriber( Dist.CLIENT )
public class EmoteMenuHandler {
	private static final int PER_PAGE = 10;
	private static final ResourceLocation EMPTY_SLOT = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/emote/empty_slot.png");
	private static final ResourceLocation PLAY_ONCE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/emote/play_once.png");
	private static final ResourceLocation PLAY_LOOPED = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/emote/play_looped.png");
	private static final ResourceLocation SOUND = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/emote/sound.png");
	private static final ResourceLocation NO_SOUND = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/emote/no_sound.png");
	private static final ResourceLocation BUTTON_UP = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/emote/button_up.png");
	private static final ResourceLocation BUTTON_DOWN = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/emote/button_down.png");
	private static final ResourceLocation BUTTON_LEFT = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/emote/button_left.png");
	private static final ResourceLocation BUTTON_RIGHT = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/emote/button_right.png");
	public static final ResourceLocation resetTexture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/reset_icon.png");

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = ConfigSide.CLIENT, category = {"ui", "emotes"}, key = "emoteXOffset", comment = "Offset the x position of the emote button in relation to its normal position" )
	public static Integer emoteXOffset = 0;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = ConfigSide.CLIENT, category = {"ui", "emotes"}, key = "emoteYOffset", comment = "Offset the y position of the emote button in relation to its normal position" )
	public static Integer emoteYOffset = 0;

	private static int emotePage = 0;
	private static boolean keybinding = false;
	private static String currentlyKeybinding = null;

	@SubscribeEvent
	public static void addEmoteButton(ScreenEvent.Init.Post initGuiEvent){
		Screen screen = initGuiEvent.getScreen();
		currentlyKeybinding = null;

		if (screen instanceof ChatScreen chatScreen && DragonUtils.isDragon(Minecraft.getInstance().player)) {
			emotePage = Mth.clamp(emotePage, 0, maxPages() - 1);
			List<Emote> emotes = getEmotes();

			if(emotes == null || emotes.size() <= 0){
				return;
			}

			int width = 160;
			int height = 10;

			int startX = chatScreen.width - width;
			int startY = chatScreen.height - 55;

			startX += emoteXOffset;
			startY += emoteYOffset;

			// Emote page count
			initGuiEvent.addListener(new Button(startX, startY - (PER_PAGE + 2) * height - 5, width, height, Component.empty().append(">"), button -> {
			}, Supplier::get){
				@Override
				public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
					isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
					DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
					active = visible = handler.getEmoteData().emoteMenuOpen;
					if(!handler.getEmoteData().emoteMenuOpen){
						return;
					}
					int color = new Color(0.15F, 0.15F, 0.15F, 0.75F).getRGB();
					guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);

					int j = getFGColor();
					guiGraphics.drawCenteredString(Minecraft.getInstance().font, (emotePage + 1) + "/" + maxPages(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, j | Mth.ceil(alpha * 255.0F) << 24);
				}

				@Override
				public boolean mouseClicked(double mouseX, double mouseY, int button) {
					return false;
				}
			});

			// Emote left scroll button
			initGuiEvent.addListener(new Button(startX + width / 4 - 10, startY - (PER_PAGE + 2) * height - 5, 15, height, Component.empty(), button -> {
				if(emotePage > 0){
					emotePage = Mth.clamp(emotePage - 1, 0, maxPages() - 1);
					emotes.clear();
					emotes.addAll(getEmotes());
				}
				currentlyKeybinding = null;
			}, Supplier::get) {
				@Override
				public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
					isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
					DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
					active = visible = handler.getEmoteData().emoteMenuOpen;
					if(!handler.getEmoteData().emoteMenuOpen){
						return;
					}

					if (isHovered) {
						guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), new Color(0.35F, 0.35F, 0.35F, 0.75F).getRGB());
					}

					guiGraphics.blit(BUTTON_LEFT, getX() + (getWidth() - 9) / 2, getY() + (getHeight() - 9) / 2, 0, 0, 9, 9, 9, 9);
				}
			});

			// Emote right scroll button
			initGuiEvent.addListener(new Button(startX + width - (width / 4 + 5), startY - (PER_PAGE + 2) * height - 5, 15, height, Component.empty(), button -> {
				if(emotePage < maxPages() - 1){
					emotePage = Mth.clamp(emotePage + 1, 0, maxPages() - 1);
					emotes.clear();
					emotes.addAll(getEmotes());
				}
				currentlyKeybinding = null;
			}, Supplier::get) {
				@Override
				public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
					isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
					DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
					active = visible = handler.getEmoteData().emoteMenuOpen;
					if(!handler.getEmoteData().emoteMenuOpen){
						return;
					}

					if (isHovered) {
						guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), new Color(0.35F, 0.35F, 0.35F, 0.75F).getRGB());
					}

					guiGraphics.blit(BUTTON_RIGHT, getX() + (getWidth() - 9) / 2, getY() + (getHeight() - 9) / 2, 0, 0, 9, 9, 9, 9);
				}
			});

			// Button to open / close the Emote menu
			initGuiEvent.addListener(new Button(startX, startY, width, height, Component.empty().append(">"), btn -> {
				DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
				handler.getEmoteData().emoteMenuOpen = !handler.getEmoteData().emoteMenuOpen;
				NetworkHandler.CHANNEL.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), handler.getEmoteData()));
				currentlyKeybinding = null;
			}, Supplier::get) {
				@Override
				public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
					isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
					DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
					int color = isHovered ? new Color(0.35F, 0.35F, 0.35F, 0.75F).getRGB() : new Color(0.15F, 0.15F, 0.15F, 0.75F).getRGB();
					guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, color);

					int j = getFGColor();
					guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("ds.emote.toggle"), getX() + width / 2, getY() + (height - 8) / 2, j | Mth.ceil(alpha * 255.0F) << 24);

					if(handler.getEmoteData().emoteMenuOpen){
						guiGraphics.blit(BUTTON_UP, getX(), getY(), 0, 0, 9, 9, 9, 9);
					}else{
						guiGraphics.blit(BUTTON_DOWN, getX(), getY(), 0, 0, 9, 9, 9, 9);
					}
				}
			});

			// Emote entries
			for(int i = 0; i < PER_PAGE; i++){
				int finalI = i;

				// Emote buttons (Loop | Sound | Emote)
				initGuiEvent.addListener(new Button(startX, startY - 20 - height * (PER_PAGE - 1 - finalI), width, height, Component.empty(), btn -> {
					DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
					Emote emote = emotes.size() > finalI ? emotes.get(finalI) : null;

					if(emote == null || Stream.of(handler.getEmoteData().currentEmotes).anyMatch(s -> s != null && Objects.equals(s.animation, emote.animation))){
						return;
					}

					if(emote.blend && Stream.of(handler.getEmoteData().currentEmotes).anyMatch(s -> s != null && s.blend) || !emote.blend && Stream.of(handler.getEmoteData().currentEmotes).anyMatch(s -> s != null && !s.blend)){
						clearEmotes(Minecraft.getInstance().player);
					}

					addEmote(emote);
				}, Supplier::get) {
					@Override
					public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
						DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
						active = visible = handler.getEmoteData().emoteMenuOpen;
						isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
						if(!handler.getEmoteData().emoteMenuOpen){
							return;
						}
						int color = isHovered && emotes.size() > finalI ? new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB() : new Color(0.1F, 0.1F, 0.1F, 0.5F).getRGB();
						guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);

						Emote emote = emotes.size() > finalI ? emotes.get(finalI) : null;

						if(emote != null){
							guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable(emote.name), getX() + 22, getY() + (getHeight() - 8) / 2, Color.lightGray.getRGB());
							guiGraphics.blit(emote.loops ? PLAY_LOOPED : PLAY_ONCE, getX(), getY(), 0, 0, 10, 10, 10, 10);
							guiGraphics.blit(emote.sound != null ? SOUND : NO_SOUND, getX() + 10, getY(), 0, 0, 10, 10, 10, 10);
						}
					}
				});

				// Emote keybind menu
				initGuiEvent.addListener(new ExtendedButton(startX - 65, startY - 20 - height * (PER_PAGE - 1 - finalI), 60, height, Component.empty(), btn -> {
					Emote emote = emotes.size() > finalI ? emotes.get(finalI) : null;

					if(emote != null){
						currentlyKeybinding = emote.id;
					}
				}, Supplier::get) {
					@Override
					public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
						DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
						active = visible = handler.getEmoteData().emoteMenuOpen && keybinding;
						isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();

						if(!handler.getEmoteData().emoteMenuOpen || !keybinding){
							return;
						}

						int color = isHovered && emotes.size() > finalI ? new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB() : new Color(0.1F, 0.1F, 0.1F, 0.5F).getRGB();
						guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);


						Emote emote = emotes.size() > finalI ? emotes.get(finalI) : null;

						if(emote != null){
							if(Objects.equals(currentlyKeybinding, emote.id)){
								RenderingUtils.drawRect(guiGraphics, getX(), getY(), getWidth() - 1, getHeight(), new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB());
								TextRenderUtil.drawCenteredScaledText(guiGraphics, getX() + width / 2, getY() + 1, 1f, "...", -1);
							}else if(handler.getEmoteData().emoteKeybinds.containsKey(emote.id)){
								int id = handler.getEmoteData().emoteKeybinds.get(emote.id);
								if(id != 0){
									Key input = Type.KEYSYM.getOrCreate(id);
									TextRenderUtil.drawCenteredScaledText(guiGraphics, getX() + getWidth() / 2, getY() + 1, 1f, input.getDisplayName().getString(), -1);
								}
							}
						}
					}

					@Override
					public boolean mouseClicked(double mouseX, double mouseY, int button){
						if(button == GLFW.GLFW_MOUSE_BUTTON_RIGHT){
							DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
							Emote emote = emotes.size() > finalI ? emotes.get(finalI) : null;

							if(emote != null){
								handler.getEmoteData().emoteKeybinds.put(emote.id, -1);
								NetworkHandler.CHANNEL.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), handler.getEmoteData()));
								return true;
							}
						}

						return super.mouseClicked(mouseX, mouseY, button);
					}
				});

				// Reset Emote keybind button
				initGuiEvent.addListener(new ExtendedButton(startX - 70 - height, startY - 20 - height * (PER_PAGE - 1 - finalI), height, height, Component.empty(), btn -> {
					Emote emote = emotes.size() > finalI ? emotes.get(finalI) : null;
					DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);

					if(emote != null){
						currentlyKeybinding = null;
						handler.getEmoteData().emoteKeybinds.put(emote.id, -1);
						NetworkHandler.CHANNEL.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), handler.getEmoteData()));
					}
				}, Supplier::get) {
					@Override
					public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
						Emote emote = emotes.size() > finalI ? emotes.get(finalI) : null;

						DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
						active = visible = handler.getEmoteData().emoteMenuOpen && keybinding && emote != null && handler.getEmoteData().emoteKeybinds.getOrDefault(emote.id, -1) != -1;
						isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();

						if(!handler.getEmoteData().emoteMenuOpen || !keybinding || emote == null || handler.getEmoteData().emoteKeybinds.getOrDefault(emote.id, -1) == -1){
							return;
						}

						int color = isHovered && emotes.size() > finalI ? new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB() : new Color(0.1F, 0.1F, 0.1F, 0.5F).getRGB();
						guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
						guiGraphics.blit(resetTexture, getX(), getY(), 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
					}
				});
			}

			// Button to open / close Emote keybinds
			initGuiEvent.addListener(new ExtendedButton(startX + width / 2 - width / 4, startY - height, width / 2, height, Component.empty(), button -> {
				keybinding = !keybinding;
				currentlyKeybinding = null;
			}, Supplier::get) {
				@Override
				public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
					DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
					active = visible = handler.getEmoteData().emoteMenuOpen;
					isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();

					if(!handler.getEmoteData().emoteMenuOpen){
						return;
					}

					int color = isHovered ? new Color(0.1F, 0.1F, 0.1F, 0.8F).getRGB() : new Color(0.1F, 0.1F, 0.1F, 0.5F).getRGB();
					guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);

					int foregroundColor = getFGColor();
					guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("ds.emote.keybinds"), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, foregroundColor | Mth.ceil(alpha * 255.0F) << 24);
				}
			});
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

	public static void clearEmotes(final Entity entity){
		if (entity instanceof Player) {
			DragonStateHandler handler = DragonUtils.getHandler(entity);
			handler.getEmoteData().currentEmotes = new Emote[EmoteCap.MAX_EMOTES];
			handler.getEmoteData().emoteTicks = new Integer[EmoteCap.MAX_EMOTES];
			NetworkHandler.CHANNEL.sendToServer(new SyncEmote(entity.getId(), handler.getEmoteData()));
		}
	}

	public static void addEmote(Emote emote){
		DragonStateHandler cap = DragonUtils.getHandler(Minecraft.getInstance().player);
		for(int i = 0; i < EmoteCap.MAX_EMOTES; i++){
			if(cap.getEmoteData().currentEmotes[i] == null){
				cap.getEmoteData().currentEmotes[i] = emote;
				break;
			}
		}

		List<Emote> ls1 = Stream.of(cap.getEmoteData().currentEmotes).limit(EmoteCap.MAX_EMOTES).toList();
		List<Integer> ls2 = Stream.of( cap.getEmoteData().emoteTicks).limit(EmoteCap.MAX_EMOTES).toList();

		cap.getEmoteData().currentEmotes = ls1.toArray(new Emote[0]);
		cap.getEmoteData().emoteTicks = ls2.toArray(new Integer[0]);

		NetworkHandler.CHANNEL.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), cap.getEmoteData()));
	}

	public static List<Emote> getEmotes(){
		DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);

		ArrayList<Emote> emotes = new ArrayList<>();
		HashMap<Integer, ArrayList<Emote>> list = new HashMap<>();

		emotes.addAll(EmoteRegistry.EMOTES);

		emotes.removeIf(em -> {
			if(em.requirements != null){
				if(em.requirements.type != null){
					boolean hasType = false;
					for(String t : em.requirements.type){
						if(t.equalsIgnoreCase(handler.getTypeName())){
							hasType = true;
							break;
						}
					}

					if(!hasType){
						return true;
					}
				}

				if(em.requirements.age != null){
					boolean hasAge = false;
					for(String t : em.requirements.age){
						if(t.equalsIgnoreCase(handler.getLevel().name)){
							hasAge = true;
							break;
						}
					}

					return !hasAge;
				}
			}

			return false;
		});

		int num = 0;
		for(Emote emote : emotes){
			num = createMap(num, list, emote);
		}

		return list.size() > emotePage ? list.get(emotePage) : new ArrayList<>();
	}

	public static int maxPages(){
		int num = 0;
		HashMap<Integer, ArrayList<Emote>> list = new HashMap<>();

		for(Emote emote : EmoteRegistry.EMOTES){
			num = createMap(num, list, emote);
		}

		return list.keySet().size();
	}

	private static int createMap(int num, HashMap<Integer, ArrayList<Emote>> list, Emote emote){
		if(!list.containsKey(num)){
			list.put(num, new ArrayList<>());
		}

		if(list.get(num).size() >= PER_PAGE){
			num++;
			list.put(num, new ArrayList<>());
		}

		list.get(num).add(emote);
		return num;
	}

	@OnlyIn( Dist.CLIENT )
	@SubscribeEvent
	public static void onKey(InputEvent.Key keyInputEvent){
		Screen sc = Minecraft.getInstance().screen;
		int pKeyCode = keyInputEvent.getKey();

		if (pKeyCode == -1) {
			return;
		}

		DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);
		if (pKeyCode == -1) {
			return;
		}

		if(handler.isDragon()){
			if(sc instanceof ChatScreen){
				if(currentlyKeybinding != null){
					if(pKeyCode == 256){
						handler.getEmoteData().emoteKeybinds.remove(currentlyKeybinding);
					}else{
						handler.getEmoteData().emoteKeybinds.put(currentlyKeybinding, keyInputEvent.getKey());
					}
					NetworkHandler.CHANNEL.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), handler.getEmoteData()));
					currentlyKeybinding = null;
				}
			}else{
				if(handler.getEmoteData().emoteKeybinds.contains(pKeyCode)){
					Map.Entry<String, Integer> entry = handler.getEmoteData().emoteKeybinds.entrySet().stream().filter(s -> s.getValue() == pKeyCode).findFirst().orElse(null);
					if(entry != null){
						Emote emote = EmoteRegistry.EMOTES.stream().filter(s -> Objects.equals(s.id, entry.getKey())).findFirst().orElse(null);

						if(emote == null || Stream.of(handler.getEmoteData().currentEmotes).anyMatch(s -> s != null && Objects.equals(s.animation, emote.animation))){
							return;
						}

						if(emote.blend && Stream.of(handler.getEmoteData().currentEmotes).anyMatch(s -> s != null && s.blend) || !emote.blend && Stream.of(handler.getEmoteData().currentEmotes).anyMatch(s -> s != null && !s.blend)){
							clearEmotes(Minecraft.getInstance().player);
						}

						addEmote(emote);
					}
				}
			}
		}
	}
}