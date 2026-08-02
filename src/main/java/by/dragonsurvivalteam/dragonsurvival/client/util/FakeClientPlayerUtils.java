package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

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

    public static void processDragons(final Consumer<DragonEntity> processor) {
        FAKE_DRAGONS.values().forEach(processor);
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
    public static void clearActivePlayers(final RenderFrameEvent.Pre event) {
        ACTIVE_FAKE_PLAYERS.clear();
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
                ACTIVE_FAKE_PLAYERS.remove(index);
            }
        });
    }
}
