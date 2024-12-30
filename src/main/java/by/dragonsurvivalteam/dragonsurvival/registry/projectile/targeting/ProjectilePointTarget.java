package by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public record ProjectilePointTarget(Either<Either<BlockTargeting, EntityTargeting>, ProjectileTargeting.WorldTargeting> target) implements ProjectileTargeting {
    public static final MapCodec<ProjectilePointTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.either(Codec.either(BlockTargeting.CODEC, EntityTargeting.CODEC), WorldTargeting.CODEC).fieldOf("target").forGetter(ProjectilePointTarget::target)
    ).apply(instance, ProjectilePointTarget::new));

    @Override
    public void apply(final Projectile projectile, int projectileLevel) {
        if (target.left().isPresent()) {
            Functions.logOrThrow("Point target must be a world target");
            return;
        }

        WorldTargeting target = this.target.right().get();
        Vec3 position = projectile.position();
        ServerLevel level = (ServerLevel) projectile.level();
        if(level.getGameTime() % target.tickRate() != 0) {
            return;
        }

        if(target.locationConditions().isPresent() && !target.locationConditions().get().matches(level, position.x, position.y, position.z)
                || target.weatherConditions().isPresent() && !target.weatherConditions().get().matches(level, position)
                || target.chance() < level.random.nextDouble()) {
            return;
        }

        target.effects().forEach(effect -> effect.apply(projectile, projectileLevel));
    }

    @Override
    public MutableComponent getDescription(Player dragon, int level) {
        MutableComponent description = Component.empty();
        if(tickRate() > 1) {
            description.append(Component.translatable(LangKey.ABILITY_X_SECONDS, tickRate() / 20.f));
        }
        return description;
    }

    @Override
    public MapCodec<? extends ProjectileTargeting> codec() {
        return CODEC;
    }
}
