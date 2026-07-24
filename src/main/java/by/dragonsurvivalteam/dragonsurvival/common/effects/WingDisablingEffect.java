package by.dragonsurvivalteam.dragonsurvival.common.effects;

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
    public void onEffectStarted(final LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide() && entity instanceof Player player && DragonStateProvider.getData(player).isDragon()) {
            FlightData.getData(player).areWingsSpread = false;
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncWingsSpread(player.getId(), false));
        }
    }
}