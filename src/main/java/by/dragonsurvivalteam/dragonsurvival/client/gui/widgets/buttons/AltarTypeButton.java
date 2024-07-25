package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonAltarGUI;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.player.SynchronizeDragonCap;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncAltarCooldown;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AltarTypeButton extends Button {
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_altar_icons.png");
	private final DragonAltarGUI gui;
	public AbstractDragonType type;

	public AltarTypeButton(DragonAltarGUI gui, AbstractDragonType type, int x, int y){
		super(x, y, 49, 147, Component.empty(), Button::onPress, DEFAULT_NARRATION);
		this.gui = gui;
		this.type = type;
	}

	private List<Component> altarDragonInfoLocalized(final String dragonType, final List<Item> foodList) {
		String foodInfo = "";

		if (Screen.hasShiftDown()) {
			if (!Objects.equals(dragonType, "human")) {
				StringBuilder food = new StringBuilder();

				for (Item item : foodList) {
					food.append(item.getName(new ItemStack(item)).getString()).append("; ");
				}

				foodInfo = food.toString();
			}
		} else {
			foodInfo = I18n.get("ds.hold_shift.for_food");
		}

		List<Component> tooltips = new ArrayList<>();

		for (String string : I18n.get("ds.altar_dragon_info." + dragonType).split("\n")) {
			if (string.equals("{0}")) {
				string = foodInfo;
			}

			tooltips.add(Component.literal(string));
		}

		return tooltips;
	}

	@Override
	public void onPress(){
		initiateDragonForm(type);
	}

	@Override
	protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		boolean atTheTopOrBottom = mouseY > getY() + 6 && mouseY < getY() + 26 || mouseY > getY() + 133 && mouseY < getY() + 153;

		if (isHovered() && atTheTopOrBottom) {
			guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, altarDragonInfoLocalized(type == null ? "human" : type.getTypeNameLowerCase() + "_dragon", type == null ? Collections.emptyList() : DragonFoodHandler.getEdibleFoods(type)), mouseX, mouseY);
		}

		RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

		int uOffset = 3;

		if (DragonUtils.isDragonType(type, DragonTypes.CAVE)) {
			uOffset = 0;
		} else if (DragonUtils.isDragonType(type, DragonTypes.FOREST)) {
			uOffset = 1;
		} else if (DragonUtils.isDragonType(type, DragonTypes.SEA)) {
			uOffset = 2;
		}

		guiGraphics.fill(getX() - 1, getY() - 1, getX() + width, getY() + height, new Color(0.5f, 0.5f, 0.5f).getRGB());
		guiGraphics.blit(BACKGROUND_TEXTURE, getX(), getY(), uOffset * 49, isHovered ? 0 : 147, 49, 147, 512, 512);
	}

	private void initiateDragonForm(AbstractDragonType type){
		LocalPlayer player = Minecraft.getInstance().player;

		if(player == null)
			return;

		if(type == null){
			Minecraft.getInstance().player.sendSystemMessage(Component.translatable("ds.choice_human"));

			DragonStateProvider.getCap(player).ifPresent(cap -> {
				player.level().playSound(player, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);

				if (ServerConfig.saveGrowthStage) {
					cap.setSavedDragonSize(cap.getTypeName(), cap.getSize());
				}

				cap.setType(null);
				cap.setBody(null, player);
				cap.setSize(20F, player);
				cap.setIsHiding(false);

				if (!ServerConfig.saveAllAbilities) {
					cap.getMovementData().spinLearned = false;
					cap.setHasFlight(false);
				}

				NetworkHandler.CHANNEL.sendToServer(new SyncAltarCooldown(Minecraft.getInstance().player.getId(), Functions.secondsToTicks(ServerConfig.altarUsageCooldown)));
				NetworkHandler.CHANNEL.sendToServer(new SynchronizeDragonCap(player.getId(), cap.isHiding(), cap.getType(), cap.getBody(), cap.getSize(), cap.hasFlight(), 0));
				NetworkHandler.CHANNEL.sendToServer(new SyncSpinStatus(Minecraft.getInstance().player.getId(), cap.getMovementData().spinAttack, cap.getMovementData().spinCooldown, cap.getMovementData().spinLearned));
				ClientEvents.sendClientData(new RequestClientData(cap.getType(), cap.getBody(), cap.getLevel()));
			});
			player.closeContainer();
		}else
			Minecraft.getInstance().setScreen(new DragonEditorScreen(gui, type));
	}
}