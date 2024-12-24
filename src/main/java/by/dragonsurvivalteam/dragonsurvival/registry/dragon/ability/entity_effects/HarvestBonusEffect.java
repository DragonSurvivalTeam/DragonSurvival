package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.HarvestBonus;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public record HarvestBonusEffect(List<HarvestBonus> bonuses) implements AbilityEntityEffect{
    public static final MapCodec<HarvestBonusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HarvestBonus.CODEC.listOf().fieldOf("bonuses").forGetter(HarvestBonusEffect::bonuses)
    ).apply(instance, HarvestBonusEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            bonuses.forEach(bonus -> bonus.apply(dragon, ability, livingEntity));
        }
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            bonuses.forEach(bonus -> bonus.remove(livingEntity));
        }
    }

    public boolean shouldAppendSelfTargetingToDescription() {
        return false;
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        for(HarvestBonus bonus : bonuses) {
            components.add(Component.translatable(LangKey.ABILITY_HARVEST_LEVEL_BONUS, String.format("%.0f", bonus.bonus().calculate(ability.level()))));
        }

        return components;
    }

    public static List<AbilityEntityEffect> single(final HarvestBonus bonus) {
        return List.of(new HarvestBonusEffect(List.of(bonus)));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
