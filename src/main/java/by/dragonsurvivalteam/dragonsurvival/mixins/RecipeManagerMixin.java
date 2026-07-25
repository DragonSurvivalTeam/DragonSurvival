package by.dragonsurvivalteam.dragonsurvival.mixins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void dragonSurvival$copyNeoForgeConditions(
            final Map<ResourceLocation, JsonElement> recipes,
            final ResourceManager resourceManager,
            final ProfilerFiller profiler,
            final CallbackInfo callback
    ) {
        for (JsonElement element : recipes.values()) {
            if (element instanceof JsonObject recipe
                    && recipe.has("neoforge:conditions")
                    && !recipe.has("conditions")) {
                recipe.add("conditions", toForgeConditions(recipe.get("neoforge:conditions")));
            }
        }
    }

    private static JsonElement toForgeConditions(final JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray result = new JsonArray();
            element.getAsJsonArray().forEach(value -> result.add(toForgeConditions(value)));
            return result;
        }
        if (!element.isJsonObject()) {
            return element.deepCopy();
        }

        JsonObject result = new JsonObject();
        for (var entry : element.getAsJsonObject().entrySet()) {
            JsonElement value = toForgeConditions(entry.getValue());
            if (entry.getKey().equals("type") && value.isJsonPrimitive()) {
                String type = value.getAsString();
                if (type.startsWith("neoforge:")) {
                    value = new JsonPrimitive("forge:" + type.substring("neoforge:".length()));
                }
            }
            result.add(entry.getKey(), value);
        }
        return result;
    }
}
