package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class MovementData {
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

    public Vec2 desiredMoveVec = Vec2.ZERO;

    public boolean isFirstPerson;
    public boolean isFreeLook;
    public boolean wasFreeLook;

    //TODO: Biting is not correctly synced,
    // since we are setting it inside of the clientside animation
    // (code after it is received from other players over the server)
    public boolean bite;
    public boolean dig;

    public boolean isMoving() {
        return desiredMoveVec.lengthSquared() > INPUT_EPSILON * INPUT_EPSILON;
    }

    public void setFreeLook(boolean isFreeLook) {
        this.wasFreeLook = this.isFreeLook;
        this.isFreeLook = isFreeLook;
    }

    public void setFirstPerson(boolean isFirstPerson) {
        this.isFirstPerson = isFirstPerson;
    }

    public void setBite(boolean bite) {
        this.bite = bite;
    }

    public void setDesiredMoveVec(Vec2 desiredMoveVec) {
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
}