package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;
import software.bernie.geckolib.util.RenderUtil;

public class Functions {
    public static int minutesToTicks(int minutes) {
        return secondsToTicks(minutes) * 60;
    }

    public static int secondsToTicks(double seconds) {
        return (int) (seconds * 20);
    }

    public static int secondsToTicks(int seconds) {
        return seconds * 20;
    }

    public static double ticksToMinutes(int ticks) {
        return ticksToSeconds(ticks) / 60;
    }

    public static double ticksToSeconds(int ticks) {
        return ticks / 20d;
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
    public static float angleDifference(float a, float b) {
        return Mth.wrapDegrees(b - a);
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
        delta = Math.clamp(delta, -halfRange, halfRange);

        return center + delta;
    }


    /**
     * Instead of strictly limiting the angle, this enforces a soft spring-like limit.
     * @see Functions#limitAngleDelta(double, double, double)
     * @param value     Input angle
     * @param center    Center angle of the range arc
     * @param halfRange Half of the range arc. <= 0 always returns center, >= 180 always returns value (wrapped).
     * @param pullCoeff Pull coefficient. Clamped to 0..1 (no limit..hard limit)
     * @return Value, limited to be within +-halfRange of center.
     */
    public static double limitAngleDeltaSoft(double value, double center, double halfRange, double pullCoeff) {
        pullCoeff = Math.clamp(pullCoeff, 0, 1);
        var targetAngle = limitAngleDelta(value, center, halfRange);
        return RenderUtil.lerpYaw(pullCoeff, value, targetAngle);
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
            return RenderUtil.lerpYaw(t, start, end);
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


    public static ListTag newDoubleList(double... pNumbers) {
        ListTag listtag = new ListTag();

        for (double d0 : pNumbers) {
            listtag.add(DoubleTag.valueOf(d0));
        }

        return listtag;
    }

    /**
     * Returns a new NBTTagList filled with the specified floats
     */
    public static ListTag newFloatList(float... pNumbers) {
        ListTag listtag = new ListTag();

        for (float f : pNumbers) {
            listtag.add(FloatTag.valueOf(f));
        }

        return listtag;
    }

    public static int wrap(int value, int min, int max) {
        return value < min ? max : value > max ? min : value;
    }

    public static Vector3f getDragonCameraOffset(Entity entity) {
        Vector3f lookVector = new Vector3f(0, 0, 0);

        if (entity instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);
            if (handler.isDragon()) {
                float f1 = -(float) handler.getMovementData().bodyYaw * ((float) Math.PI / 180F);

                float f4 = Mth.sin(f1);
                float f5 = Mth.cos(f1);
                AttributeInstance attributeInstance = player.getAttribute(Attributes.SCALE);
                double scale = attributeInstance != null ? attributeInstance.getValue() : 1.0d;
                lookVector.set((float) (f4 * (handler.getSize() * scale / 40)), 0, (float) (f5 * (handler.getSize() * scale / 40)));
            }
        }

        return lookVector;
    }
}