package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class SummonData implements INBTSerializable<CompoundTag> {
    public static final Codec<SummonData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("owner_uuid").forGetter(data -> Optional.ofNullable(data.ownerUUID)),
            SummonedEntities.AttackBehaviour.CODEC.optionalFieldOf("attack_behaviour", SummonedEntities.AttackBehaviour.DEFENSIVE).forGetter(data -> data.attackBehaviour),
            SummonedEntities.MovementBehaviour.CODEC.optionalFieldOf("movement_behaviour", SummonedEntities.MovementBehaviour.FOLLOW).forGetter(data -> data.movementBehaviour)
    ).apply(instance, SummonData::new));

    private static final String DATA = "data";

    public SummonedEntities.AttackBehaviour attackBehaviour;
    public SummonedEntities.MovementBehaviour movementBehaviour;

    private UUID ownerUUID;
    private LivingEntity summonOwner;

    public SummonData() {
        // For the data attachment entry-point
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public SummonData(final Optional<UUID> ownerUUID, final SummonedEntities.AttackBehaviour attackBehaviour, final SummonedEntities.MovementBehaviour movementBehaviour) {
        this.ownerUUID = ownerUUID.orElse(null);
        this.attackBehaviour = attackBehaviour;
        this.movementBehaviour = movementBehaviour;
    }

    public @Nullable LivingEntity getOwner(final Level level) {
        if (ownerUUID == null) {
            return null;
        }

        if (summonOwner != null && summonOwner.isAlive()) {
            return summonOwner;
        }

        if (level instanceof ServerLevel serverLevel && serverLevel.getEntity(ownerUUID) instanceof LivingEntity livingEntity) {
            summonOwner = livingEntity;
        } else if (level.getPlayerByUUID(ownerUUID) instanceof LivingEntity livingEntity) {
            summonOwner = livingEntity;
        } else {
            summonOwner = null;
        }

        return summonOwner;
    }

    public void setOwnerUUID(@Nullable final LivingEntity owner) {
        summonOwner = owner;

        if (owner == null) {
            ownerUUID = null;
        } else {
            ownerUUID = owner.getUUID();
        }
    }

    public boolean isOwner(final Entity entity) {
        return ownerUUID != null && ownerUUID.equals(entity.getUUID());
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial(DragonSurvival.LOGGER::error)
                .ifPresent(compound -> tag.put(DATA, compound));

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        CODEC.decode(NbtOps.INSTANCE, tag.getCompound(DATA)).resultOrPartial(DragonSurvival.LOGGER::error).ifPresent(data -> {
            ownerUUID = data.getFirst().ownerUUID;
            attackBehaviour = data.getFirst().attackBehaviour;
            movementBehaviour = data.getFirst().movementBehaviour;
        });
    }
}
