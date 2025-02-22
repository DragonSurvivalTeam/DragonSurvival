package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthItem;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncGrowthState;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = DragonSurvival.MODID)
public class DragonGrowthHandler {
    @Translation(comments = "Natural growth is §cinactive§r")
    private static final String INACTIVE = Translation.Type.GUI.wrap("message.natural_growth_inactive");

    @Translation(comments = "Natural growth is §2active§r")
    private static final String ACTIVE = Translation.Type.GUI.wrap("message.natural_growth_active");

    @Translation(comments = "You have reached the largest growth")
    private static final String REACHED_LARGEST = Translation.Type.GUI.wrap("system.reached_largest");

    @Translation(comments = "You have reached the smallest growth")
    private static final String REACHED_SMALLEST = Translation.Type.GUI.wrap("system.reached_smallest");

    @SubscribeEvent
    public static void onItemUse(final PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        Double growth = getGrowth(player, handler, event.getItemStack().getItem());

        if (growth.isNaN()) {
            if (player instanceof ServerPlayer serverPlayer) {
                DSAdvancementTriggers.STOP_NATURAL_GROWTH.get().trigger(serverPlayer);
            }

            handler.isGrowthStopped = !handler.isGrowthStopped;
            event.getItemStack().consume(1, player);

            if (player.level().isClientSide()) {
                String message = handler.isGrowthStopped ? INACTIVE : ACTIVE;
                player.displayClientMessage(Component.translatable(message), true);
            }

            return;
        } else if (growth == 0d) {
            return;
        }

        handler.incrementGrowthUses(event.getItemStack().getItem());
        double oldGrowth = handler.getDesiredGrowth();
        handler.setDesiredGrowth(player, handler.getDesiredGrowth() + growth);

        if (handler.getDesiredGrowth() == oldGrowth) {
            player.sendSystemMessage(Component.translatable(growth > 0 ? REACHED_LARGEST : REACHED_SMALLEST).withStyle(ChatFormatting.RED));
            return;
        }

        event.getItemStack().consume(1, player);
    }

    public static Double getGrowth(final Player player, final DragonStateHandler handler, final Item item) {
        // Get stage from desired growth, so that you are prevented from spamming growth items intended for an earlier stage as you grow
        for (GrowthItem growthItem : handler.stageFromDesiredSize(player).value().growthItems()) {
            if (!growthItem.canBeUsed(handler, item)) {
                continue;
            }

            if (growthItem.growthInTicks() == 0) {
                return Double.NaN;
            }

            return handler.stage().value().ticksToGrowth(growthItem.growthInTicks());
        }

        return 0d;
    }

    @SubscribeEvent
    public static void onPlayerUpdate(final PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(serverPlayer);

        if (!data.isDragon()) {
            return;
        }

        if (serverPlayer.tickCount % getInterval() == 0) {
            DragonStage dragonStage = data.stage().value();
            double oldGrowth = data.getDesiredGrowth();
            data.setDesiredGrowth(serverPlayer, data.getDesiredGrowth() + dragonStage.ticksToGrowth(getInterval()));

            if (oldGrowth == data.getDesiredGrowth() || dragonStage.isNaturalGrowthStopped().map(condition -> condition.matches(serverPlayer.serverLevel(), serverPlayer.position(), serverPlayer)).orElse(false)) {
                if (data.isGrowing) {
                    data.isGrowing = false;
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncGrowthState(false));
                }
            } else if (!data.isGrowing) {
                data.isGrowing = true;
                PacketDistributor.sendToPlayer(serverPlayer, new SyncGrowthState(true));
            }
        }
    }

    public static int getInterval() {
        return Functions.secondsToTicks(1);
    }
}