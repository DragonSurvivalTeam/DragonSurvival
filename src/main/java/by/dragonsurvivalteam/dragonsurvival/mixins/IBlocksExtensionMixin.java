package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClimbingHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IForgeBlock.class, remap = false)
public interface IBlocksExtensionMixin {
    /**
     * @author Dragon Survival Team
     * @reason Extends Forge's default ladder check with Dragon Survival climbable effects.</br>
     * (Injectors are not supported for interfaces in 1.20.1)
     */
    @Overwrite
    default boolean isLadder(final BlockState state, final LevelReader level, final BlockPos pos, final LivingEntity entity) {
        if (state.is(BlockTags.CLIMBABLE)) {
            return true;
        }

        if (entity == null) {
            // MineColonies may cause this to be null
            return false;
        }

        ClimbableData data = AttachmentManager.getExistingData(entity, DSDataAttachments.CLIMBABLE_DATA).orElse(null);

        if (data == null) {
            return false;
        }

        return ClimbingHandler.canClimb(entity, data);
    }
}
