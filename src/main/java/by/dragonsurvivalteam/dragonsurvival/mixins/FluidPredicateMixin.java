package by.dragonsurvivalteam.dragonsurvival.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.FluidPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidPredicate.class)
public abstract class FluidPredicateMixin {
    @Inject(method = "fromJson", at = @At("HEAD"))
    private static void dragonSurvival$readModernFluids(
            final JsonElement json,
            final CallbackInfoReturnable<FluidPredicate> callback
    ) {
        if (json == null || !json.isJsonObject()) {
            return;
        }

        JsonObject predicate = json.getAsJsonObject();
        JsonElement fluids = predicate.get("fluids");
        if (fluids == null || !fluids.isJsonPrimitive()) {
            return;
        }

        String value = fluids.getAsString();
        if (value.startsWith("#")) {
            predicate.addProperty("tag", value.substring(1));
        } else {
            predicate.addProperty("fluid", value);
        }
    }
}
