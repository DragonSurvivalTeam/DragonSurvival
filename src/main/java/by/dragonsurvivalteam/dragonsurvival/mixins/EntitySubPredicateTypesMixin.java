package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.DSSubPredicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntitySubPredicate.Types.class)
public abstract class EntitySubPredicateTypesMixin {
    @Shadow @Final @Mutable public static BiMap<String, EntitySubPredicate.Type> TYPES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void dragonSurvival$registerTypes(final CallbackInfo callback) {
        TYPES = ImmutableBiMap.<String, EntitySubPredicate.Type>builder()
                .putAll(TYPES)
                .put("dragonsurvival:dragon_predicate", DSSubPredicates.DRAGON_PREDICATE)
                .put("dragonsurvival:entity_check_predicate", DSSubPredicates.ENTITY_CHECK_PREDICATE)
                .put("dragonsurvival:custom_predicates", DSSubPredicates.CUSTOM_PREDICATES)
                .build();
    }
}
