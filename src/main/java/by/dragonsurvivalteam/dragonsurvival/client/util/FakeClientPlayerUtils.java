package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@EventBusSubscriber(Dist.CLIENT)
public class FakeClientPlayerUtils {
    private static final ConcurrentHashMap<Integer, FakeClientPlayer> FAKE_PLAYERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, DragonEntity> FAKE_DRAGONS = new ConcurrentHashMap<>();
    private static final Set<Integer> ACTIVE_FAKE_PLAYERS = ConcurrentHashMap.newKeySet();

    public static DragonEntity getFakeDragon(int index, final DragonStateHandler handler) {
        FakeClientPlayer fakePlayer = getFakePlayer(index, handler);

        return FAKE_DRAGONS.computeIfAbsent(index, key -> new DragonEntity(DSEntities.DRAGON.get(), fakePlayer.level()) {
            @Override
            public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
                AnimationController<DragonEntity> controller = new AnimationController<>(this, "fake_player_controller", 2, state -> {
                    if (fakePlayer.handler.refreshBody) {
                        fakePlayer.animationController.forceAnimationReset();
                    }

                    if (fakePlayer.animationSupplier != null) {
                        if (state.getController().getCurrentAnimation() == null) {
                            // Sometimes it happens that this turns to null and the set animation below will do nothing
                            // Because the controller still has the same raw animation stored (no change = no update)
                            state.resetCurrentAnimation();
                        }

                        return state.setAndContinue(RawAnimation.begin().thenLoop(fakePlayer.animationSupplier.get()));
                    }

                    return PlayState.STOP;
                });

                fakePlayer.animationController = controller;
                controllers.add(controller);
            }

            @Override
            public Player getPlayer() {
                return fakePlayer;
            }
        });
    }

    public static FakeClientPlayer getFakePlayer(int index, final DragonStateHandler handler) {
        FakeClientPlayer fakePlayer = FAKE_PLAYERS.computeIfAbsent(index, FakeClientPlayer::new);
        fakePlayer.handler = handler;
        fakePlayer.lastAccessed = System.currentTimeMillis();
        ACTIVE_FAKE_PLAYERS.add(index);
        return fakePlayer;
    }

    public static int getNextIndex() {
        // 0 and 1 are reserved for the dragon altar, editor, smithing screen, etc.
        int index = 2;

        while (FAKE_PLAYERS.containsKey(index)) {
            index++;
        }

        return index;
    }

    public static void processActivePlayers(final Consumer<FakeClientPlayer> processor) {
        ACTIVE_FAKE_PLAYERS.forEach(index -> {
            FakeClientPlayer player = FAKE_PLAYERS.get(index);

            if (player != null) {
                processor.accept(player);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void clearActivePlayers(final TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ACTIVE_FAKE_PLAYERS.clear();
        }
    }

    @SubscribeEvent
    public static void clientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        FAKE_PLAYERS.forEach((index, player) -> {
            if (System.currentTimeMillis() - player.lastAccessed >= TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)) {
                player.remove(RemovalReason.DISCARDED);
                DragonEntity dragon = FAKE_DRAGONS.get(index);

                if (dragon != null) {
                    dragon.remove(RemovalReason.DISCARDED);
                    FAKE_DRAGONS.remove(index);
                }

                FAKE_PLAYERS.remove(index);
                ACTIVE_FAKE_PLAYERS.remove(index);
            }
        });
    }
}
