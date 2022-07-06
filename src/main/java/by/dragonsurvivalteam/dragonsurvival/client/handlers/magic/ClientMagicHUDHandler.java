package by.dragonsurvivalteam.dragonsurvival.client.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.common.magic.common.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.common.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonType;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;

import java.awt.Color;

public class ClientMagicHUDHandler{
	public static final ResourceLocation widgetTextures = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/widgets.png");
	public static final ResourceLocation castBars = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/cast_bars.png");

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = Dist.CLIENT, category = {"ui", "magic"}, key = "casterBarXPos", comment = "Offset the x position of the cast bar in relation to its normal position" )
	public static Integer castbarXOffset = 0;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = Dist.CLIENT, category = {"ui", "magic"}, key = "casterBarYPos", comment = "Offset the y position of the cast bar in relation to its normal position" )
	public static Integer castbarYOffset = 0;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = Dist.CLIENT, category = {"ui", "magic"}, key = "skillbarXOffset", comment = "Offset the x position of the magic skill bar in relation to its normal position" )
	public static Integer skillbarXOffset = 0;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = Dist.CLIENT, category = {"ui", "magic"}, key = "skillbarYOffset", comment = "Offset the y position of the magic skill bar in relation to its normal position" )
	public static Integer skillbarYOffset = 0;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = Dist.CLIENT, category = {"ui", "magic"}, key = "manabarXOffset", comment = "Offset the x position of the mana bar in relation to its normal position" )
	public static Integer manabarXOffset = 0;

	@ConfigRange( min = -1000, max = 1000 )
	@ConfigOption( side = Dist.CLIENT, category = {"ui", "magic"}, key = "manabarYOffset", comment = "Offset the y position of the mana bar in relation to its normal position" )
	public static Integer manabarYOffset = 0;

	public static void cancelExpBar(ForgeIngameGui gui, PoseStack mStack, float partialTicks, int width, int height){
		Player playerEntity = Minecraft.getInstance().player;
		if(Minecraft.getInstance().options.hideGui || !gui.shouldDrawSurvivalElements() || !Minecraft.getInstance().gameMode.hasExperience()){
			return;
		}
		int x = width / 2 - 91;

		if(!ServerConfig.consumeEXPAsMana || !DragonUtils.isDragon(playerEntity)){
			ForgeIngameGui.EXPERIENCE_BAR_ELEMENT.render(gui, mStack, partialTicks, width, height);
			return;
		}

		DragonStateProvider.getCap(playerEntity).ifPresent(cap -> {
			ActiveDragonAbility ability = cap.getMagic().getAbilityFromSlot(cap.getMagic().getSelectedAbilitySlot());
			if(ability == null){
				return;
			}

			if(ManaHandler.getCurrentMana(playerEntity) < ability.getManaCost() && ((ManaHandler.getCurrentMana(playerEntity) + (playerEntity.totalExperience / 10) >= ability.getManaCost()) || playerEntity.experienceLevel > 0)){
				Window window = Minecraft.getInstance().getWindow();

				int screenWidth = window.getGuiScaledWidth();
				int screenHeight = window.getGuiScaledHeight();

				RenderSystem.setShaderTexture(0, widgetTextures);
				PoseStack stack = mStack;
				int i = Minecraft.getInstance().player.getXpNeededForNextLevel();
				if(i > 0){
					int j = 182;
					int k = (int)(Minecraft.getInstance().player.experienceProgress * 183.0F);
					int l = screenHeight - 32 + 3;
					blit(stack, x, l, 0, 164, 182, 5);
					if(k > 0){
						blit(stack, x, l, 0, 169, k, 5);
					}
				}

				if(Minecraft.getInstance().player.experienceLevel > 0){
					String s = "" + Minecraft.getInstance().player.experienceLevel;
					int i1 = (screenWidth - Minecraft.getInstance().font.width(s)) / 2;
					int j1 = screenHeight - 31 - 4;
					Minecraft.getInstance().font.draw(stack, s, (float)(i1 + 1), (float)j1, 0);
					Minecraft.getInstance().font.draw(stack, s, (float)(i1 - 1), (float)j1, 0);
					Minecraft.getInstance().font.draw(stack, s, (float)i1, (float)(j1 + 1), 0);
					Minecraft.getInstance().font.draw(stack, s, (float)i1, (float)(j1 - 1), 0);
					Minecraft.getInstance().font.draw(stack, s, (float)i1, (float)j1, new Color(243, 48, 59).getRGB());
					return;
				}
			}
		});

		gui.renderExperienceBar(mStack, x);
	}

	public static void blit(PoseStack p_238474_1_, int p_238474_2_, int p_238474_3_, int p_238474_4_, int p_238474_5_, int p_238474_6_, int p_238474_7_){
		Screen.blit(p_238474_1_, p_238474_2_, p_238474_3_, 0, (float)p_238474_4_, (float)p_238474_5_, p_238474_6_, p_238474_7_, 256, 256);
	}

	public static void renderAbilityHud(ForgeIngameGui gui, PoseStack mStack, float partialTicks, int width, int height){
		if(Minecraft.getInstance().options.hideGui){
			return;
		}

		Player playerEntity = Minecraft.getInstance().player;

		if(playerEntity == null || !DragonUtils.isDragon(playerEntity) || playerEntity.isSpectator()){
			return;
		}

		DragonStateProvider.getCap(playerEntity).ifPresent(cap -> {
			mStack.pushPose();
			int count = 4;
			int sizeX = 20;
			int sizeY = 20;
			boolean rightSide = true;

			int posX = rightSide ? width - (sizeX * count) - 20 : (sizeX * count) + 20;
			int posY = height - (sizeY);

			posX += skillbarXOffset;
			posY += skillbarYOffset;

			if(cap.getMagic().renderAbilityHotbar()){
				RenderSystem.setShaderTexture(0, new ResourceLocation("textures/gui/widgets.png"));
				Screen.blit(mStack, posX, posY - 2, 0, 0, 0, 41, 22, 256, 256);
				Screen.blit(mStack, posX + 41, posY - 2, 0, 141, 0, 41, 22, 256, 256);

				for(int x = 0; x < count; x++){
					ActiveDragonAbility ability = cap.getMagic().getAbilityFromSlot(x);

					if(ability != null && ability.getIcon() != null){
						RenderSystem.setShaderTexture(0, ability.getIcon());
						Screen.blit(mStack, posX + (x * sizeX) + 3, posY + 1, 0, 0, 16, 16, 16, 16);

						if(ability.getMaxCooldown() > 0 && ability.getCooldown() > 0 && ability.getMaxCooldown() != ability.getCooldown()){
							float f = Mth.clamp((float)ability.getCooldown() / (float)ability.getMaxCooldown(), 0, 1);
							int boxX = posX + (x * sizeX) + 3;
							int boxY = posY + 1;
							int offset = 16 - (16 - (int)(f * 16));
							int color = new Color(0.15F, 0.15F, 0.15F, 0.75F).getRGB();
							int fColor = ability.errorTicks > 0 ? new Color(1F, 0F, 0F, 0.75F).getRGB() : color;
							Gui.fill(mStack, boxX, boxY, boxX + 16, boxY + (offset), fColor);
						}
					}

					if(ability.errorTicks > 0){
						ability.errorTicks--;

						if(ability.errorTicks <= 0){
							ability.errorMessage = null;
						}
					}
				}

				RenderSystem.setShaderTexture(0, new ResourceLocation("textures/gui/widgets.png"));
				Screen.blit(mStack, posX + (sizeX * cap.getMagic().getSelectedAbilitySlot()) - 1, posY - 3, 2, 0, 22, 24, 24, 256, 256);

				RenderSystem.setShaderTexture(0, widgetTextures);

				int maxMana = ManaHandler.getMaxMana(playerEntity);
				int curMana = ManaHandler.getCurrentMana(playerEntity);

				int manaX = rightSide ? width - (sizeX * count) - 20 : (sizeX * count) + 20;
				int manaY = height - (sizeY);

				manaX += manabarXOffset;
				manaY += manabarYOffset;

				for(int i = 0; i < 1 + Math.ceil(maxMana / 10.0); i++){
					for(int x = 0; x < 10; x++){
						int manaSlot = (i * 10) + x;
						if(manaSlot < maxMana){
							boolean goodCondi = ManaHandler.isPlayerInGoodConditions(playerEntity);
							int condiXPos = cap.getType() == DragonType.SEA ? 0 : cap.getType() == DragonType.FOREST ? 18 : 36;
							int xPos = curMana <= manaSlot ? (goodCondi ? condiXPos + 72 : 54) : cap.getType() == DragonType.SEA ? 0 : cap.getType() == DragonType.FOREST ? 18 : 36;
							float rescale = 2.15F;
							Screen.blit(mStack, manaX + (x * (int)(18 / rescale)), manaY - 12 - (i * ((int)(18 / rescale) + 1)), xPos / rescale, 204 / rescale, (int)(18 / rescale), (int)(18 / rescale), (int)(256 / rescale), (int)(256 / rescale));
						}
					}
				}
			}


			ActiveDragonAbility ability = cap.getMagic().getAbilityFromSlot(cap.getMagic().getSelectedAbilitySlot());

			if(ability.getCurrentCastTimer() > 0){
				mStack.pushPose();
				mStack.scale(0.5F, 0.5F, 0);

				int yPos1 = cap.getType() == DragonType.CAVE ? 0 : cap.getType() == DragonType.FOREST ? 47 : 94;
				int yPos2 = cap.getType() == DragonType.CAVE ? 142 : cap.getType() == DragonType.FOREST ? 147 : 152;

				float perc = Math.min((float)ability.getCurrentCastTimer() / (float)ability.getCastingTime(), 1);

				int startX = (width / 2) - 49 + castbarXOffset;
				int startY = height - 96 + castbarYOffset;

				mStack.translate(startX, startY, 0);


				RenderSystem.setShaderTexture(0, castBars);
				Screen.blit(mStack, startX, startY, 0, yPos1, 196, 47, 256, 256);
				Screen.blit(mStack, startX + 2, startY + 41, 0, yPos2, (int)((191) * perc), 4, 256, 256);

				RenderSystem.setShaderTexture(0, ability.getIcon());
				Screen.blit(mStack, startX + 78, startY + 3, 0, 0, 36, 36, 36, 36);

				mStack.popPose();
			}

			if(ability.errorTicks > 0){
				Minecraft.getInstance().font.draw(mStack, ability.errorMessage, (width / 2) - (Minecraft.getInstance().font.width(ability.errorMessage) / 2), height - 70, 0);
			}
			mStack.popPose();
		});
	}
}