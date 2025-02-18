package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {
    @Inject(method = "spawnFire", at = @At("HEAD"), cancellable = true)
    private void preventFireFromSpawningFromDataAttachment(int extraIgnitions, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!entity.getData(DSDataAttachments.LIGHTNING_BOLT).spawnsFire) {
            ci.cancel();
        }
    }
}
