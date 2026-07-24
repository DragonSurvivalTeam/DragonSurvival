package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;

public record ConstantTrigger() implements ActivationTrigger<Void> {
    @Translation(comments = "Constant")
    private static final String TRANSLATION = Translation.Type.TRIGGER_TYPE.wrap("constant");

    public static final ConstantTrigger INSTANCE = new ConstantTrigger();
    public static final MapCodec<ConstantTrigger> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public Component translation() {
        return Component.translatable(TRANSLATION);
    }

    @Override
    public MapCodec<? extends ActivationTrigger<?>> codec() {
        return CODEC;
    }
}
