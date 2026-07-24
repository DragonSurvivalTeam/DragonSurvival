package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public interface LivingRendererAccessor {
    @Accessor("layers")
    @SuppressWarnings("rawtypes")
    List<RenderLayer> dragonSurvival$getRenderLayers();

    @Invoker("shouldShowName")
    boolean dragonSurvival$callShouldShowName(LivingEntity entity);
}