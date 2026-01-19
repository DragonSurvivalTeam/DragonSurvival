package by.dragonsurvivalteam.dragonsurvival.client.util;

import net.minecraft.world.InteractionResult;

public class InteractionResultUtils {
    public static InteractionResult sidedSuccess(boolean isClientSide) {
        return isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
}
