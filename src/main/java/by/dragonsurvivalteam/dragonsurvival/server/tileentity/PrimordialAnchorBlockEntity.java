package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.PrimordialAnchorBlock;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class PrimordialAnchorBlockEntity extends BaseBlockBlockEntity {
    public PrimordialAnchorBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.PRIMORDIAL_ANCHOR.get(), position, state);
    }

    @Translation(key = "anchor_has_bloody_state", type = Translation.Type.CONFIGURATION, comments = "If enabled, the primordial anchor will become unusuable as long as the ender dragon is dead.")
    @ConfigOption(side = ConfigSide.SERVER, category = "primordial_anchor", key = "anchor_has_bloody_state")
    public static Boolean anchorHasBloodyState = true;

    public static void serverTick(final Level level, final BlockPos position, final BlockState state, final PrimordialAnchorBlockEntity anchor) {
        //noinspection DataFlowIssue -> server is present
        if (ServerLifecycleHooks.getCurrentServer().getWorldData().endDragonFightData().dragonKilled() && anchorHasBloodyState) {
            level.setBlockAndUpdate(position, state.setValue(PrimordialAnchorBlock.BLOODY, true));
        } else {
            level.setBlockAndUpdate(position, state.setValue(PrimordialAnchorBlock.BLOODY, false));
        }
    }
}
