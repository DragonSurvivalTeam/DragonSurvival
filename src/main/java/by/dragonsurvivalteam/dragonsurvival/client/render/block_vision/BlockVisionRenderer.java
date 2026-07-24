package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import com.mojang.blaze3d.vertex.PoseStack;

public interface BlockVisionRenderer {
    void render(final BlockVisionHandler.Data data, final PoseStack pose, final int tick, final float partialTick);
}
