package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class SummonData implements ValueIOSerializable  {
    public static final Codec<SummonData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf("owner_uuid").forGetter(data -> Optional.ofNullable(data.ownerUUID)),
            Codec.BOOL.optionalFieldOf("is_allied", false).forGetter(data -> data.isAllied),
            SummonedEntities.AttackBehaviour.CODEC.optionalFieldOf("attack_behaviour", SummonedEntities.AttackBehaviour.DEFENSIVE).forGetter(data -> data.attackBehaviour),
            SummonedEntities.MovementBehaviour.CODEC.optionalFieldOf("movement_behaviour", SummonedEntities.MovementBehaviour.FOLLOW).forGetter(data -> data.movementBehaviour)
    ).apply(instance, SummonData::new));

    private static final String DATA = "data";

    public boolean isAllied;
    public SummonedEntities.AttackBehaviour attackBehaviour;
    public SummonedEntities.MovementBehaviour movementBehaviour;

    private UUID ownerUUID;
    private LivingEntity summonOwner;

    public SummonData() {
        // For the data attachment entry-point
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public SummonData(final Optional<UUID> ownerUUID, final boolean isAllied, final SummonedEntities.AttackBehaviour attackBehaviour, final SummonedEntities.MovementBehaviour movementBehaviour) {
        this.ownerUUID = ownerUUID.orElse(null);
        this.isAllied = isAllied;
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
    public void serialize(@NotNull final ValueOutput valueOutput) {
        valueOutput.store(DATA, CODEC, this);
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        SummonData data = valueInput.read(DATA, CODEC).orElseThrow();
        ownerUUID = data.ownerUUID;
        attackBehaviour = data.attackBehaviour;
        movementBehaviour = data.movementBehaviour;
    }
}
