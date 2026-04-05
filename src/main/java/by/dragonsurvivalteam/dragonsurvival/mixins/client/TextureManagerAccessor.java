package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {
    @Accessor("byPath")
    Map<Identifier, AbstractTexture> dragonSurvival$getTexturesByPath();
}
