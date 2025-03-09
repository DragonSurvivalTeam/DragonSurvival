package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import com.google.common.collect.ImmutableList;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
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

    public DragonUIRenderComponent(Screen screen, int x, int y, int xSize, int ySize, Supplier<DragonEntity> dragonGetter) {
        this.screen = screen;
        this.x = x;
        this.y = y;
        width = xSize;
        height = ySize;
        getter = dragonGetter;
    }

    @Override
    public void render(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        if (isMouseOver(pMouseX, pMouseY)) {
            screen.setFocused(this);
        }

        float scale = zoom;

        // We need to translate this backwards with the poseStack as renderEntityInInventory pushes the poseStack forward
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, -200); // We chose -200 here as the background is translated -300, and we don't want to clip with it

        Quaternionf quaternion = Axis.ZP.rotationDegrees(180);
        quaternion.mul(Axis.XP.rotationDegrees(yRot * 10));
        quaternion.rotateY((float) Math.toRadians(180 - xRot * 10));
        InventoryScreen.renderEntityInInventory(guiGraphics, x + (float) width / 2 + xOffset, y + height - 30 + yOffset, (int) scale, new Vector3f(0, 0, 0), quaternion, null, getter.get());

        guiGraphics.pose().popPose();
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
    public boolean mouseDragged(double x1, double y1, int rightClick, double x2, double y2) {
        if (isMouseOver(x1, y1)) {
            if (rightClick == 0) {
                xRot -= (float) (x2 / 5.0);
                yRot -= (float) (y2 / 5.0);
            } else if (rightClick == 1) {
                xOffset += (float) (x2);
                yOffset += (float) (y2);

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