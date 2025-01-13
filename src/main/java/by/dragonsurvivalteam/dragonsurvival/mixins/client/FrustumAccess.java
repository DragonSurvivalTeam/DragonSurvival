package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Frustum.class)
public interface FrustumAccess {
    @Invoker("cubeInFrustum") // Skip the need to create an AABB
    boolean dragonSurvival$cubeInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
