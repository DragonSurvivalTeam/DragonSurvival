package by.dragonsurvivalteam.dragonsurvival.client.emotes;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.StopAllEmotes;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmote;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.client.CameraType.THIRD_PERSON_BACK;

@EventBusSubscriber(Dist.CLIENT)
public class EmoteHandler {

    private static final double EMOTE_MOVEMENT_EPSILON = 0.01;

    @SubscribeEvent
    public static void playerTick(final PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        AtomicReference<DragonEntity> atomicDragon = ClientDragonRenderer.PLAYER_DRAGON_MAP.get(player.getId());
        if(atomicDragon == null) {
            return;
        }

        DragonEntity dragon = atomicDragon.get();
        if (player.isCrouching() || player.swinging) {
            dragon.stopAllEmotes();
            PacketDistributor.sendToServer(new StopAllEmotes(player.getId()));
            return;
        }

        DragonEmote[] currentlyPlayingEmotes = dragon.getCurrentlyPlayingEmotes();
        boolean playerIsMoving = player.getDeltaMovement().lengthSqr() > EMOTE_MOVEMENT_EPSILON;
        for(int i = 0; i < currentlyPlayingEmotes.length; i++) {
            if(currentlyPlayingEmotes[i] != null) {
                if(!currentlyPlayingEmotes[i].canMove() && playerIsMoving) {
                    PacketDistributor.sendToServer(new SyncEmote(player.getId(), currentlyPlayingEmotes[i], true));
                    dragon.stopEmote(i);
                    continue;
                }

                if(currentlyPlayingEmotes[i].sound().isPresent()) {
                    DragonEmote.Sound sound = currentlyPlayingEmotes[i].sound().get();
                    if(dragon.getTicksForEmote(i) % sound.interval() == 0) {
                        sound.playSound(player);
                    }
                }

                if (currentlyPlayingEmotes[i].thirdPerson()) {
                    Minecraft.getInstance().levelRenderer.needsUpdate();
                    CameraType pointofview = Minecraft.getInstance().options.getCameraType();

                    if (pointofview.isFirstPerson()) {
                        Minecraft.getInstance().options.setCameraType(THIRD_PERSON_BACK);

                        if (pointofview.isFirstPerson() != Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                            Minecraft.getInstance().gameRenderer.checkEntityPostEffect(Minecraft.getInstance().options.getCameraType().isFirstPerson() ? Minecraft.getInstance().getCameraEntity() : null);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerAttacked(final LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && DragonStateProvider.isDragon(player)) {
            AtomicReference<DragonEntity> atomicDragon = ClientDragonRenderer.PLAYER_DRAGON_MAP.get(player.getId());
            if(atomicDragon == null) {
                return;
            }

            DragonEntity dragon = ClientDragonRenderer.PLAYER_DRAGON_MAP.get(player.getId()).get();
            dragon.stopAllEmotes();
            PacketDistributor.sendToServer(new StopAllEmotes(player.getId()));
        }
    }
}