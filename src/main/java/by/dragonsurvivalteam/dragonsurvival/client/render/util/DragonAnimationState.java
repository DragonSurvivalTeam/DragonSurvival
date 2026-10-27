package by.dragonsurvivalteam.dragonsurvival.client.render.util;

import net.minecraft.util.Mth;

import java.util.Arrays;

/** Tick snapshots for procedural motion */
public final class DragonAnimationState {
    // We used to store the last 10 frames of data (with a 60 FPS baseline) for molang movement,
    // so 4 client ticks is close enough to that.
    private static final int HISTORY_TICKS = 4;

    /** Factor to multiply the vertical velocity by, needed for scaling for the animations */
    private static final double VERTICAL_VELOCITY_FACTOR = 10;

    /** Factor to multiply the tail motion by, needed for scaling for the animations */
    private static final double TURN_MOTION_SCALE = 0.2;

    // At 60 FPS and 20 ticks per second, each frame moved 1/3 of the way toward the average.
    // Retain the same amount of damping when updating once per game tick instead.
    private static final double REFERENCE_FRAMES_PER_TICK = 3;
    private static final double REFERENCE_FRAME_RETENTION = (REFERENCE_FRAMES_PER_TICK - 1) / REFERENCE_FRAMES_PER_TICK;
    private static final double MOTION_LERP_FACTOR = 1 - Math.pow(REFERENCE_FRAME_RETENTION, REFERENCE_FRAMES_PER_TICK);

    private final MotionHistory bodyYawChangeHistory = new MotionHistory();
    private final MotionHistory headYawChangeHistory = new MotionHistory();
    private final MotionHistory headPitchChangeHistory = new MotionHistory();
    private final MotionHistory tailMotionHistory = new MotionHistory();

    private Pose previous;
    private Pose current;

    public record Pose(double bodyYaw, double headYaw, double headPitch, double bodyYawChange,
                       double headYawChange, double headPitchChange, double tailMotionUp,
                       float flightPitch, float flightRoll) {}

    public void tick(double bodyYaw, double headYaw, double headPitch, double verticalVelocity,
                     float flightPitch, float flightRoll, boolean clearVerticalVelocity) {
        // Clear out any NaNs (I think this happens if we try to load data before the game logic has actually begun?)
        bodyYaw = finite(bodyYaw);
        headYaw = finite(headYaw);
        headPitch = finite(headPitch);
        flightPitch = (float) finite(flightPitch);
        flightRoll = (float) finite(flightRoll);

        double scaledVerticalVelocity = verticalVelocity * VERTICAL_VELOCITY_FACTOR;
        // Factor in the vertical angle of the dragon so that the vertical velocity is scaled down when the dragon is looking up or down
        // Ideally, we would just use more precise data (factor in the full rotation of the player in our animations)
        // but this works pretty well in most situations the player will encounter
        scaledVerticalVelocity *= 1 - Mth.abs(Mth.clampedMap(flightPitch, -90, 90, -1, 1));

        if (current == null) {
            current = new Pose(bodyYaw, headYaw, headPitch, 0, 0, 0, 0, flightPitch, flightRoll);
        }
        previous = current;

        bodyYawChangeHistory.add(angleDifference(previous.bodyYaw, bodyYaw) * TURN_MOTION_SCALE);
        headYawChangeHistory.add(angleDifference(previous.headYaw, headYaw) * TURN_MOTION_SCALE);
        headPitchChangeHistory.add(angleDifference(previous.headPitch, headPitch) * TURN_MOTION_SCALE);

        // Handle the clear case (see DragonEntity.java)
        if (clearVerticalVelocity) {
            tailMotionHistory.resetToZero();
        }

        tailMotionHistory.add(clearVerticalVelocity ? 0 : -finite(scaledVerticalVelocity));
        current = new Pose(bodyYaw, headYaw, headPitch,
                lerp(MOTION_LERP_FACTOR, previous.bodyYawChange, bodyYawChangeHistory.average()),
                lerp(MOTION_LERP_FACTOR, previous.headYawChange, headYawChangeHistory.average()),
                lerp(MOTION_LERP_FACTOR, previous.headPitchChange, headPitchChangeHistory.average()),
                clearVerticalVelocity ? 0 : lerp(MOTION_LERP_FACTOR, previous.tailMotionUp, tailMotionHistory.average()),
                flightPitch, flightRoll);

        if (clearVerticalVelocity) {
            previous = new Pose(previous.bodyYaw, previous.headYaw, previous.headPitch, previous.bodyYawChange,
                    previous.headYawChange, previous.headPitchChange, 0, previous.flightPitch, previous.flightRoll);
        }
    }

    public Pose sample(float partialTick) {

        if (current == null) {
            return null;
        }

        double alpha = Math.clamp(finite(partialTick), 0, 1);
        return new Pose(previous.bodyYaw + angleDifference(current.bodyYaw, previous.bodyYaw) * alpha,
                previous.headYaw + angleDifference(current.headYaw, previous.headYaw) * alpha,
                previous.headPitch + angleDifference(current.headPitch, previous.headPitch) * alpha,
                lerp(alpha, previous.bodyYawChange, current.bodyYawChange),
                lerp(alpha, previous.headYawChange, current.headYawChange),
                lerp(alpha, previous.headPitchChange, current.headPitchChange),
                lerp(alpha, previous.tailMotionUp, current.tailMotionUp),
                (float) lerp(alpha, previous.flightPitch, current.flightPitch),
                (float) lerp(alpha, previous.flightRoll, current.flightRoll));
    }

    private static double angleDifference(double target, double start) {
        double difference = (target - start) % 360;
        return difference >= 180 ? difference - 360 : difference < -180 ? difference + 360 : difference;
    }

    private static double lerp(double alpha, double start, double end) {
        return start + alpha * (end - start);
    }

    private static double finite(double value) {
        return Double.isFinite(value) ? value : 0;
    }

    /** A moving average */
    private static final class MotionHistory {
        private final double[] values = new double[HISTORY_TICKS];
        private int nextSampleIndex;
        private int sampleCount;

        public void add(double value) {
            // Wrap around and replace the oldest sample once the history is full.
            values[nextSampleIndex] = value;
            nextSampleIndex = (nextSampleIndex + 1) % values.length;
            sampleCount = Math.min(sampleCount + 1, values.length);
        }

        public double average() {
            double sum = 0;
            for (int i = 0; i < sampleCount; i++) {
                sum += values[i];
            }
            return sampleCount == 0 ? 0 : sum / sampleCount;
        }

        public void resetToZero() {
            Arrays.fill(values, 0);
        }
    }
}
