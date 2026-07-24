package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonBeaconData;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonBeaconBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DragonBeaconItem extends BlockItem {
    public DragonBeaconItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(@NotNull final BlockPlaceContext context, @NotNull final BlockState state) {
        boolean placed = super.placeBlock(context, state);

        if (placed && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof DragonBeaconBlockEntity beacon) {
            DragonBeaconData data = context.getItemInHand().getComponents().get(DSDataComponents.DRAGON_BEACON.get());

            if (data != null) {
                beacon.setData(data);
            }
        }

        return placed;
    }
}
