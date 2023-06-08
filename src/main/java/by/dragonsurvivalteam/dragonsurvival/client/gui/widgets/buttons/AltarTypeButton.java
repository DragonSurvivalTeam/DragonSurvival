package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonAltarGUI;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.utils.TooltipRender;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.util.TooltipRendering;
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
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AltarTypeButton extends Button implements TooltipRender{
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_altar_icons.png");
	private final DragonAltarGUI gui;
	public AbstractDragonType type;
	private boolean atTheTopOrBottom;

	public AltarTypeButton(DragonAltarGUI gui, AbstractDragonType type, int x, int y){
		super(x, y, 49, 147, null, null);
		this.gui = gui;
		this.type = type;
	}

	@Override
	public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY){
		if(atTheTopOrBottom) TooltipRendering.drawHoveringText(pPoseStack, altarDragonInfoLocalized(type == null ? "human" : type.getTypeName().toLowerCase() + "_dragon", type == null ? Collections.emptyList() : DragonFoodHandler.getSafeEdibleFoods(type)), pMouseX, pMouseY);
	}

	private ArrayList<Component> altarDragonInfoLocalized(String dragonType, List<Item> foodList){
		ArrayList<Component> info = new ArrayList<>();
		Component foodInfo = Component.empty();

		if(Screen.hasShiftDown()){
			if(!Objects.equals(dragonType, "human")){
				String food = "";
				for(Item item : foodList)
					food += item.getName(new ItemStack(item)).getString() + "; ";
				foodInfo = Component.nullToEmpty(food);
			}
		}else
			foodInfo = Component.translatable("ds.hold_shift.for_food");

		Component textComponent = Component.translatable("ds.altar_dragon_info." + dragonType, foodInfo.getString());
		String text = textComponent.getString();
		for(String s : text.split("\n"))
			info.add(Component.empty().append(s));
		return info;
	}

	@Override
	public void onPress(){
		initiateDragonForm(type);
	}

	@Override
	public void renderButton(PoseStack mStack, int mouseX, int mouseY, float p_230431_4_){
		atTheTopOrBottom = mouseY > y + 6 && mouseY < y + 26 || mouseY > y + 133 && mouseY < y + 153;
		RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

		fill(mStack, x - 1, y - 1, x + width + 1, y + height + 1, new Color(0.5f, 0.5f, 0.5f).getRGB());
		blit(mStack, x, y, (type == null ? 3 : Objects.equals(type, DragonTypes.CAVE) ? 0 : Objects.equals(type, DragonTypes.FOREST) ? 1 : Objects.equals(type, DragonTypes.SEA) ? 2 : 3) * 49, isHovered ? 0 : 147, 49, 147, 512, 512);
	}

	private void initiateDragonForm(AbstractDragonType type){
		LocalPlayer player = Minecraft.getInstance().player;

		if(player == null)
			return;

		if(type == null){
			Minecraft.getInstance().player.sendSystemMessage(Component.translatable("ds.choice_human"));

			DragonStateProvider.getCap(player).ifPresent(cap -> {
				player.level.playSound(player, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);
				cap.setType(null);
				if (!ServerConfig.saveGrowthStage) {
					cap.setSize(20F);
					cap.setHasWings(false);
					cap.setIsHiding(false);
				}
				cap.getMovementData().spinLearned = false;

				NetworkHandler.CHANNEL.sendToServer(new SyncAltarCooldown(Minecraft.getInstance().player.getId(), Functions.secondsToTicks(ServerConfig.altarUsageCooldown)));
				NetworkHandler.CHANNEL.sendToServer(new SynchronizeDragonCap(player.getId(), cap.isHiding(), cap.getType(), cap.getSize(), cap.hasWings(), 0));
				NetworkHandler.CHANNEL.sendToServer(new SyncSpinStatus(Minecraft.getInstance().player.getId(), cap.getMovementData().spinAttack, cap.getMovementData().spinCooldown, cap.getMovementData().spinLearned));
				ClientEvents.sendClientData(new RequestClientData(cap.getType(), cap.getLevel()));
			});
			player.closeContainer();
		}else
			Minecraft.getInstance().setScreen(new DragonEditorScreen(gui, type));
	}
}