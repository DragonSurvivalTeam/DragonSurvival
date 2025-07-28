package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

@EventBusSubscriber
public class BlockEntityHandler {
    @SubscribeEvent
    public static void addToBlockEntityType(BlockEntityTypeAddBlocksEvent e) {
        e.modify(BlockEntityType.VAULT, DSBlocks.LIGHT_VAULT.get());
        e.modify(BlockEntityType.VAULT, DSBlocks.DARK_VAULT.get());
        e.modify(BlockEntityType.VAULT, DSBlocks.HUNTER_VAULT.get());
    }
}
