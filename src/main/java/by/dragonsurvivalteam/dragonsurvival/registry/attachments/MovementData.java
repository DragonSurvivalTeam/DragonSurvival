package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.network.player.SyncPitchAndYaw;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class MovementData implements INBTSerializable<CompoundTag> {
    public static final String HEAD_YAW = "headYaw";
    public static final String HEAD_PITCH = "headPitch";
    public static final String BODY_YAW = "bodyYaw";

    /// Minimum magnitude for player input to consider the player to be moving
    /// This is used for deliberate movement, i.e. player input
    /// Forced movement (midair momentum etc.) relies on MOVE_DELTA_EPSILON for the world-space move delta vector
    public static final double INPUT_EPSILON = 0.0000001D;

    public double headYaw = 0;
    public double headPitch = 0;
    public double bodyYaw = 0;
    public Vec3 deltaMovement = Vec3.ZERO;

    public double headYawLastFrame = 0;
    public double headPitchLastFrame = 0;
    public double bodyYawLastFrame = 0;
    public Vec3 deltaMovementLastFrame = Vec3.ZERO;

    public float prevXRot = 0;
    public float prevZRot = 0;

    public Vec3 desiredMoveVec = Vec3.ZERO;

    public boolean isFirstPerson;
    /** Prevents the body from rotating when moving the camera */
    public boolean isFreeLook;

    public boolean bite;
    public boolean dig;

    public boolean isMoving() {
        return desiredMoveVec.lengthSqr() > INPUT_EPSILON * INPUT_EPSILON;
    }

    public boolean isMovingHorizontally() {
        return desiredMoveVec.x * desiredMoveVec.x + desiredMoveVec.z * desiredMoveVec.z > INPUT_EPSILON * INPUT_EPSILON;
    }

    public void setFreeLook(boolean isFreeLook) {
        this.isFreeLook = isFreeLook;
    }

    public void setFirstPerson(boolean isFirstPerson) {
        this.isFirstPerson = isFirstPerson;
    }

    public void setBite(boolean bite) {
        this.bite = bite;
    }

    public void setDig(boolean dig) {
        this.dig = dig;
    }

    public void setDesiredMoveVec(Vec3 desiredMoveVec) {
        this.desiredMoveVec = desiredMoveVec;
    }

    public void set(double bodyYaw, double headYaw, double headPitch, Vec3 deltaMovement) {
        headYawLastFrame = this.headYaw;
        bodyYawLastFrame = this.bodyYaw;
        headPitchLastFrame = this.headPitch;
        deltaMovementLastFrame = this.deltaMovement;

        this.bodyYaw = bodyYaw;
        this.headYaw = headYaw;
        this.headPitch = headPitch;
        this.deltaMovement = deltaMovement;
    }

    public static MovementData getData(final Entity entity) {
        return entity.getData(DSDataAttachments.MOVEMENT);
    }

    public void sync(final ServerPlayer player) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncPitchAndYaw(player.getId(), headYaw, headPitch, bodyYaw));
    }

    // Needed for when a player enters tracking range, as the movement data has a visual impact
    public void sync(final ServerPlayer source, final ServerPlayer target) {
        PacketDistributor.sendToPlayer(target, new SyncPitchAndYaw(source.getId(), headYaw, headPitch, bodyYaw));
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag movementData = new CompoundTag();
        movementData.putDouble(HEAD_YAW, headYaw);
        movementData.putDouble(BODY_YAW, bodyYaw);
        movementData.putDouble(HEAD_PITCH, headPitch);
        return movementData;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag nbt) {
        headYaw = nbt.getDouble(HEAD_YAW);
        bodyYaw = nbt.getDouble(BODY_YAW);
        headPitch = nbt.getDouble(HEAD_PITCH);
    }
}