package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;

import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@EventBusSubscriber(Dist.CLIENT)
public class FakeClientPlayerUtils {
    private static final ConcurrentHashMap<Integer, FakeClientPlayer> FAKE_PLAYERS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, DragonEntity> FAKE_DRAGONS = new ConcurrentHashMap<>();

    public static DragonEntity getFakeDragon(int index, final DragonStateHandler handler) {
        FakeClientPlayer fakePlayer = getFakePlayer(index, handler);

        return FAKE_DRAGONS.computeIfAbsent(index, key -> new DragonEntity(DSEntities.DRAGON.get(), fakePlayer.level()) {
            @Override
            public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
                AnimationController<DragonEntity> controller = new AnimationController<>("fake_player_controller", 2, state -> {
                    boolean wasReset = false;
                    if (fakePlayer.handler.refreshBody) {
                        fakePlayer.animationController.reset();
                        wasReset = true;
                    }

                    if (fakePlayer.animationSupplier != null) {
                        if (state.controller().getCurrentRawAnimation() == null) {
                            // Sometimes it happens that this turns to null and the set animation below will do nothing
                            // Because the controller still has the same raw animation stored (no change = no update)
                            state.controller().reset();
                            wasReset = true;
                        }

                        state.controller().setTransitionTicks(wasReset ? 0 : 2);
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
        FAKE_PLAYERS.computeIfAbsent(index, FakeClientPlayer::new);
        FAKE_PLAYERS.get(index).handler = handler;
        FAKE_PLAYERS.get(index).lastAccessed = System.currentTimeMillis();
        return FAKE_PLAYERS.get(index);
    }

    public static int getNextIndex() {
        // 0 and 1 are reserved for the dragon altar, editor, smithing screen, etc.
        int index = 2;

        while (FAKE_PLAYERS.containsKey(index)) {
            index++;
        }

        return index;
    }

    public static void processDragons(final Consumer<DragonEntity> processor) {
        FAKE_DRAGONS.values().forEach(processor);
    }

    @SubscribeEvent
    public static void clientTick(final ClientTickEvent.Pre event) {
        FAKE_PLAYERS.forEach((index, player) -> {
            if (System.currentTimeMillis() - player.lastAccessed >= TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)) {
                player.remove(RemovalReason.DISCARDED);
                DragonRenderer.clearUIRenderDragon(player.getId());
                DragonEntity dragon = FAKE_DRAGONS.get(index);

                if (dragon != null) {
                    dragon.remove(RemovalReason.DISCARDED);
                    FAKE_DRAGONS.remove(index);
                    DragonRenderer.clearRenderState(dragon.getId());
                }

                FAKE_PLAYERS.remove(index);
            }
        });
    }
}
