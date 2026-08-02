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
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public record ProjectileEffect(
        Optional<Holder<ProjectileData>> projectileData,
        Optional<Holder<EntityType<?>>> projectileType,
        TargetDirection targetDirection,
        LevelBasedValue numberOfProjectiles,
        LevelBasedValue projectileSpread,
        LevelBasedValue speed
) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Projectile type:§r %s")
    private static final String ABILITY_PROJECTILE_TYPE = Translation.Type.GUI.wrap("projectile.type");

    @Translation(comments = "§6■ Number of projectiles:§r %s")
    private static final String ABILITY_PROJECTILE_COUNT = Translation.Type.GUI.wrap("projectile.count");

    @Translation(comments = "§6■ Projectile Speed:§r %s")
    private static final String ABILITY_PROJECTILE_SPEED = Translation.Type.GUI.wrap("projectile.speed");

    @Translation(comments = "§6■ Projectile Spread:§r %s")
    private static final String ABILITY_PROJECTILE_SPREAD = Translation.Type.GUI.wrap("projectile.spread");

    public static final MapCodec<ProjectileEffect> CODEC = RecordCodecBuilder./* Need to specify otherwise the IDE can't handle validate */<ProjectileEffect>mapCodec(instance -> instance.group(
            // Since both are registry references we can't use either since it will just crash after trying the first codec, saying registry entry does not exist
            ProjectileData.CODEC.optionalFieldOf("projectile_data").forGetter(ProjectileEffect::projectileData),
            BuiltInRegistries.ENTITY_TYPE.holderByNameCodec().optionalFieldOf("projectile_type").forGetter(ProjectileEffect::projectileType),
            TargetDirection.CODEC.fieldOf("target_direction").forGetter(ProjectileEffect::targetDirection),
            LevelBasedValue.CODEC.fieldOf("number_of_projectiles").forGetter(ProjectileEffect::numberOfProjectiles),
            LevelBasedValue.CODEC.optionalFieldOf("projectile_spread", LevelBasedValue.constant(0)).forGetter(ProjectileEffect::projectileSpread),
            LevelBasedValue.CODEC.fieldOf("speed").forGetter(ProjectileEffect::speed)
    ).apply(instance, ProjectileEffect::new)).validate(
            data -> data.projectileData.isEmpty() && data.projectileType.isEmpty() ?
                    DataResult.error(() -> "Need to specify either 'projectile_data' or 'projectile_type'") :
                    DataResult.success(data)
    );

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        float speed = this.speed.calculate(ability.level());
        float amount = numberOfProjectiles.calculate(ability.level());
        float spread = projectileSpread.calculate(ability.level());

        BiConsumer<Projectile, Float> shootLogic = getShootLogic(dragon, target, speed);

        // It doesn't make sense to spawn the projectile at the entity position and then make it move towards said entity
        boolean useEntityPosition = targetDirection.direction().left().orElse(null) != TargetDirection.Type.TOWARDS_ENTITY;

        projectileData.ifPresent(projectileData -> {
            Either<ProjectileData.GenericBallData, ProjectileData.GenericArrowData> specificData = projectileData.value().typeData();

            specificData.ifLeft(data -> {
                Vec3 launchPosition;

                if (useEntityPosition) {
                    int scale = 1;

                    if (target instanceof Player player && player.getAbilities().flying) {
                        scale = 2;
                    }

                    launchPosition = target.getLookAngle().scale(scale).add(target.getEyePosition());
                } else {
                    int scale = dragon.getAbilities().flying ? 2 : 1;
                    launchPosition = dragon.getLookAngle().scale(scale).add(dragon.getEyePosition());
                }

                for (int count = 0; count < amount; count++) {
                    GenericBallEntity projectile = new GenericBallEntity(projectileData.value().generalData(), data, ability.level(), launchPosition, dragon.serverLevel());
                    projectile.setOwner(target);
                    projectile.accelerationPower = 0;

                    shootLogic.accept(projectile, spread * count);
                    target.level().addFreshEntity(projectile);
                }
            }).ifRight(data -> {
                Vec3 launchPosition;

                if (useEntityPosition) {
                    launchPosition = new Vec3(target.getX(), target.getEyeY() - 0.1f, target.getZ());
                } else {
                    launchPosition = new Vec3(dragon.getX(), dragon.getEyeY() - 0.1f, dragon.getZ());
                }

                for (int count = 0; count < amount; count++) {
                    GenericArrowEntity arrow = new GenericArrowEntity(projectileData.value().generalData(), data, ability.level(), launchPosition, dragon.serverLevel());
                    arrow.setOwner(target);
                    arrow.pickup = AbstractArrow.Pickup.DISALLOWED;

                    shootLogic.accept(arrow, spread * count);
                    target.level().addFreshEntity(arrow);
                }
            });
        });

        projectileType.ifPresent(entity -> {
            Vec3 launchPosition;

            if (useEntityPosition) {
                int scale = 1;

                if (target instanceof Player player && player.getAbilities().flying) {
                    scale = 2;
                }

                launchPosition = target.getLookAngle().scale(scale).add(target.getEyePosition());
            } else {
                int scale = dragon.getAbilities().flying ? 2 : 1;
                launchPosition = dragon.getLookAngle().scale(scale).add(dragon.getEyePosition());
            }

            for (int count = 0; count < amount; count++) {
                // TODO :: add support for all types of entities
                if (entity.value().create(dragon.serverLevel()) instanceof Projectile projectile) {
                    projectile.setOwner(dragon);
                    projectile.setPos(launchPosition);

                    if (projectile instanceof GenericArrowEntity arrow) {
                        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    }

                    // TODO :: currently shot projectiles can collide with each other (e.g. ghast fireball hitting another and exploding)
                    shootLogic.accept(projectile, spread * count);
                    target.level().addFreshEntity(projectile);
                }
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

        projectileData.ifPresent(projectileData -> {
            for (ProjectileEntityEffect entityHitEffect : projectileData.value().generalData().entityHitEffects()) {
                List<MutableComponent> effectComponents = entityHitEffect.getDescription(dragon, ability.level());
                components.addAll(effectComponents);
            }

            for (ProjectileBlockEffect blockHitEffect : projectileData.value().generalData().blockHitEffects()) {
                List<MutableComponent> effectComponents = blockHitEffect.getDescription(dragon, ability.level());
                components.addAll(effectComponents);
            }

            for (ProjectileTargeting tickingEffect : projectileData.value().generalData().tickingEffects()) {
                List<MutableComponent> effectComponents = tickingEffect.getAllEffectDescriptions(dragon, ability.level());
                components.addAll(effectComponents);
            }

            for (ProjectileTargeting commonEffect : projectileData.value().generalData().commonHitEffects()) {
                List<MutableComponent> effectComponents = commonEffect.getAllEffectDescriptions(dragon, ability.level());
                components.addAll(effectComponents);
            }

            if (projectileData.value().typeData().left().isPresent()) {
                for (ProjectileTargeting onDestroyEffect : projectileData.value().typeData().left().get().onDestroyEffects()) {
                    List<MutableComponent> effectComponents = onDestroyEffect.getAllEffectDescriptions(dragon, ability.level());
                    components.addAll(effectComponents);
                }
            }
        });

        projectileType.ifPresent(entity -> {
            components.add(Component.translatable(ABILITY_PROJECTILE_TYPE, DSColors.dynamicValue(Component.translatable(entity.value().getDescriptionId()))));
        });

        if (numberOfProjectiles.calculate(ability.level()) > 1) {
            components.add(Component.translatable(ABILITY_PROJECTILE_COUNT, DSColors.dynamicValue(numberOfProjectiles.calculate(ability.level()))));
        }

        if (projectileSpread.calculate(ability.level()) > 0) {
            components.add(Component.translatable(ABILITY_PROJECTILE_SPREAD, DSColors.dynamicValue(projectileSpread.calculate(ability.level()))));
        }

        components.add(Component.translatable(ABILITY_PROJECTILE_SPEED, DSColors.dynamicValue(speed.calculate(ability.level()))));

        return components;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
