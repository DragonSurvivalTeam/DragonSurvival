package by.dragonsurvivalteam.dragonsurvival.util.proxy;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.FollowEntitySound;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationType;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmote;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ClientProxy implements Proxy {
    private final Map<ResourceLocation, TickableSoundInstance> soundInstances = new HashMap<>();

    @Override
    public @Nullable Player getLocalPlayer() {
        return Minecraft.getInstance().player;
    }

    @Override
    public @Nullable Level getLocalLevel() {
        Player player = Minecraft.getInstance().player;
        return player != null ? player.level() : null;
    }

    @Override
    public void playSoundAtEyeLevel(final Player player, final SoundEvent event) {
        Vec3 pos = player.getEyePosition(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        SimpleSoundInstance sound = new SimpleSoundInstance(event, SoundSource.PLAYERS, 1, 1, SoundInstance.createUnseededRandom(), pos.x, pos.y, pos.z);
        Minecraft.getInstance().getSoundManager().playDelayed(sound, 0);
    }

    @Override
    public void queueTickingSound(final ResourceLocation id, final SoundEvent soundEvent, final SoundSource soundSource, final Entity entity) {
        TickableSoundInstance sound = new FollowEntitySound(soundEvent, soundSource, entity);
        TickableSoundInstance previousSound = soundInstances.put(id, sound);

        if (previousSound != null) {
            Minecraft.getInstance().getSoundManager().stop(previousSound);
        }

        Minecraft.getInstance().getSoundManager().queueTickingSound(sound);
    }

    @Override
    public void stopTickingSound(final ResourceLocation id) {
        TickableSoundInstance instance = soundInstances.remove(id);

        if (instance != null) {
            Minecraft.getInstance().getSoundManager().stop(instance);
        }
    }

    @Override
    public void setCurrentAbilityAnimation(int playerId, final Pair<AbilityAnimation, AnimationType> animation) {
        AtomicReference<DragonEntity> dragonEntity = ClientDragonRenderer.playerDragonHashMap.get(playerId);

        if (dragonEntity == null) {
            return;
        }

        dragonEntity.get().setCurrentAbilityAnimation(animation);
    }

    @Override
    public void stopEmote(int playerId, final DragonEmote emote) {
        AtomicReference<DragonEntity> dragonEntity = ClientDragonRenderer.playerDragonHashMap.get(playerId);

        if (dragonEntity == null) {
            return;
        }

        dragonEntity.get().stopEmote(emote);
    }

    @Override
    public void beginPlayingEmote(int playerId, final DragonEmote emote) {
        AtomicReference<DragonEntity> dragonEntity = ClientDragonRenderer.playerDragonHashMap.get(playerId);

        if (dragonEntity == null) {
            return;
        }

        dragonEntity.get().beginPlayingEmote(emote);
    }

    @Override
    public void stopAllEmotes(int playerId) {
        AtomicReference<DragonEntity> dragonEntity = ClientDragonRenderer.playerDragonHashMap.get(playerId);

        if (dragonEntity == null) {
            return;
        }

        dragonEntity.get().stopAllEmotes();
    }

    @Override
    public boolean isOnRenderThread() {
        return RenderSystem.isOnRenderThread();
    }
}
