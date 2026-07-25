package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(FileToIdConverter.class)
public abstract class FileToIdConverterMixin {
    private static final Map<String, String> DRAGON_SURVIVAL$SINGULAR_DIRECTORIES = Map.of(
            "advancements", "advancement",
            "functions", "function",
            "item_modifiers", "item_modifier",
            "loot_tables", "loot_table",
            "predicates", "predicate",
            "recipes", "recipe",
            "structures", "structure"
    );

    @Shadow @Final private String prefix;
    @Shadow @Final private String extension;

    @Inject(method = "listMatchingResources", at = @At("RETURN"), cancellable = true)
    private void dragonSurvival$includeSingularResources(
            final ResourceManager resourceManager,
            final CallbackInfoReturnable<Map<ResourceLocation, Resource>> callback
    ) {
        String singular = DRAGON_SURVIVAL$SINGULAR_DIRECTORIES.get(prefix);
        if (singular == null) {
            return;
        }

        Map<ResourceLocation, Resource> combined = new HashMap<>();
        resourceManager.listResources(singular, id -> id.getPath().endsWith(extension))
                .forEach((id, resource) -> combined.put(toPluralPath(id, singular), resource));
        combined.putAll(callback.getReturnValue());
        callback.setReturnValue(combined);
    }

    @Inject(method = "listMatchingResourceStacks", at = @At("RETURN"), cancellable = true)
    private void dragonSurvival$includeSingularResourceStacks(
            final ResourceManager resourceManager,
            final CallbackInfoReturnable<Map<ResourceLocation, List<Resource>>> callback
    ) {
        String singular = DRAGON_SURVIVAL$SINGULAR_DIRECTORIES.get(prefix);
        if (singular == null) {
            return;
        }

        Map<ResourceLocation, List<Resource>> combined = new HashMap<>();
        resourceManager.listResourceStacks(singular, id -> id.getPath().endsWith(extension))
                .forEach((id, resources) -> combined.put(toPluralPath(id, singular), resources));
        combined.putAll(callback.getReturnValue());
        callback.setReturnValue(combined);
    }

    private ResourceLocation toPluralPath(final ResourceLocation id, final String singular) {
        return id.withPath(prefix + id.getPath().substring(singular.length()));
    }
}
