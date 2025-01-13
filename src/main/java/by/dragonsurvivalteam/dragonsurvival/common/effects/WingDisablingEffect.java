package by.dragonsurvivalteam.dragonsurvival.common.effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncWingsSpread;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class WingDisablingEffect extends ModifiableMobEffect {
    public WingDisablingEffect(final MobEffectCategory type, int color, boolean incurable) {
        super(type, color, incurable);
    }

    @Override
    public void onEffectStarted(final LivingEntity entity, int strength) {
        if (!entity.level().isClientSide() && entity instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                FlightData data = FlightData.getData(player);
                data.areWingsSpread = false;
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncWingsSpread(player.getId(), false));
            }
        }
    }
}