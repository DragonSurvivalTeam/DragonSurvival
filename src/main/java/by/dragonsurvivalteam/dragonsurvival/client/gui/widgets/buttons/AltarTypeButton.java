package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class AltarTypeButton extends Button {
	private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_altar_icons.png");
	private final DragonAltarScreen gui;
	public AbstractDragonType type;

	public AltarTypeButton(DragonAltarScreen gui, AbstractDragonType type, int x, int y){
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
			// TODO: This is a hack to get rid of the format error that happens when parsing these altar dragon info strings; not sure how to fix it without modifying the data everywhere
			string = string.replace("Format error: ", "");
			if (string.equals("%s")) {
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
				cap.revertToHumanForm(player, false);
				PacketDistributor.sendToServer(new SyncComplete.Data(player.getId(), cap.serializeNBT(player.registryAccess())));
			});
			player.closeContainer();
		}else
			Minecraft.getInstance().setScreen(new DragonEditorScreen(gui, type));
	}
}