package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.HashSet;

public record OnKeyPressed(HashSet<String> keys) implements ActivationTrigger<String> {
    @Translation(comments = "On Key Pressed: %s")
    private static final String TRANSLATION = Translation.Type.TRIGGER_TYPE.wrap("on_key_pressed");

    public static final MapCodec<OnKeyPressed> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("keys").xmap(HashSet::new, set -> set.stream().toList()).forGetter(OnKeyPressed::keys)
    ).apply(instance, OnKeyPressed::new));

    public static OnKeyPressed create(final String... keys) {
        HashSet<String> set = new HashSet<>();
        Collections.addAll(set, keys);
        return new OnKeyPressed(set);
    }

    @Override
    public boolean test(final String testContext) {
        return keys.contains(testContext);
    }

    @Override
    public Component translation() {
        return Component.translatable(TRANSLATION, DSColors.dynamicValue(keys));
    }

    @Override
    public MapCodec<? extends ActivationTrigger<?>> codec() {
        return CODEC;
    }
}
