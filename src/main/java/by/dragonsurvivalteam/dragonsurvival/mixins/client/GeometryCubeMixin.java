package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.FlatCubeBakeState;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.loading.definition.geometry.GeometryBone;
import com.geckolib.loading.definition.geometry.GeometryCube;
import com.geckolib.loading.definition.geometry.GeometryDescription;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GeometryCube.class)
public abstract class GeometryCubeMixin {
    private static final float FLAT_FACE_DEPTH_OFFSET = 0.0001F;

    @Shadow(remap = false)
    public abstract Vec3 size();

    @Inject(method = "fixZeroSizeCube", at = @At("HEAD"), cancellable = true, remap = false)
    private void dragonSurvival$allowFlatCubes(final Vec3 size, final CallbackInfoReturnable<Vec3> cir) {
        if (FlatCubeBakeState.isDragonSurvivalModelBake()) {
            cir.setReturnValue(size);
        }
    }

    @Inject(method = "bake", at = @At("RETURN"), remap = false)
    private void dragonSurvival$removeDuplicateFlatCubeFaces(
        final GeometryBone bone,
        final GeometryDescription geometry,
        final CallbackInfoReturnable<GeoCube> cir
    ) {
        if (!FlatCubeBakeState.isDragonSurvivalModelBake()) {
            return;
        }

        final GeoCube cube = cir.getReturnValue();
        if (cube == null) {
            return;
        }

        final GeoQuad[] quads = cube.quads();
        if (quads == null || quads.length < 6) {
            return;
        }

        final Vec3 size = size();
        if (size == null) {
            return;
        }

        if (size.x == 0 && quads[1] != null) {
            quads[1] = dragonSurvival$offsetQuad(quads[1], FLAT_FACE_DEPTH_OFFSET, 0, 0);
        }

        if (size.z == 0 && quads[3] != null) {
            quads[3] = dragonSurvival$offsetQuad(quads[3], 0, 0, FLAT_FACE_DEPTH_OFFSET);
        }

        if (size.y == 0 && quads[5] != null) {
            quads[5] = dragonSurvival$offsetQuad(quads[5], 0, -FLAT_FACE_DEPTH_OFFSET, 0);
        }
    }

    private static GeoQuad dragonSurvival$offsetQuad(final GeoQuad quad, final float offsetX, final float offsetY, final float offsetZ) {
        final GeoVertex[] vertices = quad.vertices();
        final GeoVertex[] offsetVertices = new GeoVertex[vertices.length];

        for (int i = 0; i < vertices.length; i++) {
            final GeoVertex vertex = vertices[i];
            offsetVertices[i] = new GeoVertex(
                vertex.posX() + offsetX,
                vertex.posY() + offsetY,
                vertex.posZ() + offsetZ,
                vertex.texU(),
                vertex.texV()
            );
        }

        return new GeoQuad(offsetVertices, quad.normalX(), quad.normalY(), quad.normalZ(), quad.direction());
    }
}
