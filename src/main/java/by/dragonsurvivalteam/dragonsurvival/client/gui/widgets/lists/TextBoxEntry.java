package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.lists;

import by.dragonsurvivalteam.dragonsurvival.client.gui.settings.widgets.Option;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

@OnlyIn( Dist.CLIENT )
public class TextBoxEntry extends OptionListEntry{
	public final AbstractWidget widget;
	private final CategoryEntry category;
	public AbstractWidget removeButton;

	public TextBoxEntry(Option option, OptionsList optionsList, AbstractWidget widget, CategoryEntry categoryEntry){
		super(ImmutableMap.of(option, widget));
		this.widget = widget;
		category = categoryEntry;

		removeButton = new ExtendedButton(optionsList.getScrollbarPosition() - 32 - 25, 1, 50, 20, Component.empty().append("Remove"), btn -> {
			for(OptionListEntry child : optionsList.children())
				if(child.children().contains(widget)){
					optionsList.removeEntry(child);
					optionsList.scroll(-child.getHeight());
					return;
				}
		});
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks){
		widget.setY(pTop);
		widget.visible = getHeight() != 0 && visible;
		widget.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);

		removeButton.setY(pTop);
		removeButton.visible = getHeight() != 0 && visible;
		removeButton.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	public int getHeight(){
		if(category != null){
			CategoryEntry entry = category.parent;
			while(entry != null)
				if(!entry.enabled){
					return 0;
				}else{
					entry = entry.parent;
				}
		}

		return category == null || category.enabled ? 20 : 0;
	}

	@Override
	public List<? extends GuiEventListener> children(){
		return ImmutableList.of(widget, removeButton);
	}

	@Override
	public List<? extends NarratableEntry> narratables(){
		return Collections.emptyList();
	}
}