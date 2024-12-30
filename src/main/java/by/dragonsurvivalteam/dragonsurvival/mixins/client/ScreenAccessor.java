package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("font")
    Font dragonSurvival$getFont();

    @Accessor("children")
    List<GuiEventListener> dragonSurvival$children();

    @Invoker("removeWidget")
    void dragonSurvival$removeWidget(final GuiEventListener listener);

    @Invoker("addRenderableWidget")
    @SuppressWarnings("UnusedReturnValue") // ignore
    <T extends GuiEventListener & Renderable & NarratableEntry> T dragonSurvival$addRenderableWidget(final T widget);

    @Invoker("addRenderableOnly")
    <T extends Renderable> T dragonSurvival$addRenderableOnly(T renderable);
}
