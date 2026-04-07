package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class GenericArrowRenderer extends ArrowRenderer<GenericArrowEntity, GenericArrowRenderer.State> {
    public GenericArrowRenderer(final EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(final GenericArrowEntity entity, final State state, final float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        Identifier resource = entity.getResource();
        state.texture = Identifier.fromNamespaceAndPath(resource.getNamespace(), "textures/entity/projectiles/" + resource.getPath() + ".png");
    }

    @Override
    protected @NotNull Identifier getTextureLocation(final State state) {
        return state.texture;
    }

    public static class State extends ArrowRenderState {
        private Identifier texture = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/projectiles/arrow.png");
    }
}
