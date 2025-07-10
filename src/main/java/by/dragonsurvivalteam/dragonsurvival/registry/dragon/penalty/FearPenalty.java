package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Fear;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FearData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

public record FearPenalty(List<Fear> fears) implements PenaltyEffect {
    public static final MapCodec<FearPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Fear.CODEC.listOf().fieldOf("fears").forGetter(FearPenalty::fears)
    ).apply(instance, FearPenalty::new));

    @Override
    public void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty) {
        FearData data = player.getData(DSDataAttachments.FEAR);
        int growth = (int) DragonStateProvider.getData(player).getGrowth();

        fears.forEach(fear -> {
            Fear.Instance instance = data.get(fear.id());

            int newDuration = (int) fear.duration().calculate(1);
            int newDistance = (int) fear.distance().calculate(growth);
            float newWalkSpeed = fear.walkSpeed().calculate(growth);
            float newSprintSpeed = fear.sprintSpeed().calculate(growth);

            if (instance != null && instance.currentDuration() == newDuration && instance.distance() == newDistance && instance.walkSpeed() == newWalkSpeed && instance.sprintSpeed() == newSprintSpeed) {
                return;
            }

            data.remove(player, instance);
            data.add(player, new Fear.Instance(fear, CommonData.from(fear.id(), player, penalty, Optional.empty(), fear.shouldRemoveAutomatically()), newDuration, newDistance, newWalkSpeed, newSprintSpeed));
        });
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
