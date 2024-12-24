package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.List;

public record ProjectilePotionEffect(PotionData potion) implements ProjectileEntityEffect {
    public static final MapCodec<ProjectilePotionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PotionData.CODEC.fieldOf("potion").forGetter(ProjectilePotionEffect::potion)
    ).apply(instance, ProjectilePotionEffect::new));

    @Override
    public void apply(Projectile projectile, Entity target, int projectileLevel) {
        if(projectile.getOwner() instanceof ServerPlayer) {
            potion.apply((ServerPlayer) projectile.getOwner(), projectileLevel, target);
        } else {
            potion.apply(null, projectileLevel, target);
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final int level) {
        return potion.getDescription(level);
    }

    @Override
    public MapCodec<? extends ProjectileEntityEffect> entityCodec() {
        return CODEC;
    }
}
