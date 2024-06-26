package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;


@Mixin( EntityRenderDispatcher.class )
public interface AccessorEntityRendererManager{
	@Accessor( "playerRenderers" )
	Map<String, EntityRenderer<? extends Player>> getPlayerRenderers();
}