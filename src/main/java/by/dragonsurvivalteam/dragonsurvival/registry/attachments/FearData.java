package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Fear;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class FearData extends Storage<Fear.Instance> {
    public static final int NO_FEAR = -1;

    public record Data(int distance, float walkSpeed, float sprintSpeed) {}

    public Data getData(final Entity entity) {
        int distance = NO_FEAR;
        float walkSpeed = Fear.DEFAULT_WALK_SPEED;
        float sprintSpeed = Fear.DEFAULT_SPRINT_SPEED;

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return new Data(distance, walkSpeed, sprintSpeed);
        }

        for (Fear.Instance instance : all()) {
            if (instance.distance() == NO_FEAR) {
                continue;
            }

            if (instance.baseData().entityCondition().map(condition -> condition.test(Condition.entityContext(serverLevel, entity))).orElse(true)) {
                if (instance.distance() > distance) {
                    distance = instance.distance();
                }

                if (instance.walkSpeed() > walkSpeed) {
                    walkSpeed = instance.walkSpeed();
                }

                if (instance.sprintSpeed() > sprintSpeed) {
                    sprintSpeed = instance.sprintSpeed();
                }
            }
        }

        return new Data(distance, walkSpeed, sprintSpeed);
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            player.getExistingData(DSDataAttachments.FEAR).ifPresent(storage -> {
                storage.tick(player);

                if (storage.isEmpty()) {
                    player.removeData(storage.type());
                }
            });
        }
    }

    @Override
    protected void save(@NotNull ValueOutput valueOutput, final Fear.Instance entry, final String key) {
        entry.save(valueOutput, key);
    }

    @Override
    protected Fear.Instance load(@NotNull ValueInput valueInput, final String key) {
        return Fear.Instance.load(valueInput, key);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.FEAR.value();
    }
}
