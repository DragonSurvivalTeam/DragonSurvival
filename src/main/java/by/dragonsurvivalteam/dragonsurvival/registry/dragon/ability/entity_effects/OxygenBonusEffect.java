package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public record OxygenBonusEffect(List<OxygenBonus> bonuses) implements AbilityEntityEffect {
    public static final MapCodec<OxygenBonusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            OxygenBonus.CODEC.listOf().fieldOf("bonuses").forGetter(OxygenBonusEffect::bonuses)
    ).apply(instance, OxygenBonusEffect::new));

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

        for (OxygenBonus bonus : bonuses) {
            components.add(bonus.getDescription(ability.level()));
        }

        return components;
    }

    public static List<AbilityEntityEffect> only(final OxygenBonus bonus) {
        return List.of(new OxygenBonusEffect(List.of(bonus)));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
