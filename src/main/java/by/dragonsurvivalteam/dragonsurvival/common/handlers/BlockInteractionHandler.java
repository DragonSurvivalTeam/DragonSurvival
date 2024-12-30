package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;

@EventBusSubscriber
public class BlockInteractionHandler {
    private static final Lazy<Map<Block, Block>> ALTAR_MAP = Lazy.of(() -> Map.of(
            Blocks.STONE, DSBlocks.STONE_DRAGON_ALTAR.get(),
            Blocks.MOSSY_COBBLESTONE, DSBlocks.MOSSY_DRAGON_ALTAR.get(),
            Blocks.SANDSTONE, DSBlocks.SANDSTONE_DRAGON_ALTAR.get(),
            Blocks.RED_SANDSTONE, DSBlocks.RED_SANDSTONE_DRAGON_ALTAR.get(),
            Blocks.OAK_LOG, DSBlocks.OAK_DRAGON_ALTAR.get(),
            Blocks.BIRCH_LOG, DSBlocks.BIRCH_DRAGON_ALTAR.get(),
            Blocks.PURPUR_BLOCK, DSBlocks.PURPUR_DRAGON_ALTAR.get(),
            Blocks.NETHER_BRICKS, DSBlocks.NETHER_BRICK_DRAGON_ALTAR.get(),
            Blocks.BLACKSTONE, DSBlocks.BLACKSTONE_DRAGON_ALTAR.get()
    ));

    @SubscribeEvent
    public static void createAltar(final PlayerInteractEvent.RightClickBlock event) {
        if (!ServerConfig.transformAltar || event.getEntity().isSpectator()) {
            return;
        }

        ItemStack stack = event.getItemStack();

        if (!stack.is(DSItems.ELDER_DRAGON_BONE)) {
            return;
        }

        Block altar = ALTAR_MAP.get().get(event.getLevel().getBlockState(event.getPos()).getBlock());

        if (altar == null) {
            return;
        }

        BlockPlaceContext direction = new BlockPlaceContext(event.getLevel(), event.getEntity(), event.getHand(), event.getItemStack(), new BlockHitResult(Vec3.ZERO, event.getEntity().getDirection(), event.getPos(), false));
        BlockState state = altar.getStateForPlacement(direction);

        if (state == null) {
            return;
        }

        event.getLevel().setBlockAndUpdate(event.getPos(), state);
        event.getLevel().playSound(event.getEntity(), event.getPos(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1, 1);

        if (!event.getEntity().isCreative()) {
            stack.shrink(1);
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
