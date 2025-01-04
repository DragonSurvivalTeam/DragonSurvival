package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.SourceOfMagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class SourceOfMagicItem extends BlockItem {
    public SourceOfMagicItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(@NotNull final BlockPlaceContext context, @NotNull final BlockState state) {
        boolean placed = super.placeBlock(context, state);

        if (placed && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof SourceOfMagicBlockEntity source) {
            SourceOfMagicData data = context.getItemInHand().getComponents().get(DSDataComponents.SOURCE_OF_MAGIC.get());

            if (data != null) {
                source.setConsumables(data.consumables());
                source.setApplicableSpecies(data.applicableSpecies());
            }
        }

        return placed;
    }
}
