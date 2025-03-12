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
    private static final Lazy<Map<Block, Block>> ALTAR_MAP = Lazy.of(() -> Map.ofEntries(
            Map.entry(Blocks.STONE, DSBlocks.STONE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.MOSSY_COBBLESTONE, DSBlocks.MOSSY_DRAGON_ALTAR.get()),
            Map.entry(Blocks.SANDSTONE, DSBlocks.SANDSTONE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.RED_SANDSTONE, DSBlocks.RED_SANDSTONE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.OAK_PLANKS, DSBlocks.OAK_DRAGON_ALTAR.get()),
            Map.entry(Blocks.BIRCH_PLANKS, DSBlocks.BIRCH_DRAGON_ALTAR.get()),
            Map.entry(Blocks.PURPUR_BLOCK, DSBlocks.PURPUR_DRAGON_ALTAR.get()),
            Map.entry(Blocks.NETHER_BRICKS, DSBlocks.NETHER_BRICK_DRAGON_ALTAR.get()),
            Map.entry(Blocks.BLACKSTONE, DSBlocks.BLACKSTONE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.BONE_BLOCK, DSBlocks.BONE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.PACKED_ICE, DSBlocks.ICE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.QUARTZ_BLOCK, DSBlocks.QUARTZ_DRAGON_ALTAR.get()),
            Map.entry(Blocks.OBSIDIAN, DSBlocks.OBSIDIAN_DRAGON_ALTAR.get()),
            Map.entry(Blocks.AMETHYST_BLOCK, DSBlocks.AMETHYST_DRAGON_ALTAR.get()),
            Map.entry(Blocks.PACKED_MUD, DSBlocks.MUDBRICK_DRAGON_ALTAR.get()),
            Map.entry(Blocks.PRISMARINE_BRICKS, DSBlocks.PRISMARINE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.RED_NETHER_BRICKS, DSBlocks.RED_NETHER_BRICK_DRAGON_ALTAR.get()),
            Map.entry(Blocks.NETHERRACK, DSBlocks.NETHERRACK_DRAGON_ALTAR.get()),
            Map.entry(Blocks.END_STONE, DSBlocks.ENDSTONE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.COBBLED_DEEPSLATE, DSBlocks.DEEPSLATE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.TUFF, DSBlocks.TUFF_DRAGON_ALTAR.get()),
            Map.entry(Blocks.BAMBOO_PLANKS, DSBlocks.BAMBOO_DRAGON_ALTAR.get()),
            Map.entry(Blocks.CRIMSON_PLANKS, DSBlocks.CRIMSON_DRAGON_ALTAR.get()),
            Map.entry(Blocks.WARPED_PLANKS, DSBlocks.WARPED_DRAGON_ALTAR.get()),
            Map.entry(Blocks.MANGROVE_PLANKS, DSBlocks.MANGROVE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.CHERRY_PLANKS, DSBlocks.CHERRY_DRAGON_ALTAR.get()),
            Map.entry(Blocks.ACACIA_PLANKS, DSBlocks.ACACIA_DRAGON_ALTAR.get()),
            Map.entry(Blocks.DARK_OAK_PLANKS, DSBlocks.DARK_OAK_DRAGON_ALTAR.get()),
            Map.entry(Blocks.JUNGLE_PLANKS, DSBlocks.JUNGLE_DRAGON_ALTAR.get()),
            Map.entry(Blocks.SPRUCE_PLANKS, DSBlocks.SPRUCE_DRAGON_ALTAR.get())
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
        event.getLevel().playSound(event.getEntity(), event.getPos(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.1f, 1.5f);

        stack.consume(1, event.getEntity());

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
