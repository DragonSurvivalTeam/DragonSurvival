package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class SourceOfMagicHandler {
    @SubscribeEvent // SourceOfMagicBlock#entityInside cannot clear the counter if the entity is not inside
    public static void handleTimer(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        MovementData movement = MovementData.getData(player);
        handler.isOnMagicSource = isOnMagicSource(player, handler);

        if (handler.isOnMagicSource && !movement.isMoving() && !movement.dig && !player.isCrouching()) {
            handler.magicSource++;
        } else {
            handler.magicSource = 0;
        }
    }

    private static boolean isOnMagicSource(final Player player, final DragonStateHandler handler) {
        BlockState state = player.getBlockStateOn();

        if (!(state.getBlock() instanceof SourceOfMagicBlock sourceBlock)) {
            return false;
        }

        if (!sourceBlock.isMagic(state)) {
            return false;
        }

        return player.level().getBlockEntity(player.blockPosition()) instanceof SourceOfMagicBlockEntity source && source.isApplicableFor(handler);
    }
}
