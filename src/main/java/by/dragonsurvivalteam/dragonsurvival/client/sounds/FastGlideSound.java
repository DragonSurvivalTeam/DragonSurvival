package by.dragonsurvivalteam.dragonsurvival.client.sounds;

import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.Shapes;

public class FastGlideSound extends ElytraOnPlayerSoundInstance {
    private final LocalPlayer player;
    private int time;

    public FastGlideSound(final LocalPlayer player) {
        super(player);
        this.player = player;
        looping = true;
        delay = 0;
        volume = 0.1f;
    }

    @Override
    public void tick() {
        time++;

        if (!player.isRemoved() && (time <= 20 || ServerFlightHandler.isGliding(player))) {
            x = (float) player.getX();
            y = (float) player.getY();
            z = (float) player.getZ();

            double movementStrength = player.getDeltaMovement().lengthSqr();

            if (movementStrength >= Shapes.EPSILON) {
                volume = (float) Mth.clamp(movementStrength / 4, 0, 1);
            } else {
                volume = 0;
            }

            if (time < 20) {
                volume = 0;
            } else if (time < 40) {
                volume = (volume * ((float) (time - 20) / 20));
            }

            if (volume > 0.8) {
                pitch = 1 + (volume - 0.8f);
            } else {
                pitch = 1;
            }
        } else {
            stop();
        }
    }
}