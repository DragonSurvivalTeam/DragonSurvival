package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityFlagsPredicateAccess;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityFlagsPredicate.class)
public abstract class EntityFlagsPredicateMixin implements EntityFlagsPredicateAccess {
    @Unique
    private @Nullable Boolean dragonsurvival$onGround;

    @Override
    public void dragonsurvival$setOnGround(final Boolean onGround) {
        dragonsurvival$onGround = onGround;
    }

    @Inject(method = "matches", at = @At("RETURN"), cancellable = true)
    private void dragonsurvival$matchOnGround(final Entity entity, final CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue() && dragonsurvival$onGround != null && entity.onGround() != dragonsurvival$onGround) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "fromJson", at = @At("RETURN"))
    private static void dragonsurvival$readOnGround(final JsonElement json, final CallbackInfoReturnable<EntityFlagsPredicate> callback) {
        if (json != null && json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();

            if (object.has("is_on_ground")) {
                ((EntityFlagsPredicateAccess) callback.getReturnValue()).dragonsurvival$setOnGround(object.get("is_on_ground").getAsBoolean());
            }
        }
    }

    @Inject(method = "serializeToJson", at = @At("RETURN"))
    private void dragonsurvival$writeOnGround(final CallbackInfoReturnable<JsonElement> callback) {
        if (dragonsurvival$onGround != null && callback.getReturnValue().isJsonObject()) {
            callback.getReturnValue().getAsJsonObject().addProperty("is_on_ground", dragonsurvival$onGround);
        }
    }
}
