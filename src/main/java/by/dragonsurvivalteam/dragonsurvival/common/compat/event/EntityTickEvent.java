package by.dragonsurvivalteam.dragonsurvival.common.compat.event;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public class EntityTickEvent extends Event {
    private final Entity entity;

    protected EntityTickEvent(final Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public static class Pre extends EntityTickEvent {
        public Pre(final Entity entity) {
            super(entity);
        }
    }

    public static class Post extends EntityTickEvent {
        public Post(final Entity entity) {
            super(entity);
        }
    }
}
