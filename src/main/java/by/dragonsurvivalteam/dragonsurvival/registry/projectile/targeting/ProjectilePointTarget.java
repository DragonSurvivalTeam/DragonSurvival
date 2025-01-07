package by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

public record ProjectilePointTarget(GeneralData generalData) implements ProjectileTargeting {
    public static final MapCodec<ProjectilePointTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> ProjectileTargeting.codecStart(instance).apply(instance, ProjectilePointTarget::new));

    @Override
    public void apply(final Projectile projectile, int projectileLevel) {
        if (projectile.level().getGameTime() % generalData.tickRate() != 0 || generalData.chance() < projectile.getRandom().nextDouble()) {
            return;
        }

        generalData.effects().forEach(effect -> effect.apply((ServerLevel) projectile.level(), projectile, null, projectileLevel));
    }

    @Override
    public MutableComponent getDescription(Player dragon, int level) {
        MutableComponent description = Component.empty();

        if (generalData.tickRate() > 1) {
            description.append(Component.translatable(LangKey.ABILITY_X_SECONDS, Functions.ticksToSeconds(generalData.tickRate())));
        }

        return description;
    }

    @Override
    public MapCodec<? extends ProjectileTargeting> codec() {
        return CODEC;
    }
}
