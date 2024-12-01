package by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.LightningHandler;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.Projectile;

public record ProjectileLightningWorldEffect(LightningHandler.Data data) implements ProjectileWorldEffect {

    public static final MapCodec<ProjectileLightningWorldEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    LightningHandler.Data.CODEC.fieldOf("data").forGetter(ProjectileLightningWorldEffect::data)
            ).apply(instance, ProjectileLightningWorldEffect::new)
    );

    @Override
    public void apply(Projectile projectile, int projectileLevel) {
        LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(projectile.level());
        lightningboltentity.moveTo(projectile.position());
        if(projectile.getOwner() instanceof ServerPlayer serverPlayer) {
            lightningboltentity.setCause(serverPlayer);
        }
        lightningboltentity.setData(DSDataAttachments.LIGHTNING_BOLT, LightningHandler.fromData(data));
        projectile.level().addFreshEntity(lightningboltentity);
    }

    @Override
    public MapCodec<? extends ProjectileWorldEffect> worldCodec() {
        return CODEC;
    }
}
