package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncBreathParticles;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public record BreathParticlesEffect(float spread, float speedPerSize, ParticleOptions mainParticle, ParticleOptions secondaryParticle) implements AbilityEntityEffect {
    private static final int DEFAULT_PARTICLE_COUNT = 20; // TODO :: add as parameter? of type (amount or have it calculated by size etc.)?

    public static final MapCodec<BreathParticlesEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("spread").forGetter(BreathParticlesEffect::spread),
            Codec.FLOAT.fieldOf("speed_per_size").forGetter(BreathParticlesEffect::speedPerSize),
            ParticleTypes.CODEC.fieldOf("main_particle").forGetter(BreathParticlesEffect::mainParticle),
            ParticleTypes.CODEC.fieldOf("secondary_particle").forGetter(BreathParticlesEffect::secondaryParticle)
    ).apply(instance, BreathParticlesEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        double particleAmount;

        if (target instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            particleAmount = handler.isDragon() ? handler.getSize() : DEFAULT_PARTICLE_COUNT;
        } else {
            particleAmount = DEFAULT_PARTICLE_COUNT;
        }

        particleAmount = Mth.clamp(particleAmount * 0.6, 12, 100);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(target, new SyncBreathParticles(target.getId(), spread, speedPerSize, (int) particleAmount, mainParticle, secondaryParticle));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
