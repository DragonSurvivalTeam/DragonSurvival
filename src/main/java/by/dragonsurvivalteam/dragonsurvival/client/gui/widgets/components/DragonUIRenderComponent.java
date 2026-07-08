package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

public class DragonUIRenderComponent extends AbstractContainerEventHandler implements Renderable {
    private final Screen screen;
    private final Supplier<DragonEntity> getter;
    public float yRot, xRot;
    public float xOffset, yOffset;
    public float zoom;
    public int x, y, width, height;
    private @Nullable Identifier textureOverride;
    private @Nullable Identifier glowTextureOverride;

    public DragonUIRenderComponent(Screen screen, int x, int y, int xSize, int ySize, Supplier<DragonEntity> dragonGetter) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        width = xSize;
        height = ySize;
        getter = dragonGetter;
    }

    public void setTextureOverrides(final @Nullable Identifier textureOverride, final @Nullable Identifier glowTextureOverride) {
        this.textureOverride = textureOverride;
        this.glowTextureOverride = glowTextureOverride;
    }

    public void setZoom(final Holder<DragonStage> dragonStage) {
        zoom = (float) (0.4 * dragonStage.value().growthRange().min() + 20);
    }

    @Override
    public void extractRenderState(@NotNull final GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTicks) {
        if (isMouseOver(pMouseX, pMouseY)) {
            screen.setFocused(this);
        }

        float scale = zoom;

        // We need to translate this backwards with the poseStack as renderEntityInInventory pushes the poseStack forward
        Quaternionf rotation = Axis.ZP.rotationDegrees(180);
        rotation.mul(Axis.XP.rotationDegrees(yRot * 10));
        rotation.rotateY((float) Math.toRadians(180 - xRot * 10));

        EntityRenderState renderState = DragonRenderer.createUIRenderState(getter.get(), pPartialTicks, 0.0F, 0.0F, 0.0F, null, textureOverride, glowTextureOverride);

        // Not sure what changed about the scale of UI elements that forces me to divide by 50 here for translation, but it is good enough for now
        Vector3f translation = new Vector3f(xOffset / 50.f, yOffset /  50.f, 0.0F);

        // Add some arbitrary padding on the scissor (+/- 100)
        graphics.entity(renderState, scale, translation, rotation, null, x - 100, y - 100, x + width + 100, y + height + 100);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return pMouseX >= x && pMouseX <= x + width && pMouseY >= y && pMouseY <= y + height;
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return ImmutableList.of();
    }

    @Override
    public boolean mouseDragged(@NotNull MouseButtonEvent event, double mouseX, double mouseY) {
        if (isMouseOver(event.x(), event.y())) {
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                xRot -= (float) (mouseX / 5.0);
                yRot -= (float) (mouseY / 5.0);
            } else if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
                xOffset += (float) (mouseX);
                yOffset += (float) (mouseY);

                xOffset = Mth.clamp(xOffset, -((float) width / 2.f), (float) width / 2.f);
                yOffset = Mth.clamp(yOffset, -((float) height / 2.f), (float) height / 2.f);
            }

            return true;
        } else {
            setDragging(false);
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (isMouseOver(pMouseX, pMouseY)) {
            zoom += (float) pScrollY * 2;
            zoom = Mth.clamp(zoom, 10, 100);
            return true;
        }

        return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }
}
