package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonAltarGUI;
import by.dragonsurvivalteam.dragonsurvival.client.gui.SkinsScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.components.DragonEditorConfirmComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.components.DragonUIRenderComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons.*;
import by.dragonsurvivalteam.dragonsurvival.client.gui.utils.TooltipRender;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.*;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ArrowButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.DropDownButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ExtendedCheckbox;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.magic.ClientMagicHUDHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skinPartSystem.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skinPartSystem.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skinPartSystem.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skinPartSystem.objects.DragonEditorObject.Texture;
import by.dragonsurvivalteam.dragonsurvival.client.skinPartSystem.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.client.skinPartSystem.objects.SkinPreset.SkinAgeGroup;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.client.util.TooltipRendering;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.common.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonType;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncAltarCooldown;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.CompleteDataSync;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.google.common.collect.EvictingQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.apache.commons.lang3.text.WordUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@OnlyIn( Dist.CLIENT)
public class DragonEditorScreen extends Screen implements TooltipRender{
	@ConfigRange( min = 1, max = 1000 )
	@ConfigOption( side = Dist.CLIENT, category = "misc", key = "editorHistory", comment = "The amount of undos and redos that are saved in the dragon editor." )
	public static Integer editorHistory = 10;

	public int HISTORY_SIZE = editorHistory;
	private static final ResourceLocation backgroundTexture = new ResourceLocation("textures/block/dirt.png");
	public final ConcurrentHashMap<Integer, EvictingQueue<CompoundTag>> UNDO_QUEUES = new ConcurrentHashMap<>();
	public final ConcurrentHashMap<Integer, EvictingQueue<CompoundTag>> REDO_QUEUES = new ConcurrentHashMap<>();
	private final Screen source;
	private final String[] animations = {"sit", "idle", "fly", "swim_fast", "run"};
	public int guiLeft;
	public int guiTop;
	public DragonLevel level = DragonLevel.ADULT;
	public DragonType type;
	public SkinPreset preset;
	public boolean confirmation = false;
	public boolean showUi = true;
	public int currentSelected;
	public DragonUIRenderComponent dragonRender;
	public DragonStateHandler handler = new DragonStateHandler();
	public int backgroundColor = -804253680;
	float tick = 0;
	private int curAnimation = 0;
	private int lastSelected;
	private boolean hasInit = false;
	private DragonEditorConfirmComponent conf;

	public DragonEditorScreen(Screen source){
		this(source, DragonType.NONE);
	}

	public DragonEditorScreen(Screen source, DragonType type){
		super(new TranslatableComponent("ds.gui.dragon_editor"));
		this.source = source;
		this.type = type;
	}

	private static void reverseQueue(Queue<CompoundTag> queue){
		int n = queue.size();
		Stack<CompoundTag> stack = new Stack<>();
		for(int i = 0; i < n; i++){
			CompoundTag curr = queue.poll();
			stack.push(curr);
		}
		for(int i = 0; i < n; i++){
			CompoundTag curr = stack.pop();
			queue.add(curr);
		}
	}

	public void doAction(){
		UNDO_QUEUES.computeIfAbsent(currentSelected, (s) -> EvictingQueue.create(HISTORY_SIZE));
		REDO_QUEUES.computeIfAbsent(currentSelected, (s) -> EvictingQueue.create(HISTORY_SIZE));

		REDO_QUEUES.get(currentSelected).clear();
		reverseQueue(UNDO_QUEUES.get(currentSelected));
		UNDO_QUEUES.get(currentSelected).add(preset.writeNBT());
		reverseQueue(UNDO_QUEUES.get(currentSelected));
	}

	public void undoAction(){
		UNDO_QUEUES.computeIfAbsent(currentSelected, (s) -> EvictingQueue.create(HISTORY_SIZE));
		REDO_QUEUES.computeIfAbsent(currentSelected, (s) -> EvictingQueue.create(HISTORY_SIZE));

		if(UNDO_QUEUES.get(currentSelected).size() > 0){
			reverseQueue(REDO_QUEUES.get(currentSelected));
			REDO_QUEUES.get(currentSelected).add(preset.writeNBT());
			reverseQueue(REDO_QUEUES.get(currentSelected));

			this.preset.readNBT(UNDO_QUEUES.get(currentSelected).poll());
			handler.getSkin().updateLayers.addAll(Arrays.stream(EnumSkinLayer.values()).distinct().toList());
			update();
		}
	}

	public void redoAction(){
		UNDO_QUEUES.computeIfAbsent(currentSelected, (s) -> EvictingQueue.create(HISTORY_SIZE));
		REDO_QUEUES.computeIfAbsent(currentSelected, (s) -> EvictingQueue.create(HISTORY_SIZE));

		if(REDO_QUEUES.get(currentSelected).size() > 0){
			reverseQueue(UNDO_QUEUES.get(currentSelected));
			UNDO_QUEUES.get(currentSelected).add(preset.writeNBT());
			reverseQueue(UNDO_QUEUES.get(currentSelected));

			this.preset.readNBT(REDO_QUEUES.get(currentSelected).poll());
			handler.getSkin().updateLayers.addAll(Arrays.stream(EnumSkinLayer.values()).distinct().toList());
			update();
		}
	}

	@Override
	public void render(PoseStack stack, int pMouseX, int pMouseY, float pPartialTicks){
		tick += pPartialTicks;
		if(tick >= (60 * 20)){
			save();
			tick = 0;
		}

		if(showUi){
			dragonRender.x = width / 2 - 70;
			dragonRender.y = guiTop;
			dragonRender.width = 140;
			dragonRender.height = 125;
		}else{
			dragonRender.x = 0;
			dragonRender.width = width;
		}

		FakeClientPlayerUtils.getFakePlayer(0, handler).animationSupplier = () -> animations[curAnimation];

		stack.pushPose();
		stack.translate(0, 0, -600);
		this.renderBackground(stack);
		children().stream().filter(DragonUIRenderComponent.class::isInstance).toList().forEach((s) -> ((DragonUIRenderComponent)s).render(stack, pMouseX, pMouseY, pPartialTicks));
		stack.popPose();

		DragonAltarGUI.renderBorders(stack, backgroundTexture, 0, width, 32, height - 32, width, height);

		stack.pushPose();
		stack.translate(0, 0, 300);

		TextRenderUtil.drawCenteredScaledText(stack, width / 2, 10, 2f, title.getString(), DyeColor.WHITE.getTextColor());

		if(showUi){
			int i = 0;
			for(EnumSkinLayer layers : EnumSkinLayer.values()){
				String name = layers.name;
				SkinsScreen.drawNonShadowLineBreak(stack, font, new TranslatableComponent("ds.gui.dragon_editor.part." + name.toLowerCase()), (i < 5 ? width / 2 - 100 - 100 : width / 2 + 83) + 45, guiTop + 10 + ((i >= 5 ? (i - 5) * 30 : i * 30)) - 12, DyeColor.WHITE.getTextColor());
				i++;
			}
		}

		if(showUi){
			SkinsScreen.drawNonShadowLineBreak(stack, font, new TextComponent(WordUtils.capitalize(animations[curAnimation].replace("_", " "))), width / 2, height / 2 + 72, DyeColor.GRAY.getTextColor());
		}

		for(Widget widget : new CopyOnWriteArrayList<>(this.renderables)){
			widget.render(stack, pMouseX, pMouseY, pPartialTicks);
		}

		for(int x = 0; x < this.children.size(); ++x){
			GuiEventListener ch = children.get(x);
			if(!(ch instanceof DragonUIRenderComponent)){
				((Widget)ch).render(stack, pMouseX, pMouseY, pPartialTicks);
			}
		}

		stack.popPose();
	}

	public void save(){
		SkinPreset newPreset = new SkinPreset();
		newPreset.readNBT(preset.writeNBT());

		if(DragonUtils.getDragonType(minecraft.player) == this.type){
			NetworkHandler.CHANNEL.sendToServer(new SyncPlayerSkinPreset(minecraft.player.getId(), newPreset));
		}

		DragonEditorRegistry.savedCustomizations.skinPresets.computeIfAbsent(this.type, (t) -> new HashMap<>());
		DragonEditorRegistry.savedCustomizations.skinPresets.get(this.type).put(currentSelected, newPreset);

		DragonEditorRegistry.savedCustomizations.current.get(this.type).put(level, currentSelected);

		try{
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			FileWriter writer = new FileWriter(DragonEditorRegistry.savedFile);
			gson.toJson(DragonEditorRegistry.savedCustomizations, writer);
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	@Override
	public void renderBackground(PoseStack pMatrixStack){
		Gui.fill(pMatrixStack, 0, 0, this.width, this.height, backgroundColor);
	}

	@Override
	public void renderBackground(PoseStack pMatrixStack, int pVOffset){
		super.renderBackground(pMatrixStack, pVOffset);
	}

	@Override
	public void init(){
		super.init();

		this.guiLeft = (this.width - 256) / 2;
		this.guiTop = (this.height - 120) / 2;

		conf = new DragonEditorConfirmComponent(this, width / 2 - (130 / 2), height / 2 - (141 / 2), 130, 154);
		initDragonRender();

		DragonStateHandler localHandler = DragonUtils.getHandler(getMinecraft().player);

		if(!hasInit){
			level = localHandler.getLevel();

			if(type == DragonType.NONE){
				type = localHandler.getType();
			}

			currentSelected = DragonEditorRegistry.savedCustomizations.current.get(type).get(level);
			preset = new SkinPreset();
			preset.readNBT(DragonEditorRegistry.savedCustomizations.skinPresets.get(type).get(currentSelected).writeNBT());
			handler.getSkin().skinPreset = preset;

			dragonRender.zoom = (float)(level.size * preset.sizeMul);

			this.handler.setHasWings(true);
			this.handler.setType(type);
			hasInit = true;
			handler.getSkin().updateLayers.addAll(Arrays.stream(EnumSkinLayer.values()).distinct().toList());
			update();
		}

		addRenderableWidget(new NewbornEditorButton(this));
		addRenderableWidget(new YoungEditorButton(this));
		addRenderableWidget(new AdultEditorButton(this));

		int maxWidth = -1;

		for(EnumSkinLayer layers : EnumSkinLayer.values()){
			String name = layers.name().substring(0, 1).toUpperCase(Locale.ROOT) + layers.name().toLowerCase().substring(1).replace("_", " ");
			maxWidth = (int)Math.max(maxWidth, font.width(name) * 1.45F);
		}

		int i = 0;
		for(EnumSkinLayer layers : EnumSkinLayer.values()){
			ArrayList<String> valueList = DragonEditorHandler.getKeys(type, layers);

			if(layers != EnumSkinLayer.BASE){
				valueList.add(0, SkinCap.defaultSkinValue);
			}

			String[] values = valueList.toArray(new String[0]);
			String curValue = preset.skinAges.get(level).layerSettings.get(layers).selectedSkin;


			DropDownButton btn = new DragonEditorDropdownButton(this, i < 5 ? DragonEditorScreen.this.width / 2 - 100 - 100 : DragonEditorScreen.this.width / 2 + 83, DragonEditorScreen.this.guiTop + 10 + ((i >= 5 ? (i - 5) * 30 : i * 30)), 90, 15, curValue, values, layers){
				public void updateMessage(){
					if(current != null){
						message = new TranslatableComponent("ds.skin_part." + type.name().toLowerCase(Locale.ROOT) + "." + current.toLowerCase(Locale.ROOT));
					}
				}
			};
			addRenderableWidget(btn);
			addRenderableWidget(new ArrowButton(btn.x - 15, btn.y + 1, 13, 13, false, (s) -> {
				int index = 0;

				for(int i1 = 0; i1 < btn.values.length; i1++){
					if(Objects.equals(btn.values[i1], btn.current)){
						index = i1;
						break;
					}
				}

				index = DragonUtils.wrap(index - 1, 0, btn.values.length - 1);
				doAction();
				btn.current = btn.values[index];
				btn.setter.accept(btn.current);
				btn.updateMessage();
			}){
				@Override
				public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
					active = showUi;

					if(active){
						super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
					}
				}
			});

			addRenderableWidget(new ArrowButton(btn.x + btn.getWidth() + 2, btn.y + 1, 13, 13, true, (s) -> {
				int index = 0;

				for(int i1 = 0; i1 < btn.values.length; i1++){
					if(Objects.equals(btn.values[i1], btn.current)){
						index = i1;
						break;
					}
				}

				index = DragonUtils.wrap(index + 1, 0, btn.values.length - 1);
				doAction();
				btn.current = btn.values[index];
				btn.setter.accept(btn.current);
				btn.updateMessage();
			}){
				@Override
				public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
					active = showUi || btn.values == null || btn.values.length <= 1;

					if(showUi){
						super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
					}
				}
			});

			addRenderableWidget(new ColorSelectorButton(this, layers, btn.x + 15 + btn.getWidth() + 2, btn.y, btn.getHeight(), btn.getHeight(), (s) -> {
				doAction();
				preset.skinAges.get(level).layerSettings.get(layers).hue = s.floatValue();
				handler.getSkin().updateLayers.add(layers);
				update();
			}));
			i++;
		}

		addRenderableWidget(new Button(width / 2 + 30, height / 2 + 75 - 7, 15, 15, TextComponent.EMPTY, (btn) -> {
			curAnimation += 1;

			if(curAnimation >= animations.length){
				curAnimation = 0;
			}
		}){
			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				this.active = this.visible = showUi;
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
			}

			@Override
			public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				RenderSystem.setShaderTexture(0, ClientMagicHUDHandler.widgetTextures);

				if(isHoveredOrFocused()){
					blit(stack, x, y, 66 / 2, 222 / 2, 11, 17, 128, 128);
				}else{
					blit(stack, x, y, 44 / 2, 222 / 2, 11, 17, 128, 128);
				}
			}
		});

		addRenderableWidget(new Button(width / 2 - 30 - 15, height / 2 + 75 - 7, 15, 15, TextComponent.EMPTY, (btn) -> {
			curAnimation -= 1;

			if(curAnimation < 0){
				curAnimation = animations.length - 1;
			}
		}){
			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				this.active = this.visible = showUi;
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
			}

			@Override
			public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				RenderSystem.setShaderTexture(0, ClientMagicHUDHandler.widgetTextures);

				if(isHoveredOrFocused()){
					blit(stack, x, y, 22 / 2, 222 / 2, 11, 17, 128, 128);
				}else{
					blit(stack, x, y, 0, 222 / 2, 11, 17, 128, 128);
				}
			}
		});

		for(int num = 1; num <= 9; num++){
			addRenderableWidget(new DragonEditorSlotButton(width / 2 + 195 + 15, guiTop + ((num - 1) * 12) + 5 + 20, num, this));
		}

		addRenderableWidget(new ForgeSlider(width / 2 - 100 - 100, height - 25, 100, 20, new TranslatableComponent("ds.gui.dragon_editor.size"), new TextComponent("%"), ServerConfig.minSizeVari, ServerConfig.maxSizeVari, Math.round((preset.sizeMul - 1.0) * 100), true){
			@Override
			protected void applyValue(){
				super.applyValue();
				double val = 1.0 + (getValueInt() / 100.0);
				if(preset.sizeMul != val){
					preset.sizeMul = val;
					dragonRender.zoom = (float)(level.size * preset.sizeMul);
				}

				double val1 = Math.round((preset.sizeMul - 1.0) * 100);

				if(val1 > 0){
					setMessage(new TranslatableComponent("ds.gui.dragon_editor.size").append("+").append(val1 + "%"));
				}else{
					setMessage(new TranslatableComponent("ds.gui.dragon_editor.size").append(val1 + "%"));
				}
			}

			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

				if(isHoveredOrFocused()){
					renderToolTip(pMatrixStack, pMouseX, pMouseY);
				}
			}

			@Override
			public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY){
				TooltipRendering.drawHoveringText(pPoseStack, new TranslatableComponent("ds.gui.dragon_editor.size_info"), pMouseX, pMouseY);
			}
		});

		addRenderableWidget(new ExtendedCheckbox(width / 2 + 100, height - 16, 120, 14, 14, new TranslatableComponent("ds.gui.dragon_editor.wings"), preset.skinAges.get(level).wings, (p) -> preset.skinAges.get(level).wings = p.selected()){
			@Override
			public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				selected = preset.skinAges.get(level).wings;
				super.renderButton(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
			}
		});
		addRenderableWidget(new ExtendedCheckbox(width / 2 + 100, height - 31, 120, 14, 14, new TranslatableComponent("ds.gui.dragon_editor.default_skin"), preset.skinAges.get(level).defaultSkin, (p) -> preset.skinAges.get(level).defaultSkin = p.selected()){
			@Override
			public void renderButton(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				selected = preset.skinAges.get(level).defaultSkin;
				super.renderButton(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
			}

			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, new TranslatableComponent("ds.gui.dragon_editor.default_skin.tooltip"), p_230443_2_, p_230443_3_);
			}
		});

		addRenderableWidget(new ExtendedButton(width / 2 - 75 - 10, height - 25, 75, 20, new TranslatableComponent("ds.gui.dragon_editor.save"), null){
			Widget renderButton;
			boolean toggled;

			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				super.renderButton(mStack, mouseX, mouseY, partial);
				if(isHovered){
					TooltipRendering.drawHoveringText(mStack, new TranslatableComponent("ds.gui.dragon_editor.tooltip.done"), mouseX, mouseY);
				}

				if(toggled && (!visible || !confirmation)){
					toggled = false;
					Screen screen = Minecraft.getInstance().screen;
					screen.children.removeIf((s) -> s == conf);
					screen.renderables.removeIf((s) -> s == renderButton);
				}
			}

			@Override
			public void onPress(){
				DragonStateProvider.getCap(minecraft.player).ifPresent(cap -> {
					minecraft.player.level.playSound(minecraft.player, minecraft.player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);

					if(cap.getType() != type && cap.getType() != DragonType.NONE){
						if(!ServerConfig.saveAllAbilities || !ServerConfig.saveGrowthStage){
							confirmation = true;
							return;
						}
					}
					if(!confirmation){
						confirm();
					}
				});

				if(confirmation){
					if(!toggled){
						renderButton = new ExtendedButton(0, 0, 0, 0, TextComponent.EMPTY, null){
							@Override
							public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_){
								this.active = this.visible = false;

								if(conf != null && confirmation){
									conf.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
								}
							}
						};
						children.add(0, conf);
						children.add(conf);
						renderables.add(renderButton);
					}
					toggled = !toggled;
				}else{
					children.removeIf((s) -> s == conf);
					renderables.removeIf((s) -> s == renderButton);
				}
			}
		});

		addRenderableWidget(new ExtendedButton(width / 2 + 10, height - 25, 75, 20, new TranslatableComponent("ds.gui.dragon_editor.back"), null){
			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				super.renderButton(mStack, mouseX, mouseY, partial);

				if(isHovered){
					TooltipRendering.drawHoveringText(mStack, new TranslatableComponent("ds.gui.dragon_editor.tooltip.back"), mouseX, mouseY);
				}
			}

			@Override
			public void onPress(){
				Minecraft.getInstance().setScreen(source);
			}
		});

		addRenderableWidget(new ExtendedButton(guiLeft + 256 + 16, 9, 19, 19, TextComponent.EMPTY, (btn) -> {
			doAction();
			preset.skinAges.put(level, new SkinAgeGroup(level, type));
			handler.getSkin().updateLayers.addAll(Arrays.stream(EnumSkinLayer.values()).distinct().toList());
			update();
		}){
			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, new TranslatableComponent("ds.gui.dragon_editor.reset"), p_230443_2_, p_230443_3_);
			}

			@Override
			public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				RenderSystem.setShaderTexture(0, new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/reset_button.png"));
				blit(stack, x, y, 0, 0, width, height, width, height);
			}
		});


		addRenderableWidget(new ExtendedButton(guiLeft + 256 + 30 + 16, 9, 19, 19, TextComponent.EMPTY, (btn) -> {
			doAction();

			ArrayList<String> extraKeys = DragonEditorHandler.getKeys(FakeClientPlayerUtils.getFakePlayer(0, handler), EnumSkinLayer.EXTRA);

			extraKeys.removeIf((s) -> {
				Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, handler), EnumSkinLayer.EXTRA, s, type);
				return !text.random;
			});

			for(EnumSkinLayer layer : EnumSkinLayer.values()){
				ArrayList<String> keys = DragonEditorHandler.getKeys(FakeClientPlayerUtils.getFakePlayer(0, handler), layer);

				if(Objects.equals(layer.name, "Extra")){
					keys = extraKeys;
				}

				if(layer != EnumSkinLayer.BASE){
					keys.add(SkinCap.defaultSkinValue);
				}

				if(keys.size() > 0){
					String key = keys.get(minecraft.player.level.random.nextInt(keys.size()));
					if(Objects.equals(layer.name, "Extra")){
						extraKeys.remove(key);
					}

					preset.skinAges.get(level).layerSettings.get(layer).selectedSkin = key;
					Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, handler), layer, key, type);

					if(text != null && text.randomHue){
						preset.skinAges.get(level).layerSettings.get(layer).hue = 0.25f + (minecraft.player.level.random.nextFloat() * 0.5f);
						preset.skinAges.get(level).layerSettings.get(layer).saturation = 0.25f + (minecraft.player.level.random.nextFloat() * 0.5f);
						preset.skinAges.get(level).layerSettings.get(layer).brightness = 0.3f + (minecraft.player.level.random.nextFloat() * 0.2f);
						preset.skinAges.get(level).layerSettings.get(layer).modifiedColor = true;
					}else{
						preset.skinAges.get(level).layerSettings.get(layer).hue = 0.5f;
						preset.skinAges.get(level).layerSettings.get(layer).saturation = 0.5f;
						preset.skinAges.get(level).layerSettings.get(layer).brightness = 0.5f;
						preset.skinAges.get(level).layerSettings.get(layer).modifiedColor = true;
					}
				}
				handler.getSkin().updateLayers.add(layer);
			}

			update();
		}){
			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, new TranslatableComponent("ds.gui.dragon_editor.random"), p_230443_2_, p_230443_3_);
			}

			@Override
			public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				RenderSystem.setShaderTexture(0, new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/random_icon.png"));
				blit(stack, x, y, 0, 0, width, height, width, height);
			}
		});

		addRenderableWidget(new UndoRedoButton(guiLeft + 327, 11, 16, 16, false, (s) -> {
			undoAction();
		}){
			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, new TranslatableComponent("ds.gui.dragon_editor.undo"), p_230443_2_, p_230443_3_);
			}

			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				this.active = UNDO_QUEUES.containsKey(currentSelected) && UNDO_QUEUES.get(currentSelected).size() > 0;
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
			}
		});

		addRenderableWidget(new UndoRedoButton(guiLeft + 347, 11, 16, 16, true, (s) -> {
			redoAction();
		}){
			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, new TranslatableComponent("ds.gui.dragon_editor.redo"), p_230443_2_, p_230443_3_);
			}

			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				this.active = REDO_QUEUES.containsKey(currentSelected) && REDO_QUEUES.get(currentSelected).size() > 0;
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
			}
		});

		addRenderableWidget(new ExtendedButton(width / 2 + 193 + 15, guiTop + 5, 16, 16, TextComponent.EMPTY, (p) -> {}){
			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				this.active = this.visible = showUi;
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

				if(visible){
					RenderSystem.setShaderTexture(0, new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/save_icon.png"));
					blit(pMatrixStack, x, y, 0, 0, 16, 16, 16, 16);
				}
			}

			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, new TranslatableComponent("ds.gui.dragon_editor.save_slot"), p_230443_2_, p_230443_3_);
			}

			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){}
		});

		addRenderableWidget(new CopySettingsButton(this, width / 2 + 193 + 15, guiTop - 16, 16, 16, TextComponent.EMPTY, (p) -> {}));

		addRenderableWidget(new ExtendedButton(dragonRender.x + dragonRender.width - 17, dragonRender.y + dragonRender.height + 3, 15, 15, TextComponent.EMPTY, (btn) -> {
			dragonRender.yRot = -3;
			dragonRender.xRot = -5;
			dragonRender.xOffset = 0;
			dragonRender.yOffset = 0;
			dragonRender.zoom = (float)(level.size * preset.sizeMul);
		}){
			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, new TranslatableComponent("ds.gui.dragon_editor.reset"), p_230443_2_, p_230443_3_);
			}

			@Override
			public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				RenderSystem.setShaderTexture(0, new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/reset_position_button.png"));
				blit(stack, x, y, 0, 0, width, height, width, height);
			}

			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				this.active = this.visible = showUi;
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
			}
		});

		addRenderableWidget(new ExtendedCheckbox(guiLeft - 15, 11, 40, 16, 16, new TranslatableComponent("ds.gui.dragon_editor.show_ui"), showUi, (p) -> showUi = p.selected()));
		addRenderableWidget(new BackgroundColorButton(guiLeft - 45, 10, 18, 18, TextComponent.EMPTY, (s) -> {}, this));
		addRenderableWidget(new HelpButton(type, guiLeft - 75, 11, 15, 15, "ds.help.customization", 1));
		//addRenderableWidget(new ScreenshotButton(guiLeft + 240, 10, 18, 18, TextComponent.EMPTY, (s) -> {}, this));

	}

	public void update(){
		if(type != DragonType.NONE){
			handler.setType(type);
		}
		handler.getSkin().skinPreset = preset;
		handler.setSize(level.size);
		handler.setHasWings(true);

		if(currentSelected != lastSelected){
			preset = new SkinPreset();
			preset.readNBT(DragonEditorRegistry.savedCustomizations.skinPresets.get(type).get(currentSelected).writeNBT());

			handler.getSkin().skinPreset = preset;
		}

		lastSelected = currentSelected;

		initDragonRender();
	}

	private void initDragonRender(){
		children.removeIf(DragonUIRenderComponent.class::isInstance);

		float yRot = -3, xRot = -5, zoom = 0, xOffset = 0, yOffset = 0;
		if(dragonRender != null){
			yRot = dragonRender.yRot;
			xRot = dragonRender.xRot;
			zoom = dragonRender.zoom;
			xOffset = dragonRender.xOffset;
			yOffset = dragonRender.yOffset;
		}

		dragonRender = new DragonUIRenderComponent(this, width / 2 - 70, guiTop, 140, 125, () -> FakeClientPlayerUtils.getFakeDragon(0, handler));
		dragonRender.xRot = xRot;
		dragonRender.yRot = yRot;
		dragonRender.zoom = zoom;
		dragonRender.xOffset = xOffset;
		dragonRender.yOffset = yOffset;

		children.add(0, dragonRender);
	}

	public void confirm(){
		DragonStateProvider.getCap(minecraft.player).ifPresent(cap -> {
			minecraft.player.level.playSound(minecraft.player, minecraft.player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);

			if(cap.getType() != type){
				Minecraft.getInstance().player.sendMessage(new TranslatableComponent("ds." + type.name().toLowerCase() + "_dragon_choice"), Minecraft.getInstance().player.getUUID());
				cap.setType(type);

				if(!ServerConfig.saveGrowthStage || cap.getSize() == 0){
					cap.setSize(DragonLevel.BABY.size);
				}

				cap.setHasWings(ServerConfig.saveGrowthStage ? cap.hasWings() || ServerConfig.startWithWings : ServerConfig.startWithWings);
				cap.setIsHiding(false);
				cap.getMovementData().spinLearned = ServerConfig.saveGrowthStage && cap.getMovementData().spinLearned;

				NetworkHandler.CHANNEL.sendToServer(new CompleteDataSync(Minecraft.getInstance().player.getId(), cap.writeNBT()));
				NetworkHandler.CHANNEL.sendToServer(new SyncAltarCooldown(Minecraft.getInstance().player.getId(), Functions.secondsToTicks(ServerConfig.altarUsageCooldown)));
				NetworkHandler.CHANNEL.sendToServer(new SyncSpinStatus(Minecraft.getInstance().player.getId(), cap.getMovementData().spinAttack, cap.getMovementData().spinCooldown, cap.getMovementData().spinLearned));
				ClientEvents.sendClientData(new RequestClientData(cap.getType(), cap.getLevel()));

				if(type == DragonType.NONE && cap.getType() != DragonType.NONE){
					DragonCommand.reInsertClawTools(Minecraft.getInstance().player, cap);
				}
			}
		});

		save();

		Minecraft.getInstance().player.closeContainer();
	}

	@Override
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY){
		if(!super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)){
			if(dragonRender != null && dragonRender.isMouseOver(pMouseX, pMouseY)){
				return dragonRender.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
			}
		}

		return false;
	}
}