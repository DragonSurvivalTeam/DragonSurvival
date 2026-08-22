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
        ClimbableData data = entity.getExistingData(DSDataAttachments.CLIMBABLE_DATA).orElse(null);

        if (data == null) {
            return original;
        }

        // Make sure to always reset at the start since it is referenced at other points
        data.climbPosition = null;

        if (original) {
            return true;
        }

        return ClimbingHandler.canClimb(entity, data);
    }
}
