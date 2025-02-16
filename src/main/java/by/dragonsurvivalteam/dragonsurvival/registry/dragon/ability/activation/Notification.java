package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.Optional;

public record Notification(Component notEnoughMana, Optional<Component> usageBlocked) {
    @Translation(comments = "§fNot enough§r §cmana or experience§r!")
    public static final String NO_MANA = Translation.Type.GUI.wrap("ability.no_mana");
    public static final Component NO_MANA_MESSAGE = Component.translatable(NO_MANA).withStyle(ChatFormatting.RED);

    public static Codec<Notification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("not_enough_mana", NO_MANA_MESSAGE).forGetter(Notification::notEnoughMana),
            ComponentSerialization.CODEC.optionalFieldOf("usage_blocked").forGetter(Notification::usageBlocked)
    ).apply(instance, Notification::new));

    public static final Notification DEFAULT = new Notification(NO_MANA_MESSAGE, Optional.empty());
    public static final Notification NONE = new Notification(Component.empty(), Optional.empty());
}
