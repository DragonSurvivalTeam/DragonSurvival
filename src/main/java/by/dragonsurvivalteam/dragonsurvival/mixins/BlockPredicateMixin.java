package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPredicate.class)
public abstract class BlockPredicateMixin {
    @Inject(method = "fromJson", at = @At("HEAD"))
    private static void dragonSurvival$readModernBlocks(
            final JsonElement json,
            final CallbackInfoReturnable<BlockPredicate> callback
    ) {
        if (json == null || !json.isJsonObject()) {
            return;
        }

        JsonObject predicate = json.getAsJsonObject();
        JsonElement blocks = predicate.get("blocks");
        if (blocks == null || !blocks.isJsonPrimitive()) {
            return;
        }

        String value = blocks.getAsString();
        if (value.startsWith("#")) {
            JsonArray resolved = new JsonArray();
            ResourceLocationWrapper.getEntries(value, BuiltInRegistries.BLOCK)
                    .forEach(id -> resolved.add(id.toString()));
            if (resolved.size() > 0) {
                predicate.add("blocks", resolved);
            } else {
                predicate.remove("blocks");
                predicate.addProperty("tag", value.substring(1));
            }
        } else {
            JsonArray values = new JsonArray();
            values.add(value);
            predicate.add("blocks", values);
        }
    }
}
