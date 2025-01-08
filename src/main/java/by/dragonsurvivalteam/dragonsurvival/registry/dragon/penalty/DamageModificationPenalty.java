package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DamageModification;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public record DamageModificationPenalty(DamageModification modification, int duration) implements PenaltyEffect {
    @Translation(comments = "Damage Modification Penalty") // Not important enough for a separate field in the codec / passing down the penalty holder
    private static final String NAME = Translation.Type.GUI.wrap("penalty.damage_modification.name");

    public static final MapCodec<DamageModificationPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageModification.CODEC.fieldOf("modification").forGetter(DamageModificationPenalty::modification),
            Codec.INT.fieldOf("duration").forGetter(DamageModificationPenalty::duration)
    ).apply(instance, DamageModificationPenalty::new));

    private static final ClientEffectProvider.ClientData CLIENT_DATA = new ClientEffectProvider.ClientData(ModifierWithDuration.DEFAULT_MODIFIER_ICON, Component.translatable(NAME), Component.empty());

    @Override
    public void apply(final ServerPlayer player) {
        DamageModifications modifications = player.getData(DSDataAttachments.DAMAGE_MODIFICATIONS);
        DamageModification.Instance instance = modifications.get(modification.id());

        if (instance != null && instance.currentDuration() == duration) {
            return;
        }

        // TODO :: can packets run into race conditions?
        //  since they share the same id if the removal packet arrives after the add one the client would be missing the data
        //  (so far this issue doesn't seem to have occurred?)
        //  for some storage entries we need to make sure to call remove though (modifier most importantly)
        modifications.remove(player, instance);
        modifications.add(player, new DamageModification.Instance(modification, CLIENT_DATA, 1, duration));
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
