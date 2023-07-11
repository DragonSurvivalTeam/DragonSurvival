package by.dragonsurvivalteam.dragonsurvival.client.gui;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.magic.ClientMagicHUDHandler;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TooltipRendering;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.processor.IBone;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class SkinsScreen extends Screen{
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/skin_interface.png");

	private static final ResourceLocation UNCHECKED = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/unchecked.png");
	private static final ResourceLocation CHECKED = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_claws_tetris.png");

	private static final ResourceLocation DISCORD = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/discord_button.png");
	private static final ResourceLocation WIKI = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/wiki_button.png");
	private static final ResourceLocation HELP = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_claws_button.png");

	private static final String DISCORD_URL = "http://discord.gg/8SsB8ar";
	private static final String WIKI_URL = "https://github.com/DragonSurvivalTeam/DragonSurvival/wiki/3.-Customization";
	private static final ArrayList<String> seenSkins = new ArrayList<>();
	private final DragonStateHandler handler = new DragonStateHandler();
	public static ResourceLocation skinTexture = null;
	public static ResourceLocation glowTexture = null;
	private static String playerName = null;
	private static String lastPlayerName = null;
	private static DragonLevel level = DragonLevel.ADULT;
	private static boolean noSkin = false;
	private static boolean loading = false;
	public Screen sourceScreen;
	private int guiLeft;
	private int guiTop;
	private float yRot = -3;
	private float xRot = -5;
	private float zoom = 0;
	private URI clickedLink;
	protected int imageWidth = 164;
	protected int imageHeight = 128;

	public SkinsScreen(Screen sourceScreen){
		super(Component.empty());
		this.sourceScreen = sourceScreen;
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks){
		if(minecraft == null){
			return;
		}

		stack.pushPose();
		stack.translate(0F, 0F, -500);
		renderBackground(stack);
		stack.popPose();

		int startX = guiLeft;
		int startY = guiTop;

		stack.pushPose();
		final IBone neckandHead = ClientDragonRender.dragonModel.getAnimationProcessor().getBone("Neck");

		if(neckandHead != null){
			neckandHead.setHidden(false);
		}

		DragonEntity dragon = FakeClientPlayerUtils.getFakeDragon(0, handler);
		EntityRenderer<? super DragonEntity> dragonRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon);

		if(noSkin && Objects.equals(playerName, minecraft.player.getGameProfile().getName())){
			ClientDragonRender.dragonModel.setCurrentTexture(null);
			((DragonRenderer)dragonRenderer).glowTexture = null;
		}else{
			ClientDragonRender.dragonModel.setCurrentTexture(skinTexture);
			((DragonRenderer)dragonRenderer).glowTexture = glowTexture;
		}

		float scale = zoom;
		stack.scale(scale, scale, scale);

		if(!loading){
			handler.setHasWings(true);
			handler.setSize(level.size);
			handler.setType(DragonUtils.getDragonType(minecraft.player));

			handler.getSkinData().skinPreset.initDefaults(handler);

			if(noSkin && Objects.equals(playerName, minecraft.player.getGameProfile().getName())){
				handler.getSkinData().skinPreset.readNBT(DragonUtils.getHandler(minecraft.player).getSkinData().skinPreset.writeNBT());
			}else{
				handler.getSkinData().skinPreset.skinAges.get(level).get().defaultSkin = true;
			}

			FakeClientPlayerUtils.getFakePlayer(0, handler).animationSupplier = () -> "fly";
			stack.pushPose();
			stack.translate(0, 0, 100);
			ClientDragonRender.renderEntityInInventory(dragon, startX + 15, startY + 70, scale, xRot, yRot);
			stack.popPose();
		}

		((DragonRenderer)dragonRenderer).glowTexture = null;

		stack.popPose();

		stack.translate(0F, 0F, 400);

		RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
		blit(stack, startX + 128, startY, 0, 0, 164, 256);

		drawNonShadowString(stack, minecraft.font, Component.translatable("ds.gui.skins").withStyle(ChatFormatting.DARK_GRAY), startX + 128 + imageWidth / 2, startY + 7, -1);
		drawCenteredString(stack, minecraft.font, Component.translatable("ds.gui.skins.toggle"), startX + 128 + imageWidth / 2, startY + 30, -1);

		drawNonShadowString(stack, minecraft.font, Component.empty().append(playerName + " - " + level.getName()).withStyle(ChatFormatting.GRAY), startX + 15, startY - 15, -1);

		if(!loading){
			if(noSkin){
				if(playerName == minecraft.player.getGameProfile().getName()){
					drawNonShadowLineBreak(stack, minecraft.font, Component.translatable("ds.gui.skins.noskin.yours").withStyle(ChatFormatting.DARK_GRAY), startX + 40, startY + imageHeight - 20, -1);
				}else{
					drawNonShadowLineBreak(stack, minecraft.font, Component.translatable("ds.gui.skins.noskin").withStyle(ChatFormatting.DARK_GRAY), startX + 65, startY + imageHeight - 20, -1);
				}
			}
		}

		super.render(stack, mouseX, mouseY, partialTicks);

		for(Widget btn : renderables){
			if(btn instanceof AbstractWidget && ((AbstractWidget)btn).isHoveredOrFocused()){
				((AbstractWidget)btn).renderToolTip(stack, mouseX, mouseY);
			}
		}

		stack.translate(0F, 0F, -400f);
	}

	public static void drawNonShadowString(PoseStack p_238472_0_, Font p_238472_1_, Component p_238472_2_, int p_238472_3_, int p_238472_4_, int p_238472_5_){
		p_238472_1_.draw(p_238472_0_, Language.getInstance().getVisualOrder(p_238472_2_), p_238472_3_ - p_238472_1_.width(p_238472_2_) / 2, p_238472_4_, p_238472_5_);
	}

	public static void drawNonShadowLineBreak(PoseStack p_238472_0_, Font p_238472_1_, Component p_238472_2_, int p_238472_3_, int p_238472_4_, int p_238472_5_){
		FormattedCharSequence ireorderingprocessor = p_238472_2_.getVisualOrderText();

		List<FormattedText> wrappedLine = p_238472_1_.getSplitter().splitLines(p_238472_2_, 150, Style.EMPTY);
		int i = 0;
		for(FormattedText properties : wrappedLine){
			p_238472_1_.draw(p_238472_0_, Language.getInstance().getVisualOrder(properties), p_238472_3_ - p_238472_1_.width(ireorderingprocessor) / 2, p_238472_4_ + i * 9, p_238472_5_);
			i++;
		}
	}

	@Override
	public void init(){
		super.init();

		guiLeft = (width - 256) / 2;
		guiTop = (height - 128) / 2;

		int startX = guiLeft;
		int startY = guiTop;

		if(playerName == null){
			playerName = minecraft.player.getGameProfile().getName();
		}

		setTextures();

		addRenderableWidget(new TabButton(startX + 128 + 5, startY - 26, 0, this));
		addRenderableWidget(new TabButton(startX + 128 + 33, startY - 26, 1, this));
		addRenderableWidget(new TabButton(startX + 128 + 62, startY - 26, 2, this));
		addRenderableWidget(new TabButton(startX + 128 + 91, startY - 28, 3, this));

		addRenderableWidget(new Button(startX + 128, startY + 45, imageWidth, 20, Component.translatable("ds.level.newborn"), button -> {
			DragonStateHandler handler = DragonUtils.getHandler(getMinecraft().player);

			handler.getSkinData().renderNewborn = !handler.getSkinData().renderNewborn;
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonSkinSettings(getMinecraft().player.getId(), handler.getSkinData().renderNewborn, handler.getSkinData().renderYoung, handler.getSkinData().renderAdult));
			setTextures();
		}){
			@Override
			public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);

				DragonStateHandler handler = DragonUtils.getHandler(getMinecraft().player);
				RenderSystem.setShaderTexture(0, !handler.getSkinData().renderNewborn ? UNCHECKED : CHECKED);
				blit(p_230431_1_, x + 3, y + 3, 0, 0, 13, 13, 13, 13);
			}
		});

		addRenderableWidget(new Button(startX + 128, startY + 45 + 23, imageWidth, 20, Component.translatable("ds.level.young"), button -> {
			DragonStateHandler handler = DragonUtils.getHandler(getMinecraft().player);

			handler.getSkinData().renderYoung = !handler.getSkinData().renderYoung;
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonSkinSettings(getMinecraft().player.getId(), handler.getSkinData().renderNewborn, handler.getSkinData().renderYoung, handler.getSkinData().renderAdult));
			setTextures();
		}){
			@Override
			public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);

				DragonStateHandler handler = DragonUtils.getHandler(getMinecraft().player);
				RenderSystem.setShaderTexture(0, !handler.getSkinData().renderYoung ? UNCHECKED : CHECKED);
				blit(p_230431_1_, x + 3, y + 3, 0, 0, 13, 13, 13, 13);
			}
		});

		addRenderableWidget(new Button(startX + 128, startY + 45 + 46, imageWidth, 20, Component.translatable("ds.level.adult"), button -> {
			DragonStateHandler handler = DragonUtils.getHandler(getMinecraft().player);

			handler.getSkinData().renderAdult = !handler.getSkinData().renderAdult;
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonSkinSettings(getMinecraft().player.getId(), handler.getSkinData().renderNewborn, handler.getSkinData().renderYoung, handler.getSkinData().renderAdult));
			setTextures();
		}){
			@Override
			public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);

				DragonStateHandler handler = DragonUtils.getHandler(getMinecraft().player);
				RenderSystem.setShaderTexture(0, !handler.getSkinData().renderAdult ? UNCHECKED : CHECKED);
				blit(p_230431_1_, x + 3, y + 3, 0, 0, 13, 13, 13, 13);
			}
		});

		addRenderableWidget(new Button(startX + 128, startY + 128, imageWidth, 20, Component.translatable("ds.gui.skins.other_skins"), button -> {
			ConfigHandler.updateConfigValue("renderOtherPlayerSkins", !ClientDragonRender.renderOtherPlayerSkins);
			//			executor.execute(() -> setTextures());
			setTextures();
		}){
			@Override
			public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);

				RenderSystem.setShaderTexture(0, ClientDragonRender.renderOtherPlayerSkins ? CHECKED : UNCHECKED);
				blit(p_230431_1_, x + 3, y + 3, 0, 0, 13, 13, 13, 13);
			}
		});

		addRenderableWidget(new Button(startX + 128 + imageWidth / 2 - 8, startY + 128 + 30, 16, 16, Component.empty(), button -> {
			try{
				URI uri = new URI(DISCORD_URL);
				clickedLink = uri;
				minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, DISCORD_URL, false));
			}catch(URISyntaxException urisyntaxexception){
				urisyntaxexception.printStackTrace();
			}
		}){
			@Override
			public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				RenderSystem.setShaderTexture(0, DISCORD);
				blit(p_230431_1_, x, y, 0, 0, 16, 16, 16, 16);
			}

			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, Component.translatable("ds.gui.skins.tooltip.discord"), p_230443_2_, p_230443_3_);
			}
		});

		addRenderableWidget(new Button(startX + 128 + imageWidth / 2 - 8 + 25, startY + 128 + 30, 16, 16, Component.empty(), button -> {
			try{
				URI uri = new URI(WIKI_URL);
				clickedLink = uri;
				minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, WIKI_URL, false));
			}catch(URISyntaxException urisyntaxexception){
				urisyntaxexception.printStackTrace();
			}
		}){
			@Override
			public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				RenderSystem.setShaderTexture(0, WIKI);
				blit(p_230431_1_, x, y, 0, 0, 16, 16, 16, 16);
			}

			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, Component.translatable("ds.gui.skins.tooltip.wiki"), p_230443_2_, p_230443_3_);
			}
		});

		addRenderableWidget(new HelpButton(startX + 128 + imageWidth / 2 - 8 - 25, startY + 128 + 30, 16, 16, "ds.gui.skins.tooltip.help", 1));

		addRenderableWidget(new Button(startX - 60, startY + 128, 90, 20, Component.translatable("ds.gui.skins.yours"), button -> {
			playerName = minecraft.player.getGameProfile().getName();
			setTextures();
		}){
			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, Component.translatable("ds.gui.skins.tooltip.yours"), p_230443_2_, p_230443_3_);
			}
		});

		addRenderableWidget(new Button(startX + 35, startY + 128, 60, 20, Component.translatable("ds.gui.skins.random"), button -> {
			ArrayList<Pair<DragonLevel, String>> skins = new ArrayList<>();
			ArrayList<String> users = new ArrayList<>();
			Random random = new Random();

			for(Map.Entry<DragonLevel, ArrayList<String>> ent : DragonSkins.SKIN_USERS.entrySet()){
				for(String user : ent.getValue()){
					skins.add(Pair.of(ent.getKey(), user));

					if(!users.contains(user)){
						users.add(user);
					}
				}
			}

			skins.removeIf(c -> seenSkins.contains(c.second));
			if(skins.size() > 0){
				Pair<DragonLevel, String> skin = skins.get(random.nextInt(skins.size()));

				if(skin != null){
					level = skin.first;
					playerName = skin.second;

					seenSkins.add(skin.second);

					if(seenSkins.size() >= users.size() / 2){
						seenSkins.remove(0);
					}

					//					executor.execute(() -> setTextures());
					setTextures();
				}
			}
		}){
			@Override
			public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
				TooltipRendering.drawHoveringText(p_230443_1_, Component.translatable("ds.gui.skins.tooltip.random"), p_230443_2_, p_230443_3_);
			}
		});

		addRenderableWidget(new Button(startX + 90, startY + 10, 11, 17, Component.empty(), button -> {
			int pos = Mth.clamp(level.ordinal() + 1, 0, DragonLevel.values().length - 1);
			level = DragonLevel.values()[pos];

			//			executor.execute(() -> setTextures());
			setTextures();
		}){
			@Override
			public void renderButton(PoseStack stack, int mouseX, int mouseY, float p_230431_4_){
				RenderSystem.setShaderTexture(0, ClientMagicHUDHandler.widgetTextures);

				if(isHoveredOrFocused()){
					blit(stack, x, y, 66 / 2, 222 / 2, 11, 17, 128, 128);
				}else{
					blit(stack, x, y, 44 / 2, 222 / 2, 11, 17, 128, 128);
				}
			}
		});

		addRenderableWidget(new Button(startX - 70, startY + 10, 11, 17, Component.empty(), button -> {
			int pos = Mth.clamp(level.ordinal() - 1, 0, DragonLevel.values().length - 1);
			level = DragonLevel.values()[pos];

			//			executor.execute(() -> setTextures());
			setTextures();
		}){
			@Override
			public void renderButton(PoseStack stack, int mouseX, int mouseY, float p_230431_4_){
				RenderSystem.setShaderTexture(0, ClientMagicHUDHandler.widgetTextures);

				if(isHoveredOrFocused()){
					blit(stack, x, y, 22 / 2, 222 / 2, 11, 17, 128, 128);
				}else{
					blit(stack, x, y, 0, 222 / 2, 11, 17, 128, 128);
				}
			}
		});
	}

	public void setTextures(){
		loading = true;

		ResourceLocation skinTexture = DragonSkins.getPlayerSkin(playerName + "_" + level.name);
		ResourceLocation glowTexture = null;
		boolean defaultSkin = false;

		if(!DragonSkins.renderStage(minecraft.player, level) && playerName == minecraft.player.getGameProfile().getName() || skinTexture == null){
			skinTexture = null;
			defaultSkin = true;
		}

		if(skinTexture != null){
			glowTexture = DragonSkins.getPlayerGlow(playerName + "_" + level.name);
		}

		SkinsScreen.glowTexture = glowTexture;
		SkinsScreen.skinTexture = skinTexture;

		if(Objects.equals(lastPlayerName, playerName) || lastPlayerName == null){
			zoom = level.size;
		}

		noSkin = defaultSkin;
		loading = false;
		lastPlayerName = playerName;
	}

	private void confirmLink(boolean p_231162_1_){
		if(p_231162_1_){
			openLink(clickedLink);
		}

		clickedLink = null;
		minecraft.setScreen(this);
	}

	private void openLink(URI p_231156_1_){
		Util.getPlatform().openUri(p_231156_1_);
	}

	@Override
	public boolean isPauseScreen(){
		return false;
	}

	@Override
	public boolean mouseDragged(double x1, double y1, int p_231045_5_, double x2, double y2){
		xRot -= x2 / 5;
		yRot -= y2 / 5;

		//		xRot = MathHelper.clamp(xRot, -17, 17);
		//		yRot = MathHelper.clamp(yRot, -17, 17);

		return super.mouseDragged(x1, y1, p_231045_5_, x2, y2);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount){
		zoom += (float)amount;
		zoom = Mth.clamp(zoom, 10, 80);

		return true;
	}
}