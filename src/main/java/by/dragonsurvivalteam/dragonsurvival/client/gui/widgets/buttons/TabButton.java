package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import static by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonInventory.SendOpenDragonInventoryAndMaintainCursorPosition;

import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.AbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.InventoryScreenHandler;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.SkinsScreen;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;


public class TabButton extends Button {
	public enum TabType {
		INVENTORY,
		ABILITY,
		GITHUB_REMINDER,
		SKINS
	}

	private final TabType tabType;
	private final Screen parent;

	public TabButton(int x, int y, TabType tabType, Screen parent){
		super(x, y, 28, 32, Component.empty(), button -> {}, DEFAULT_NARRATION);
		this.tabType = tabType;
		this.parent = parent;

		setTooltip(Tooltip.create(Component.translatable("ds.gui.tab_button." + tabType.ordinal())));
	}

	private boolean setInventoryScreen(Screen sourceScreen) {
		if (sourceScreen instanceof InventoryScreen) {
			Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
			PacketDistributor.sendToServer(new RequestOpenInventory.Data());
			return true;
		} else if (sourceScreen instanceof DragonInventoryScreen) {
			SendOpenDragonInventoryAndMaintainCursorPosition();
			return true;
		}

		return false;
	}

	@Override
	public void onPress(){
		if(!isCurrent())
			switch(tabType){
				case INVENTORY -> {
					boolean setSuccessfully = false;
					if(parent instanceof AbilityScreen){
						if(((AbilityScreen)parent).sourceScreen != null){
							setSuccessfully = setInventoryScreen(((AbilityScreen)parent).sourceScreen);
						}
					} else if(parent instanceof SkinsScreen){
						if(((SkinsScreen)parent).sourceScreen != null){
							setSuccessfully = setInventoryScreen(((SkinsScreen)parent).sourceScreen);
						}
					}

					if(!setSuccessfully) {
						if(InventoryScreenHandler.dragonInventory){
							SendOpenDragonInventoryAndMaintainCursorPosition();
						} else {
							Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
							PacketDistributor.sendToServer(new RequestOpenInventory.Data());
						}
					}
				}
				case ABILITY -> Minecraft.getInstance().setScreen(new AbilityScreen(parent));
				case SKINS -> Minecraft.getInstance().setScreen(new SkinsScreen(parent));
			}
	}

	public boolean isCurrent(){
		return switch(tabType){
			case INVENTORY -> parent instanceof DragonInventoryScreen || parent instanceof InventoryScreen;
			case ABILITY -> parent instanceof AbilityScreen;
			case SKINS -> parent instanceof SkinsScreen;
			default -> false;
		};
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float p_230431_4_){
		if (isCurrent()) {
			guiGraphics.blit(MagicHUD.widgetTextures, getX(), getY(), tabType == TabType.INVENTORY ? 0 : 28, 0, 28, 32);
		} else if (isHovered()) {
			guiGraphics.blit(MagicHUD.widgetTextures, getX(), getY(), 84, 0, 28, 32);
		} else {
			guiGraphics.blit(MagicHUD.widgetTextures, getX(), getY(), 56, 0, 28, 32);
		}

		if (isHovered() || isCurrent()) {
			guiGraphics.blit(MagicHUD.widgetTextures, getX() + 2, getY() + 2 + (isCurrent() ? 2 : 0), tabType.ordinal() * 24, 67, 24, 24);
		} else {
			guiGraphics.blit(MagicHUD.widgetTextures, getX() + 2, getY() + 2 + (isCurrent() ? 2 : 0), tabType.ordinal() * 24, 41, 24, 24);
		}
	}
}