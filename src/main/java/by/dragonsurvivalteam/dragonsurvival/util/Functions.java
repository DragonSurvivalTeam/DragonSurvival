package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import com.mojang.math.Vector3f;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class Functions{
	public static int minutesToTicks(int minutes){
		return secondsToTicks(minutes) * 60;
	}

	public static int secondsToTicks(double seconds) {
		return (int) (seconds * 20);
	}
	
	public static int secondsToTicks(int seconds){
		return seconds * 20;
	}

	public static double ticksToMinutes(int ticks){
		return ticksToSeconds(ticks) / 60;
	}

	public static double ticksToSeconds(int ticks){
		return ticks / 20;
	}

	public static float angleDifference(float angle1, float angle2){
		float phi = Math.abs(angle1 - angle2) % 360;
		float dif = phi > 180 ? 360 - phi : phi;
		int sign = angle1 - angle2 >= 0 && angle1 - angle2 <= 180 || angle1 - angle2 <= -180 && angle1 - angle2 >= -360 ? 1 : -1;
		dif *= sign;
		return dif;
	}
	
	
	public static ListTag newDoubleList(double... pNumbers) {
		ListTag listtag = new ListTag();

		for(double d0 : pNumbers) {
			listtag.add(DoubleTag.valueOf(d0));
		}

		return listtag;
	}

	/**
	 * Returns a signed angle delta between a and b within the range [-180..180), returning the shorter distance.
	 * <br/>
	 * a + return value = b
	 *
	 * @param a First angle
	 * @param b Second angle
	 * @return Delta between angles
	 */
	public static double angleDifference(double a, double b) {
		return Mth.wrapDegrees(b - a);
	}

	/**
	 * Clamps value (as degrees) to be within +-halfRange of center.
	 * <br/>
	 * Returns a wrapped value in the range -180..180, snapping towards the closer of the bounds.
	 * Prefers snapping towards the positive direction (CW for Minecraft yaw).
	 *
	 * @param value     Input angle
	 * @param center    Center angle of the range arc
	 * @param halfRange Half of the range arc. <= 0 always returns center, >= 180 always returns value (wrapped).
	 * @return Value, limited to be within +-halfRange of center.
	 */
	public static double limitAngleDelta(double value, double center, double halfRange) {
		if (halfRange <= 0) return Mth.wrapDegrees(center);
		if (halfRange >= 180) return Mth.wrapDegrees(value);

		var delta = angleDifference(center, value);
		delta = Mth.clamp(delta, -halfRange, halfRange);

		return center + delta;
	}

	/** From 1.21 GeckoLib */
	public static double lerpYaw(double delta, double start, double end) {
		start = Mth.wrapDegrees(start);
		end = Mth.wrapDegrees(end);
		double diff = start - end;
		end = diff > 180 || diff < -180 ? start + Math.copySign(360 - Math.abs(diff), diff) : end;

		return Mth.lerp(delta, start, end);
	}


	/**
	 * Instead of strictly limiting the angle, this enforces a soft spring-like limit.
	 *
	 * @param value     Input angle
	 * @param center    Center angle of the range arc
	 * @param halfRange Half of the range arc. <= 0 always returns center, >= 180 always returns value (wrapped).
	 * @param pullCoeff Pull coefficient. Clamped to 0..1 (no limit..hard limit)
	 * @return Value, limited to be within +-halfRange of center.
	 * @see Functions#limitAngleDelta(double, double, double)
	 */
	public static double limitAngleDeltaSoft(double value, double center, double halfRange, double pullCoeff) {
		pullCoeff = Mth.clamp(pullCoeff, 0, 1);
		var targetAngle = limitAngleDelta(value, center, halfRange);
		return lerpYaw(pullCoeff, value, targetAngle);
	}

	/**
	 * Lerps from start to end, but making sure to avoid a particular angle, potentially taking a longer path.
	 *
	 * @param t          Lerp factor
	 * @param start      Start angle
	 * @param end        End angle
	 * @param avoidAngle Angle to be avoided - the lerp will pass through the other arc.
	 * @return Linearly interpolated angle
	 */
	public static double lerpAngleAwayFrom(double t, double start, double end, double avoidAngle) {
		if (Math.abs(Mth.wrapDegrees(avoidAngle - end)) < 0.0001) {
			// You're trying to go to the same angle that you're trying to avoid - too bad!
			return lerpYaw(t, start, end);
		}

		start = Mth.wrapDegrees(start);
		end = Mth.wrapDegrees(end);
		double diff = Mth.wrapDegrees(end - start);
		double avoidDiff = Mth.wrapDegrees(avoidAngle - start);
		var flipDir = Math.signum(diff) == Math.signum(avoidDiff) && Math.abs(diff) > Math.abs(avoidDiff);

		if (flipDir) {
			diff = Math.copySign(360 - Math.abs(diff), -diff);
		}

		return Mth.wrapDegrees(start + diff * t);
	}

	/**
	 * Inverse of lerp - the `t` from lerp(t, start, end). Not clamped.
	 * @param value Input value
	 * @param start Range start
	 * @param end Range end
	 * @return Normalized position of value between start and end, not clamped (extrapolated). 0 at start, 1 at end.
	 * Divides by zero when start == end - will return an infinity or NaN.
	 */
	public static double inverseLerp(double value, double start, double end) {
		return (value - start) / (end - start);
	}
	/**
	 * Inverse of lerp - the `t` from lerp(t, start, end). Clamped to 0..1.
	 * @param value Input value
	 * @param start Range start
	 * @param end Range end
	 * @return Normalized position of value between start and end, clamped to 0..1.
	 * Divides by zero when start == end - will return an infinity or NaN.
	 */
	public static double inverseLerpClamped(double value, double start, double end) {
		return Mth.clamp(inverseLerp(value, start, end), 0, 1);
	}

	/**
	 * Inverse of lerp - the `t` from lerp(t, start, end). Clamped to 0..1. Returns 0 if start == end.
	 * @param value Input value
	 * @param start Range start
	 * @param end Range end
	 * @return Normalized position of value between start and end, clamped to 0..1.
	 * Does NOT divide by zero when start == end, and falls back to 0.
	 */
	public static double inverseLerpClampedSafe(double value, double start, double end) {
		// We specifically care about the difference being 0, as that's relevant for the division here
		// Floats are weird, this might not be the same as start == end; hopefully this works
		return start - end == 0 ? 0 : inverseLerpClamped(value, start, end);
	}

	/**
	 * Adds a deadzone to value, normalizing it within ranges -maxRange..-deadzone and deadzone..maxRange.
	 * <br/>
	 * When value is between -deadzone..deadzone, the output is 0.
	 * When within the negative or positive range from deadzone to maxRange, the output is an inverse lerp
	 * between -1..0 and 0..1 respectively.
	 * The result is clamped to -1..1
	 * @param value Input value
	 * @param deadzone Minimum in both directions - deadzone
	 * @param maxRange Maximum in both directions
	 * @return Clamped inverse lerp of value between -maxRange..maxRange, with 0 offset by deadzone.
	 */
	public static double deadzoneNormalized(double value, double deadzone, double maxRange) {
		return Math.copySign(inverseLerpClamped(Math.abs(value), deadzone, maxRange), value);
	}

	/**
	 * Returns a new NBTTagList filled with the specified floats
	 */
	public static ListTag newFloatList(float... pNumbers) {
		ListTag listtag = new ListTag();

		for(float f : pNumbers) {
			listtag.add(FloatTag.valueOf(f));
		}

		return listtag;
	}
	
	public static int wrap(int value, int min, int max){
		return value < min ? max : value > max ? min : value;
	}
	
	public static Vector3f getDragonCameraOffset(Entity entity){
		Vector3f lookVector = new Vector3f(0, 0, 0);

		if(entity instanceof Player player){
			DragonStateHandler handler = DragonUtils.getHandler(player);
			if(handler.isDragon()){
				float f1 = -(float)handler.getMovementData().bodyYaw * ((float)Math.PI / 180F);

				float f4 = Mth.sin(f1);
				float f5 = Mth.cos(f1);
				lookVector.set((float)(f4 * (handler.getSize() / 40)), 0, (float)(f5 * (handler.getSize() / 40)));
			}
		}

		return lookVector;
	}
}