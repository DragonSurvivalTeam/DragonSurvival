package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.item.HunterItemLayerAccess;
import by.dragonsurvivalteam.dragonsurvival.client.render.item.HunterItemRenderStateAccess;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin implements HunterItemRenderStateAccess {
    @Unique private float dragonSurvival$hunterItemAlpha = HunterHandler.UNMODIFIED;

    @Inject(method = "clear", at = @At("TAIL"))
    private void dragonSurvival$clearHunterItemAlpha(final CallbackInfo callback) {
        dragonSurvival$hunterItemAlpha = HunterHandler.UNMODIFIED;
    }

    @Inject(method = "newLayer", at = @At("RETURN"))
    private void dragonSurvival$inheritHunterItemAlpha(final CallbackInfoReturnable<ItemStackRenderState.LayerRenderState> callback) {
        ((HunterItemLayerAccess) callback.getReturnValue()).dragonSurvival$setHunterItemAlpha(dragonSurvival$hunterItemAlpha);
    }

    @Override
    public float dragonSurvival$getHunterItemAlpha() {
        return dragonSurvival$hunterItemAlpha;
    }

    @Override
    public void dragonSurvival$setHunterItemAlpha(final float alpha) {
        dragonSurvival$hunterItemAlpha = alpha;
    }
}
