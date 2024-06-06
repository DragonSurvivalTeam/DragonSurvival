package by.dragonsurvivalteam.dragonsurvival.mixins;

import java.util.List;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin( LivingEntityRenderer.class )
public interface AccessorLivingRenderer{
	@Accessor( "layers" )
	List<RenderLayer> getRenderLayers();
	@Invoker( "shouldShowName" )
	boolean callShouldShowName(LivingEntity p_177070_1_);
}