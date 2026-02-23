package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericBallEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GenericBallRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<GenericBallEntity, R> {

    // Data tickets
    public static DataTicket<Identifier> MODEL_RESOURCE = DataTicket.create("ballModel", Identifier.class);
    public static DataTicket<Identifier> TEXTURE_RESOURCE = DataTicket.create("ballTexture", Identifier.class);

    public GenericBallRenderer(final EntityRendererProvider.Context renderManager, final GeoModel<GenericBallEntity> model) {
        super(renderManager, model);
    }

    @Override
    protected int getBlockLightLevel(@NotNull final GenericBallEntity entity, @NotNull final BlockPos position) {
        return 15;
    }

    @Override
    public void addRenderData(GenericBallEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(MODEL_RESOURCE, animatable.getModelResource());
        renderState.addGeckolibData(TEXTURE_RESOURCE, animatable.getTextureResource());
    }
}
