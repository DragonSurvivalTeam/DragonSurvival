package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.PrimordialAnchorBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class PrimordialAnchorBlockEntity extends BaseBlockBlockEntity {
    public PrimordialAnchorBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.PRIMORDIAL_ANCHOR.get(), position, state);
    }

    public static void serverTick(final Level level, final BlockPos position, final BlockState state, final PrimordialAnchorBlockEntity anchor) {
        //noinspection DataFlowIssue -> server is present
        if (ServerLifecycleHooks.getCurrentServer().getWorldData().endDragonFightData().dragonKilled()) {
            level.setBlockAndUpdate(position, state.setValue(PrimordialAnchorBlock.BLOODY, true));
        } else {
            level.setBlockAndUpdate(position, state.setValue(PrimordialAnchorBlock.BLOODY, false));
        }
    }
}
