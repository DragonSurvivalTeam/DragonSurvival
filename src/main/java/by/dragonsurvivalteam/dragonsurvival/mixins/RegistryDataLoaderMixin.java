package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ConditionalOps;
import com.google.gson.JsonElement;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderMixin {
    @Redirect(
            method = "loadRegistryContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/crafting/conditions/ICondition;shouldRegisterEntry(Lcom/google/gson/JsonElement;)Z",
                    remap = false
            )
    )
    private static boolean dragonSurvival$acceptNeoForgeConditions(final JsonElement json) {
        return ConditionalOps.shouldRegisterEntry(json);
    }
}
