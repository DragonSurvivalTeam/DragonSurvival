package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericArrowEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericBallEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileTargeting;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public record ProjectileEffect(Holder<ProjectileData> projectileData, TargetDirection targetDirection, LevelBasedValue numberOfProjectiles, LevelBasedValue projectileSpread, LevelBasedValue speed) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Number of projectiles:§r %s")
    private static final String ABILITY_PROJECTILE_COUNT = Translation.Type.GUI.wrap("projectile.count");

    @Translation(comments = "§6■ Projectile Speed:§r %s")
    private static final String ABILITY_PROJECTILE_SPEED = Translation.Type.GUI.wrap("projectile.speed");

    @Translation(comments = "§6■ Projectile Spread:§r %s")
    private static final String ABILITY_PROJECTILE_SPREAD = Translation.Type.GUI.wrap("projectile.spread");

    public static final MapCodec<ProjectileEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProjectileData.CODEC.fieldOf("projectile_data").forGetter(ProjectileEffect::projectileData),
            TargetDirection.CODEC.fieldOf("target_direction").forGetter(ProjectileEffect::targetDirection),
            LevelBasedValue.CODEC.fieldOf("number_of_projectiles").forGetter(ProjectileEffect::numberOfProjectiles),
            LevelBasedValue.CODEC.optionalFieldOf("projectile_spread", LevelBasedValue.constant(0)).forGetter(ProjectileEffect::projectileSpread),
            LevelBasedValue.CODEC.fieldOf("speed").forGetter(ProjectileEffect::speed)
    ).apply(instance, ProjectileEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        ProjectileData projectileData = projectileData().value();
        Either<ProjectileData.GenericBallData, ProjectileData.GenericArrowData> specificData = projectileData.typeData();

        float speed = this.speed.calculate(ability.level());
        BiConsumer<Projectile, Float> shootLogic = getShootLogic(dragon, entity, speed);

        // It doesn't make sense to spawn the projectile at the entity position and then make it move towards said entity
        boolean useEntityPosition = targetDirection.direction().left().orElse(null) != TargetDirection.Type.TOWARDS_ENTITY;

        specificData.ifLeft(data -> {
            for (int count = 0; count < numberOfProjectiles.calculate(ability.level()); count++) {
                Vec3 launchPosition;

                if (useEntityPosition) {
                    int scale = 1;

                    if (entity instanceof Player player && player.getAbilities().flying) {
                        scale = 2;
                    }

                    launchPosition = entity.getLookAngle().scale(scale).add(entity.getEyePosition());
                } else {
                    int scale = dragon.getAbilities().flying ? 2 : 1;
                    launchPosition = dragon.getLookAngle().scale(scale).add(dragon.getEyePosition());
                }

                GenericBallEntity projectile = new GenericBallEntity(projectileData.generalData(), data, ability.level(), launchPosition, dragon.serverLevel());
                projectile.setOwner(entity);
                projectile.accelerationPower = 0;

                float spread = count * projectileSpread.calculate(ability.level());
                shootLogic.accept(projectile, spread);
                entity.level().addFreshEntity(projectile);
            }
        }).ifRight(data -> {
            for (int i = 0; i < numberOfProjectiles.calculate(ability.level()); i++) {
                Vec3 launchPosition;

                if (useEntityPosition) {
                    launchPosition = new Vec3(entity.getX(), entity.getEyeY() - 0.1f, entity.getZ());
                } else {
                    launchPosition = new Vec3(dragon.getX(), dragon.getEyeY() - 0.1f, dragon.getZ());
                }

                GenericArrowEntity arrow = new GenericArrowEntity(projectileData.generalData(), data, ability.level(), launchPosition, dragon.serverLevel());
                arrow.setOwner(entity);
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

                float spread = i * projectileSpread.calculate(ability.level());
                shootLogic.accept(arrow, spread);
                entity.level().addFreshEntity(arrow);
            }
        });
    }

    private BiConsumer<Projectile, Float> getShootLogic(final ServerPlayer dragon, final Entity entity, float speed) {
        BiConsumer<Projectile, Float> shootLogic;

        if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.LOOKING_AT) {
            shootLogic = (arrow, spread) -> arrow.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0, speed, spread);
        } else if (targetDirection.direction().right().isPresent()) {
            Direction direction = targetDirection.direction().right().get();
            shootLogic = (arrow, spread) -> arrow.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), speed, spread);
        } else {
            shootLogic = (arrow, spread) -> {
                Vec3 target = dragon.position().vectorTo(entity.position());
                arrow.shoot(target.x(), target.y(), target.z(), speed, spread);
            };
        }

        return shootLogic;
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        for (ProjectileEntityEffect entityHitEffect : projectileData().value().generalData().entityHitEffects()) {
            List<MutableComponent> effectComponents = entityHitEffect.getDescription(dragon, ability.level());
            components.addAll(effectComponents);
        }

        for (ProjectileBlockEffect blockHitEffect : projectileData().value().generalData().blockHitEffects()) {
            List<MutableComponent> effectComponents = blockHitEffect.getDescription(dragon, ability.level());
            components.addAll(effectComponents);
        }

        for (ProjectileTargeting tickingEffect : projectileData().value().generalData().tickingEffects()) {
            List<MutableComponent> effectComponents = tickingEffect.getAllEffectDescriptions(dragon, ability.level());
            components.addAll(effectComponents);
        }

        for (ProjectileTargeting commonEffect : projectileData().value().generalData().commonHitEffects()) {
            List<MutableComponent> effectComponents = commonEffect.getAllEffectDescriptions(dragon, ability.level());
            components.addAll(effectComponents);
        }

        if (projectileData().value().typeData().left().isPresent()) {
            for (ProjectileTargeting onDestroyEffect : projectileData().value().typeData().left().get().onDestroyEffects()) {
                List<MutableComponent> effectComponents = onDestroyEffect.getAllEffectDescriptions(dragon, ability.level());
                components.addAll(effectComponents);
            }
        }

        if (numberOfProjectiles.calculate(ability.level()) > 1) {
            components.add(Component.translatable(ABILITY_PROJECTILE_COUNT, numberOfProjectiles.calculate(ability.level())));
        }

        if (projectileSpread.calculate(ability.level()) > 0) {
            components.add(Component.translatable(ABILITY_PROJECTILE_SPREAD, projectileSpread.calculate(ability.level())));
        }

        components.add(Component.translatable(ABILITY_PROJECTILE_SPEED, speed.calculate(ability.level())));

        return components;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
