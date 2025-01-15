package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DamageModification;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

public record DamageModificationPenalty(DamageModification modification, int duration) implements PenaltyEffect {
    public static final MapCodec<DamageModificationPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageModification.CODEC.fieldOf("modification").forGetter(DamageModificationPenalty::modification),
            Codec.INT.fieldOf("duration").forGetter(DamageModificationPenalty::duration)
    ).apply(instance, DamageModificationPenalty::new));

    @Override
    public void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty) {
        DamageModifications modifications = player.getData(DSDataAttachments.DAMAGE_MODIFICATIONS);
        DamageModification.Instance instance = modifications.get(modification.base().id());

        if (instance != null && instance.currentDuration() == duration) {
            return;
        }

        modifications.remove(player, instance);
        modifications.add(player, new DamageModification.Instance(modification, ClientEffectProvider.ClientData.from(penalty), 1, duration));
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
