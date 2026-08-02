package by.dragonsurvivalteam.dragonsurvival.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.DisplayInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DisplayInfo.class)
public abstract class DisplayInfoMixin {
    @Inject(method = "fromJson", at = @At("HEAD"))
    private static void dragonSurvival$readModernIcon(
            final JsonObject json,
            final CallbackInfoReturnable<DisplayInfo> callback
    ) {
        if (!json.has("icon") || !json.get("icon").isJsonObject()) {
            return;
        }

        JsonObject icon = json.getAsJsonObject("icon");
        if (icon.has("id") && !icon.has("item")) {
            icon.add("item", icon.get("id"));
        }

        JsonObject components = dragonSurvival$object(icon.get("components"));
        JsonObject profile = components == null ? null : dragonSurvival$object(components.get("minecraft:profile"));
        JsonElement name = profile == null ? null : profile.get("name");
        if (!icon.has("nbt") && name != null && name.isJsonPrimitive()) {
            icon.add("nbt", new JsonPrimitive("{SkullOwner:" + dragonSurvival$quote(name.getAsString()) + "}"));
        }
    }

    @Unique private static JsonObject dragonSurvival$object(final JsonElement element) {
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    @Unique private static String dragonSurvival$quote(final String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
