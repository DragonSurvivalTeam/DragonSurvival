package by.dragonsurvivalteam.dragonsurvival.common.capability.objects;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class DragonMovementData{
	// Animations are authored with 60 FPS as a reference, and 1 tick is 1/20th of a second
	public static final double REFERENCE_REALTIME_TICK = 1.0 / 3.0;
	public double realtimeDeltaTick = 0;

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

	public boolean isFirstPerson = false;
	public boolean isFreeLook = false;
	public boolean wasFreeLook = false;

	//TODO: Biting is not correctly synced, since we are setting it inside of the clientside animation code after it is received from other players over the server
	public boolean bite = false;

	public boolean dig = false;

	public boolean spinLearned;
	public int spinCooldown;
	public int spinAttack;

	public double getTickFactor() {
        return realtimeDeltaTick == 0 ? 1 : REFERENCE_REALTIME_TICK / realtimeDeltaTick;
	}

	public DragonMovementData(){}
}