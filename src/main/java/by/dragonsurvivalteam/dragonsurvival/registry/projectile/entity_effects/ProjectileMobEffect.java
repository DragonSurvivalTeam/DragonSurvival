package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;

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
    public MapCodec<? extends ProjectileEntityEffect> entityCodec() {
        return CODEC;
    }
}
