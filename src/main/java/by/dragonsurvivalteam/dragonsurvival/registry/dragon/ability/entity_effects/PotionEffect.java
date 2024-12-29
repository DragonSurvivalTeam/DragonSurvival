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
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        potion.apply(dragon, ability.level(), entity);
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
    public static List<AbilityEntityEffect> single(final LevelBasedValue amplifier, final LevelBasedValue duration, final Holder<MobEffect>... effects) {
        return single(amplifier, duration, LevelBasedValue.constant(1), effects);
    }

    @SafeVarargs
    public static List<AbilityEntityEffect> single(final LevelBasedValue amplifier, final LevelBasedValue duration, final LevelBasedValue probability, final Holder<MobEffect>... effects) {
        return List.of(new PotionEffect(PotionData.of(amplifier, duration, probability, effects)));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
