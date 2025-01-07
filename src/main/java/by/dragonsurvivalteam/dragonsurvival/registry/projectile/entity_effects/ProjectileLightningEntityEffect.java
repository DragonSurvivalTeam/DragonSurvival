package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.LightningHandler;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.Projectile;

public record ProjectileLightningEntityEffect(LightningHandler.Data data) implements ProjectileEntityEffect {
    public static final MapCodec<ProjectileLightningEntityEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    LightningHandler.Data.CODEC.fieldOf("data").forGetter(ProjectileLightningEntityEffect::data)
            ).apply(instance, ProjectileLightningEntityEffect::new)
    );

    @Override
    public void apply(final Projectile projectile, final Entity target, final int level) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(projectile.level());
        bolt.moveTo(target.position());

        if (projectile.getOwner() instanceof ServerPlayer serverPlayer) {
            bolt.setCause(serverPlayer);
        }

        bolt.setData(DSDataAttachments.LIGHTNING_BOLT, LightningHandler.fromData(data));
        projectile.level().addFreshEntity(bolt);
    }

    @Override
    public MapCodec<? extends ProjectileEntityEffect> codec() {
        return CODEC;
    }
}
