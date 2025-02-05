package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record NearbyEntityPredicate(HolderSet<EntityType<?>> entityTypes, int radius) {
    public static final Codec<NearbyEntityPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entity_types").forGetter(NearbyEntityPredicate::entityTypes),
            Codec.INT.fieldOf("radius").forGetter(NearbyEntityPredicate::radius)
    ).apply(instance, NearbyEntityPredicate::new));

    public static NearbyEntityPredicate of(final int radius, final EntityType<?>... types) {
        List<Holder<EntityType<?>>> holders = new ArrayList<>();

        for (EntityType<?> type : types) {
            BuiltInRegistries.ENTITY_TYPE.getResourceKey(type).ifPresent(key -> {
                holders.add(BuiltInRegistries.ENTITY_TYPE.getHolderOrThrow(key));
            });
        }

        return new NearbyEntityPredicate(HolderSet.direct(holders), radius);
    }

    public boolean matches(final ServerLevel level, final Vec3 position) {
        for (Entity entity : level.getEntities(null, AABB.ofSize(position, radius * 2, radius * 2, radius * 2))) {
            if (entity.getType().is(entityTypes)) {
                return true;
            }
        }

        return false;
    }
}
