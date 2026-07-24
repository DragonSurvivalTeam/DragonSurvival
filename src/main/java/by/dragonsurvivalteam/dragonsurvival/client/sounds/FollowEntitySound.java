package by.dragonsurvivalteam.dragonsurvival.client.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class FollowEntitySound extends AbstractTickableSoundInstance {
    private final Entity entity;

    public FollowEntitySound(SoundEvent event, SoundSource source, Entity entity) {
        super(event, source, entity.getRandom());
        looping = true;
        this.entity = entity;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public void tick() {
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
