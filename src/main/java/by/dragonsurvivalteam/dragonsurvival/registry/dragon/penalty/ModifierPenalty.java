package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record ModifierPenalty(List<ModifierWithDuration> modifiers) implements PenaltyEffect {
    public static final MapCodec<ModifierPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ModifierWithDuration.CODEC.listOf().fieldOf("modifiers").forGetter(ModifierPenalty::modifiers)
    ).apply(instance, ModifierPenalty::new));

    @Override
    public void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty) {
        ModifiersWithDuration modifiers = player.getData(DSDataAttachments.MODIFIERS_WITH_DURATION);

        for (ModifierWithDuration modifier : this.modifiers) {
            ModifierWithDuration.Instance instance = modifiers.get(modifier.id());
            int duration = (int) modifier.duration().calculate(1);

            if (instance != null && instance.currentDuration() == duration) {
                continue;
            }

            modifiers.remove(player, instance);
            modifiers.add(player, new ModifierWithDuration.Instance(modifier, CommonData.from(modifier.id(), player, penalty, modifier.customIcon(), modifier.shouldRemoveAutomatically()), duration));
        }
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
