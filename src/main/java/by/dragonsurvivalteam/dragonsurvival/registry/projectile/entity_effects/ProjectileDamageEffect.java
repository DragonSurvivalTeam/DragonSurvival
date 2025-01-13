package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public record ProjectileDamageEffect(Holder<DamageType> damageType, LevelBasedValue amount) implements ProjectileEntityEffect {
    @Translation(comments = "§6■ %s §6Projectile Damage:§r %s")
    private static final String ABILITY_PROJECTILE_DAMAGE = Translation.Type.GUI.wrap("projectile.damage_effect");

    public static final MapCodec<ProjectileDamageEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageType.CODEC.fieldOf("damage_type").forGetter(ProjectileDamageEffect::damageType),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(ProjectileDamageEffect::amount)

    ).apply(instance, ProjectileDamageEffect::new));

    @Override
    public void apply(final Projectile projectile, final Entity target, final int level) {
        target.hurt(new DamageSource(damageType, projectile, projectile.getOwner()), amount.calculate(level));

        if (projectile.getOwner() instanceof LivingEntity entity) {
            // Used by 'OwnerHurtTargetGoal'
            entity.setLastHurtMob(target);
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final int level) {
        //noinspection DataFlowIssue -> key is present
        MutableComponent translation = Component.translatable(Translation.Type.DAMAGE_TYPE.wrap(damageType.getKey().location()));
        return List.of(Component.translatable(ABILITY_PROJECTILE_DAMAGE, translation.withColor(DSColors.GOLD), amount.calculate(level)));
    }

    @Override
    public MapCodec<? extends ProjectileEntityEffect> codec() {
        return CODEC;
    }
}
