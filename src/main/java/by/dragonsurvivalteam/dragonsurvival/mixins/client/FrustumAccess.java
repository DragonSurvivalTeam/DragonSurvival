package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Frustum.class)
public interface FrustumAccess {
//    /** Skip the need to create an AABB */
//    @Invoker("cubeInFrustum")
//    boolean dragonSurvival$cubeInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
