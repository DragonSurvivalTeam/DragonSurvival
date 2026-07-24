package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.EffectModification;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectModifications;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record EffectModificationPenalty(List<EffectModification> modifications) implements PenaltyEffect {
    public static final MapCodec<EffectModificationPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EffectModification.CODEC.listOf().fieldOf("modifications").forGetter(EffectModificationPenalty::modifications)
    ).apply(instance, EffectModificationPenalty::new));

    @Override
    public void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty) {
        EffectModifications modifications = player.getData(DSDataAttachments.EFFECT_MODIFICATIONS);

        for (EffectModification modification : this.modifications) {
            EffectModification.Instance instance = modifications.get(modification.id());
            int duration = (int) modification.duration().calculate(1);

            if (instance != null && instance.currentDuration() == duration) {
                continue;
            }

            modifications.remove(player, instance);
            modifications.add(player, new EffectModification.Instance(modification, CommonData.from(modification.id(), player, penalty, modification.customIcon(), modification.shouldRemoveAutomatically()), duration));
        }
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
