package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public record PotionEffect(PotionData potion) implements AbilityEntityEffect {
    public static final MapCodec<PotionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PotionData.CODEC.fieldOf("potion").forGetter(PotionEffect::potion)
    ).apply(instance, PotionEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        potion.apply(dragon, ability.level(), target);
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        return potion.getDescription(ability.level());
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        potion.remove(dragon, entity);
    }

    @SafeVarargs
    public static AbilityEntityEffect single(final LevelBasedValue amplifier, final LevelBasedValue duration, final boolean effectParticles, final Holder<MobEffect>... effects) {
        return single(amplifier, duration, LevelBasedValue.constant(1), effectParticles, effects);
    }

    @SafeVarargs
    public static AbilityEntityEffect single(final LevelBasedValue amplifier, final LevelBasedValue duration, final LevelBasedValue probability, final boolean effectParticles, final Holder<MobEffect>... effects) {
        return new PotionEffect(PotionData.of(amplifier, duration, probability, effectParticles, effects));
    }

    @SafeVarargs
    public static List<AbilityEntityEffect> only(final LevelBasedValue amplifier, final LevelBasedValue duration, final boolean effectParticles, final Holder<MobEffect>... effects) {
        return only(amplifier, duration, LevelBasedValue.constant(1), effectParticles, effects);
    }

    @SafeVarargs
    public static List<AbilityEntityEffect> only(final LevelBasedValue amplifier, final LevelBasedValue duration, final LevelBasedValue probability, final boolean effectParticles, final Holder<MobEffect>... effects) {
        return List.of(single(amplifier, duration, probability, effectParticles, effects));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
