package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class ItemData implements ValueIOSerializable {
    private static final int SMELT_PROGRESS_RESET = Functions.secondsToTicks(3);

    public boolean isFireImmune;

    public double smeltingTime;
    public double smeltingProgress;

    private double previousSmeltingProgress;
    private int noSmeltingChange;

    @SubscribeEvent // Resets progress after inactivity and spawns the smoke particles
    public static void handleSmelting(final EntityTickEvent.Post event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.ITEM).ifPresent(data -> {
            if (data.smeltingTime == 0) {
                return;
            }

            if (data.smeltingProgress > 0 && data.smeltingProgress == data.previousSmeltingProgress) {
                data.noSmeltingChange++;

                if (data.noSmeltingChange == SMELT_PROGRESS_RESET) {
                    data.smeltingProgress = 0;
                    data.noSmeltingChange = 0;

                    TagValueOutput tagValueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, serverLevel.registryAccess());
                    data.serialize(tagValueOutput);

                    PacketDistributor.sendToPlayersNear(serverLevel, null, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), 16, new SyncData(event.getEntity().getId(), DSDataAttachments.ITEM.getId(), tagValueOutput.buildResult()));
                }
            } else {
                data.noSmeltingChange = 0;
            }

            data.previousSmeltingProgress = data.smeltingProgress;
        });
    }

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {
        valueOutput.putBoolean(IS_FIRE_IMMUNE, isFireImmune);
        valueOutput.putDouble(SMELTING_TIME, smeltingTime);
        valueOutput.putDouble(SMELTING_PROGRESS, smeltingProgress);
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        isFireImmune = valueInput.getBooleanOr(IS_FIRE_IMMUNE, false);
        smeltingTime = valueInput.getDoubleOr(SMELTING_TIME, 0.0);
        smeltingProgress = valueInput.getDoubleOr(SMELTING_PROGRESS, 0.0);
    }

    public static final String IS_FIRE_IMMUNE = "is_fire_immune";
    public static final String SMELTING_TIME = "smelting_time";
    public static final String SMELTING_PROGRESS = "smelting_progress";
}
