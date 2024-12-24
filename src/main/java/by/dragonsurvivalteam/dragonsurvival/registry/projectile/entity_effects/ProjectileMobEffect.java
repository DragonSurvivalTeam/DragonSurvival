package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public record ProjectileMobEffect(HolderSet<MobEffect> effects, LevelBasedValue amplifier, LevelBasedValue duration, LevelBasedValue probability) implements ProjectileEntityEffect {
    public static final MapCodec<ProjectileMobEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(BuiltInRegistries.MOB_EFFECT.key()).fieldOf("effects").forGetter(ProjectileMobEffect::effects),
            LevelBasedValue.CODEC.fieldOf("amplifier").forGetter(ProjectileMobEffect::amplifier),
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(ProjectileMobEffect::duration),
            LevelBasedValue.CODEC.optionalFieldOf("probability", LevelBasedValue.constant(1)).forGetter(ProjectileMobEffect::probability)
    ).apply(instance, ProjectileMobEffect::new));

    @Override
    public void apply(Projectile projectile, Entity target, int projectileLevel) {
        if (target instanceof LivingEntity livingEntity) {
            effects().forEach(effect -> {
                if (livingEntity.getRandom().nextDouble() < probability().calculate(projectileLevel)) {
                    livingEntity.addEffect(new MobEffectInstance(effect, (int) duration().calculate(projectileLevel), (int) amplifier().calculate(projectileLevel)), projectile.getOwner());
                }
            });
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final int level) {
        List<MutableComponent> components = new ArrayList<>();
        double duration = Functions.ticksToSeconds((int) duration().calculate(level));

        for (Holder<MobEffect> effect : effects) {
            MutableComponent name = Component.literal("§6■ ").append(Component.translatable(LangKey.ABILITY_APPLIES).append(Component.translatable(effect.value().getDescriptionId())).withColor(DSColors.ORANGE));
            int amplifier = (int) amplifier().calculate(level);

            if (amplifier > 0) {
                name.append(Component.literal(Integer.toString(amplifier)).withColor(DSColors.ORANGE));
            }

            name.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.blue(duration)));
            float probability = probability().calculate(level);

            if (probability < 1) {
                name.append(Component.translatable(LangKey.ABILITY_EFFECT_CHANCE, DSColors.blue(NumberFormat.getPercentInstance().format(probability))));
            }

            components.add(name);
        }

        return components;
    }

    @Override
    public MapCodec<? extends ProjectileEntityEffect> entityCodec() {
        return CODEC;
    }
}
