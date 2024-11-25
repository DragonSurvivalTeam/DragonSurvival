package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncGrowthState;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

@EventBusSubscriber(modid = DragonSurvival.MODID)
public class DragonGrowthHandler {
    @Translation(type = Translation.Type.MISC, comments = "You have reached the largest size")
    private static final String REACHED_LARGEST = Translation.Type.GUI.wrap("system.reached_largest");

    @Translation(type = Translation.Type.MISC, comments = "You have reached the smallest size")
    private static final String REACHED_SMALLEST = Translation.Type.GUI.wrap("system.reached_smallest");

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return;
        }

        double growth = getGrowth(data.getStage(), event.getItemStack().getItem());

        if (growth == 0) {
            return;
        }

        double newSize = data.getStage().value().getNextSize(player.registryAccess(), data.getSize() + growth, data.previousStage != null ? data.previousStage.value() : null);

        if (data.getSize() == newSize) {
            player.sendSystemMessage(Component.translatable(growth > 0 ? REACHED_LARGEST : REACHED_SMALLEST).withStyle(ChatFormatting.RED));
            return;
        }

        data.setSize(player, data.getStage(), newSize);

        if (!player.isCreative()) {
            event.getItemStack().shrink(1);
        }
    }

    public static double getGrowth(final Holder<DragonStage> dragonStage, final Item item) {
        int growth = 0;

        for (MiscCodecs.GrowthItem growthItem : dragonStage.value().growthItems()) {
            // Select the largest number (independent on positive / negative)
            if ((growth == 0 || Math.abs(growthItem.growthInTicks()) > Math.abs(growth)) && growthItem.items().contains(item.builtInRegistryHolder())) {
                growth = growthItem.growthInTicks();
            }
        }

        return dragonStage.value().ticksToSize(growth);
    }

    @SubscribeEvent
    public static void onPlayerUpdate(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(serverPlayer);

        if (!data.isDragon()) {
            return;
        }

        DragonStage dragonStage = data.getStage().value();
        double nextSize = dragonStage.getNextSize(serverPlayer.registryAccess(), data.getSize() + dragonStage.ticksToSize(getInterval()), data.previousStage != null ? data.previousStage.value() : null);
        Optional<EntityPredicate> isNaturalGrowthStopped = dragonStage.isNaturalGrowthStopped();

        if (nextSize == data.getSize() || isNaturalGrowthStopped.isPresent() && isNaturalGrowthStopped.get().matches(serverPlayer.serverLevel(), serverPlayer.position(), serverPlayer)) {
            if (data.isGrowing) {
                data.isGrowing = false;
                PacketDistributor.sendToPlayer(serverPlayer, new SyncGrowthState.Data(false));
            }

            return;
        } else if (!data.isGrowing) {
            data.isGrowing = true;
            PacketDistributor.sendToPlayer(serverPlayer, new SyncGrowthState.Data(true));
        }

        if (serverPlayer.tickCount % getInterval() == 0) {
            data.setSize(serverPlayer, data.getStage(), nextSize);
        }
    }

    public static int getInterval() {
        return Functions.secondsToTicks(1);
    }
}