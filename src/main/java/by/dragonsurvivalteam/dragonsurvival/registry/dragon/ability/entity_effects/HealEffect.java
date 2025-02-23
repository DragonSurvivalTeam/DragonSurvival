package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.text.NumberFormat;
import java.util.List;

/** Exists mainly because the instant health mob effect uses {@link LivingEntity#heal(float)} which is skipped if the health is at 0*/
public record HealEffect(LevelBasedValue percentage) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Heals§r the target for %s of their maximum health")
    private static final String HEALS_FOR = Translation.Type.GUI.wrap("heals_for");

    public static final MapCodec<HealEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("percentage").forGetter(HealEffect::percentage)
    ).apply(instance, HealEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        if (livingTarget.getHealth() > 0) {
            // Heal is skipped if the entity is dead (health = 0)
            livingTarget.heal(livingTarget.getMaxHealth() * percentage.calculate(ability.level()));
        } else {
            livingTarget.setHealth(livingTarget.getHealth() + livingTarget.getMaxHealth() * percentage.calculate(ability.level()));
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        return List.of(Component.translatable(HEALS_FOR, DSColors.dynamicValue(NumberFormat.getPercentInstance().format(percentage.calculate(ability.level())))));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
