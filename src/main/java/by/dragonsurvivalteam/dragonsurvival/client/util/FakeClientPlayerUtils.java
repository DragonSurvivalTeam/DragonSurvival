package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

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
                AnimationController<DragonEntity> controller = new AnimationController<>(this, "fake_player_controller", 2, state -> {
                    if (fakePlayer.handler.refreshBody) {
                        fakePlayer.animationController.forceAnimationReset();
                    }

                    if (fakePlayer.animationSupplier != null) {
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

    @SubscribeEvent
    public static void clientTick(final ClientTickEvent.Pre event) {
        FAKE_PLAYERS.forEach((index, player) -> {
            if (System.currentTimeMillis() - player.lastAccessed >= TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES)) {
                player.remove(RemovalReason.DISCARDED);
                DragonEntity dragon = FAKE_DRAGONS.get(index);

                if (dragon != null) {
                    dragon.remove(RemovalReason.DISCARDED);
                    FAKE_DRAGONS.remove(index);
                }

                FAKE_PLAYERS.remove(index);
            }
        });
    }
}