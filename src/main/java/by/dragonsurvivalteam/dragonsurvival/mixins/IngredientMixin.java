package by.dragonsurvivalteam.dragonsurvival.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public abstract class IngredientMixin {
    @Inject(method = "fromJson(Lcom/google/gson/JsonElement;Z)Lnet/minecraft/world/item/crafting/Ingredient;", at = @At("HEAD"))
    private static void dragonSurvival$rewriteCommonTags(
            final JsonElement json,
            final boolean canBeEmpty,
            final CallbackInfoReturnable<Ingredient> callback
    ) {
        rewriteCommonTags(json);
    }

    private static void rewriteCommonTags(final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(IngredientMixin::rewriteCommonTags);
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject object = element.getAsJsonObject();
        if (object.has("tag") && object.get("tag").isJsonPrimitive()) {
            object.addProperty("tag", legacyCommonTag(object.get("tag").getAsString()));
        }
    }

    private static String legacyCommonTag(final String tag) {
        if (!tag.startsWith("c:")) {
            return tag;
        }

        return "forge:" + switch (tag.substring("c:".length())) {
            case "foods/berry" -> "foods/berries";
            case "foods/cooked_fish" -> "cooked_fishes";
            case "foods/cooked_meat" -> "cooked_meats";
            case "foods/raw_fish" -> "raw_fishes";
            case "foods/raw_meat" -> "raw_meats";
            case "tools/bow" -> "tools/bows";
            case "tools/crossbow" -> "tools/crossbows";
            case "tools/shield" -> "tools/shields";
            default -> tag.substring("c:".length());
        };
    }
}
