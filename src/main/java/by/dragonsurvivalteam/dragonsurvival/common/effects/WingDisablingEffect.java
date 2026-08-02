package by.dragonsurvivalteam.dragonsurvival.common.effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncWingsSpread;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class WingDisablingEffect extends ModifiableMobEffect {
    public WingDisablingEffect(final MobEffectCategory type, int color, boolean incurable) {
        super(type, color, incurable);
    }

    @SubscribeEvent
    public static void onEffectStarted(final MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() instanceof WingDisablingEffect
            && !event.getEntity().level().isClientSide()
            && event.getEntity() instanceof Player player
            && DragonStateProvider.getData(player).isDragon()) {
            FlightData.getData(player).areWingsSpread = false;
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncWingsSpread(player.getId(), false));
        }
    }
}
