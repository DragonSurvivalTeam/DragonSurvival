package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.item.HunterItemRenderStateAccess;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @Inject(
        method = "updateForTopItem",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;clear()V", shift = At.Shift.AFTER)
    )
    private void dragonSurvival$storeHunterItemAlpha(
        final ItemStackRenderState output,
        final ItemStack item,
        final ItemDisplayContext displayContext,
        final @Nullable Level level,
        final @Nullable ItemOwner owner,
        final int seed,
        final CallbackInfo callback
    ) {
        float alpha = HunterHandler.UNMODIFIED;
        LivingEntity livingEntity = owner != null ? owner.asLivingEntity() : null;

        if (livingEntity != null && (dragonSurvival$isThirdPerson(displayContext) || HunterHandler.TRANSLUCENT_ITEMS_IN_FIRST_PERSON && dragonSurvival$isFirstPerson(displayContext))) {
            LivingEntity relevantEntity = dragonSurvival$getRelevantEntity(livingEntity);

            if (relevantEntity != null) {
                alpha = HunterHandler.calculateAlphaAsFloat(relevantEntity);
            }
        }

        ((HunterItemRenderStateAccess) output).dragonSurvival$setHunterItemAlpha(alpha);
    }

    @Unique
    private static boolean dragonSurvival$isThirdPerson(final ItemDisplayContext context) {
        return context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Unique
    private static boolean dragonSurvival$isFirstPerson(final ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    @Unique
    private static @Nullable LivingEntity dragonSurvival$getRelevantEntity(final LivingEntity entity) {
        if (entity instanceof DragonEntity dragon) {
            Player player = dragon.getPlayer();

            if (player != null && HunterData.hasTransparency(player)) {
                return player;
            }
        }

        return HunterData.hasTransparency(entity) ? entity : null;
    }
}
