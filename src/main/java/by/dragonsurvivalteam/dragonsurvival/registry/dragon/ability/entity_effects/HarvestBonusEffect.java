package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.HarvestBonus;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public record HarvestBonusEffect(List<HarvestBonus> bonuses) implements AbilityEntityEffect {
    public static final MapCodec<HarvestBonusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HarvestBonus.CODEC.listOf().fieldOf("harvest_bonuses").forGetter(HarvestBonusEffect::bonuses)
    ).apply(instance, HarvestBonusEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target instanceof Player player) {
            bonuses.forEach(bonus -> bonus.apply(dragon, ability, player));
        }
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity, final boolean isAutoRemoval) {
        if (entity instanceof Player player) {
            bonuses.forEach(bonus -> {
                if (!isAutoRemoval || bonus.shouldRemoveAutomatically()) {
                    bonus.remove(player);
                }
            });
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        for (HarvestBonus bonus : bonuses) {
            components.add(bonus.getDescription(ability.level()));
        }

        return components;
    }

    public static List<AbilityEntityEffect> only(final HarvestBonus bonus) {
        return List.of(new HarvestBonusEffect(List.of(bonus)));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
