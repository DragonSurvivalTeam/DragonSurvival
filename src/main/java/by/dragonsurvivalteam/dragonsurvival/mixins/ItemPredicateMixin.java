package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPredicate.class)
public abstract class ItemPredicateMixin {
    @Inject(method = "fromJson", at = @At("HEAD"))
    private static void dragonSurvival$readModernPredicate(
            final JsonElement json,
            final CallbackInfoReturnable<ItemPredicate> callback
    ) {
        if (json == null || !json.isJsonObject()) {
            return;
        }

        JsonObject predicate = json.getAsJsonObject();
        JsonElement items = predicate.get("items");
        if (items != null && items.isJsonPrimitive()) {
            String value = items.getAsString();
            if (value.startsWith("#")) {
                JsonArray resolved = new JsonArray();
                ResourceLocationWrapper.getEntries(value, BuiltInRegistries.ITEM)
                        .forEach(id -> resolved.add(id.toString()));
                if (!resolved.isEmpty()) {
                    predicate.add("items", resolved);
                } else {
                    predicate.remove("items");
                    predicate.addProperty("tag", value.substring(1));
                }
            } else {
                JsonArray values = new JsonArray();
                values.add(value);
                predicate.add("items", values);
            }
        }

        JsonObject subPredicates = dragonSurvival$object(predicate.get("predicates"));
        JsonElement enchantments = subPredicates == null ? null : subPredicates.get("minecraft:enchantments");
        if (!predicate.has("enchantments") && enchantments != null && enchantments.isJsonArray()) {
            JsonArray legacyEnchantments = enchantments.getAsJsonArray().deepCopy();
            legacyEnchantments.forEach(element -> {
                if (element.isJsonObject()) {
                    JsonObject enchantment = element.getAsJsonObject();
                    JsonElement ids = enchantment.remove("enchantments");
                    if (ids != null) {
                        if (ids.isJsonArray() && ids.getAsJsonArray().size() == 1) {
                            enchantment.add("enchantment", ids.getAsJsonArray().get(0));
                        } else if (ids.isJsonPrimitive()) {
                            enchantment.add("enchantment", ids);
                        }
                    }
                }
            });
            predicate.add("enchantments", legacyEnchantments);
        }
    }

    @Unique private static JsonObject dragonSurvival$object(final JsonElement element) {
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }
}
