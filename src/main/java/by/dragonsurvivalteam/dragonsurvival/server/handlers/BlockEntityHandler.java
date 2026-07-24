package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.BlockEntityTypeAddBlocksEvent;

@EventBusSubscriber
public class BlockEntityHandler {
    @SubscribeEvent
    public static void addToBlockEntityType(BlockEntityTypeAddBlocksEvent e) {
        e.modify(BlockEntityType.VAULT, DSBlocks.LIGHT_VAULT.get());
        e.modify(BlockEntityType.VAULT, DSBlocks.DARK_VAULT.get());
        e.modify(BlockEntityType.VAULT, DSBlocks.HUNTER_VAULT.get());
    }
}
