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
    private final SourceOfMagicData defaultData;

    public SourceOfMagicItem(final Block block, final Properties properties, final SourceOfMagicData defaultData) {
        super(block, properties);
        this.defaultData = defaultData;
    }

    @Override
    public @NotNull net.minecraft.world.item.ItemStack getDefaultInstance() {
        net.minecraft.world.item.ItemStack stack = super.getDefaultInstance();
        DSDataComponents.SOURCE_OF_MAGIC.set(stack, defaultData);
        return stack;
    }

    @Override
    protected boolean placeBlock(@NotNull final BlockPlaceContext context, @NotNull final BlockState state) {
        boolean placed = super.placeBlock(context, state);

        if (placed && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof SourceOfMagicBlockEntity source) {
            SourceOfMagicData data = DSDataComponents.SOURCE_OF_MAGIC.get(
                    context.getItemInHand(),
                    context.getLevel().registryAccess()
            );

            SourceOfMagicData appliedData = data != null ? data : defaultData;
            source.setConsumables(appliedData.consumables());
            source.setApplicableSpecies(appliedData.applicableSpecies());
        }

        return placed;
    }
}
