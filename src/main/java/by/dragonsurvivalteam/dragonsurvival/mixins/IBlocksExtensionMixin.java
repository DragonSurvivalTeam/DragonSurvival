package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClimbingHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IBlockExtension.class)
public interface IBlocksExtensionMixin {
    @ModifyReturnValue(method = "isLadder", at = @At("RETURN"))
    default boolean isLadder(boolean original, @Local(argsOnly = true) final LivingEntity entity) {
        if (entity == null) {
            // MineColonies may cause this to be null
            return original;
        }

        ClimbableData data = entity.getExistingData(DSDataAttachments.CLIMBABLE_DATA).orElse(null);

        if (data == null || original) {
            return original;
        }

        return ClimbingHandler.canClimb(entity, data);
    }
}
