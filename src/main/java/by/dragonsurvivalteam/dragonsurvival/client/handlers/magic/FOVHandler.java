package by.dragonsurvivalteam.dragonsurvival.client.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber(Dist.CLIENT)
public class FOVHandler {
    @SubscribeEvent
    public static void onFovEvent(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();

        AtomicReference<DragonEntity> atomicDragon = ClientDragonRenderer.playerDragonHashMap.get(player.getId());
        if (atomicDragon != null) {
            DragonEntity dragon = atomicDragon.get();
            if (dragon.isPlayingAnyEmote()) {
                event.setNewFovModifier(1f);
                return;
            }
        }

        DragonAbilityInstance ability = MagicData.getData(player).getCurrentlyCasting();

        if (ability != null && ability.getCurrentCastTime() > 0) {
            double perc = Math.min(ability.getCurrentCastTime() / (float) ability.getCastTime(), 1) / 4;
            double c4 = 2 * Math.PI / 3;

            if (perc != 0 && perc != 1) {
                perc = Math.pow(2, -10 * perc) * Math.sin((perc * 10 - 0.75) * c4) + 1;
            }

            float newFov = (float) Mth.clamp(perc, 1.0F, 1.0F);
            event.setNewFovModifier(newFov);
        }
    }
}