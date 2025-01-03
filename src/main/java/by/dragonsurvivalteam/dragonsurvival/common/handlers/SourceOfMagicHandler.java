package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class SourceOfMagicHandler {
    @SubscribeEvent // SourceOfMagicBlock#entityInside cannot clear the counter if the entity is not inside
    public static void handleTimer(final PlayerTickEvent.Post event) {
        DragonStateHandler handler = DragonStateProvider.getData(event.getEntity());

        if (!handler.isDragon()) {
            return;
        }

        BlockState blockStateOn = event.getEntity().getBlockStateOn();
        MovementData movement = MovementData.getData(event.getEntity());
        handler.isOnMagicSource = blockStateOn.getBlock() instanceof SourceOfMagicBlock source && source.isMagic(blockStateOn) && source.isFor(handler);

        if (handler.isOnMagicSource && !movement.isMoving()) {
            // TODO :: previously checked for crouching and digging as well
            handler.magicSource++;
        } else {
            handler.magicSource = 0;
        }
    }

    @SubscribeEvent
    public static void resetTimer(final LivingDamageEvent.Post event) {
        // TODO :: would need a whole new packet just to sync a number
        //  not sure if that is even worth it
        if (event.getEntity() instanceof Player player) {

        }
    }
}