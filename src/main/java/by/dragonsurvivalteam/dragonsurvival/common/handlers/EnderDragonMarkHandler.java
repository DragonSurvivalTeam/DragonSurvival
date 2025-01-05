package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncEnderDragonMark;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EnderDragonDamageHistory;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber
public class EnderDragonMarkHandler {

    @SubscribeEvent
    public static void onEnderDragonHealthChanged(LivingDamageEvent.Post event) {
        if(event.getEntity().level().isClientSide()) return;

        if(event.getEntity() instanceof EnderDragon enderDragon) {
            if(event.getSource().getEntity() instanceof Player player) {
                EnderDragonDamageHistory data = EnderDragonDamageHistory.getData(enderDragon);
                data.addDamage(player.getUUID(), event.getNewDamage());
            }

            // If the dragon is healed, reverse progress for all the players
            if(event.getNewDamage() < 0) {
                EnderDragonDamageHistory data = EnderDragonDamageHistory.getData(enderDragon);
                data.addDamageAll(event.getNewDamage());
            }
        }
    }

    @SubscribeEvent
    public static void onEnderDragonDeath(LivingDeathEvent event) {
        if(event.getEntity().level().isClientSide()) return;

        if(event.getEntity() instanceof EnderDragon enderDragon) {
            EnderDragonDamageHistory data = EnderDragonDamageHistory.getData(enderDragon);
            for(Player player : data.getPlayers(event.getEntity().level())) {
                DragonStateHandler handler = DragonStateProvider.getData(player);
                if(handler.isDragon()) {
                    handler.markedByEnderDragon = true;

                    PacketDistributor.sendToPlayer((ServerPlayer)player, new SyncEnderDragonMark(true));
                }
            }
        }
    }

    public static final ClientEffectProvider MARK_EFFECT = new ClientEffectProvider() {
        @Translation(type = Translation.Type.MODIFIER, comments = "Ender Dragon's Curse")
        private static final ResourceLocation MODIFIER = DragonSurvival.res("ender_dragon_curse");

        @Translation(comments = "You have been cursed by the ender dragon. You may be unable to use some of your abilities. You can cure this curse by using a Primordial Anchor block after resurrecting the dragon.")
        private static final String DESCRIPTION = Translation.Type.MODIFIER_TOOLTIP.wrap("ender_dragon_curse");

        private static final ClientData DATA = new ClientData(
                DragonSurvival.res("textures/modifiers/ender_dragon_curse.png"),
                Component.translatable(Translation.Type.MODIFIER.wrap(MODIFIER)),
                Component.translatable(DESCRIPTION),
                Component.empty()
        );

        @Override
        public ClientData clientData() {
            return DATA;
        }

        @Override
        public int getDuration() {
            return DurationInstance.INFINITE_DURATION;
        }

        @Override
        public int currentDuration() {
            return 0;
        }
    };
}
