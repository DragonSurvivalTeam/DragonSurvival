package by.dragonsurvivalteam.dragonsurvival.util.proxy;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmote;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

public interface Proxy {
    default @Nullable Player getLocalPlayer() {
        return null;
    }

    default @Nullable Level getLocalLevel() {
        return null;
    }

    default void playSoundAtEyeLevel(final Player player, final SoundEvent event) { /* Nothing to do */ }

    default void queueTickingSound(final ResourceLocation id, final SoundEvent soundEvent, final SoundSource soundSource, final Entity entity) { /* Nothing to do */ }

    default void stopTickingSound(final ResourceLocation id) { /* Nothing to do */ }

    default void setCurrentAbilityAnimation(int playerId, Pair<AbilityAnimation, AnimationType> animation) { /* Nothing to do */ }

    default void stopEmote(int playerId, DragonEmote emote) { /* Nothing to do */ }

    default void beginPlayingEmote(int playerId, DragonEmote emote) { /* Nothing to do */ }

    default void stopAllEmotes(int playerId) { /* Nothing to do */ }

    default boolean isPlayingEmote(int playerId, DragonEmote emote) {
        return false;
    }

    default float getTimer() {
        return 1;
    }

    default float getPartialTick() {
        return 1;
    }

    /** This will always return 'false' when in production */
    default boolean isOnRenderThread() {
        return false;
    }

    default boolean isFakePlayer(final Player player) {
        return player instanceof FakePlayer;
    }

    default boolean dragonRenderingWasCancelled(final Player player) {
        return false;
    }

    @Nullable RegistryAccess getAccess();

    boolean isMining(final Player player);
}
