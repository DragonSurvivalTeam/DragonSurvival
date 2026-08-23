package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClimbingHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IForgeBlock.class, remap = false)
public interface IBlocksExtensionMixin {
    /**
     * @author Dragon Survival Team
     * @reason Extends Forge's default ladder check with Dragon Survival climbable effects.
     */
    @ModifyReturnValue(method = "isLadder", at = @At("RETURN"))
    default boolean isLadder(boolean original, @Local(argsOnly = true) final LivingEntity entity) {
        if (entity == null) {
            // MineColonies may cause this to be null
            return original;
        }

        ClimbableData data = AttachmentManager.getExistingData(entity, DSDataAttachments.CLIMBABLE_DATA).orElse(null);

        if (data == null || original) {
            return original;
        }

        return ClimbingHandler.canClimb(entity, data);
    }
}
