package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;


public final class SpinFlightPresentation {
    private static final float ACTIVE_PROGRESS_LERP = 0.68F;
    private static final float IDLE_PROGRESS_LERP = 0.20F;
    private static final float ENTRY_PHASE_END = 0.30F;
    private static final float EXIT_PHASE_START = 0.78F;
    private static final float ENTRY_PUNCH_END = 0.10F;
    private static final float FOV_RAMP_START = 0.08F;
    private static final float FOV_RAMP_END = 0.34F;
    private static final float CRUISE_RAMP_START = 0.12F;
    private static final float CRUISE_RAMP_END = 0.45F;
    private static final float INACTIVE_EPSILON = 0.0001F;

    private static final float ENTRY_ZOOM_MULTIPLIER = 1.45F;
    private static final float MIN_ZOOM = 0.55F;
    private static final float MAX_ZOOM = 1.45F;
    private static final float MIN_FOV_MULTIPLIER = 0.75F;
    private static final float MAX_FOV_MULTIPLIER = 1.35F;

    private static final float DETACHED_CAMERA_OFFSET = 1.15F;
    private static final float CAMERA_LIFT = 0.14F;
    private static final float EXIT_CAMERA_SETTLE = 0.03F;
    private static final float ENTRY_PITCH = 2.35F;
    private static final float SUSTAIN_PITCH = 0.28F;
    private static final float EXIT_REBOUND_PITCH = 0.45F;
    private static final float EXIT_ZOOM_REBOUND = 0.08F;
    private static final float EXIT_FOV_SETTLE = 0.06F;
    private static final float ROLL_SWAY = 0.32F;
    private static final float ZOOM_LERP_MIN = 0.28F;
    private static final float ZOOM_LERP_MAX = 0.74F;

    private static final MotionProfile PITCH_MOTION = new MotionProfile(1.65F, 0.12F, 0.85F, 0.04F, 0.35F);
    private static final MotionProfile ROLL_MOTION = new MotionProfile(1.95F, 0.18F, 1.10F, 0.05F, 1.20F);
    private static final MotionProfile BOB_MOTION = new MotionProfile(2.25F, 0.014F, 1.45F, 0.006F, 2.40F);

    private static final float ROLL_SWAY_FREQUENCY = 0.75F;
    private static final float ROLL_SWAY_PHASE = 0.60F;
    private static final float BOB_FREQUENCY = 1.90F;
    private static final float BOB_PHASE = 1.10F;
    private static final float BOB_AMPLITUDE = 0.018F;

    private static float smoothedProgress;

    private SpinFlightPresentation() {}

    public static void tick(@Nullable final Player player) {
        if (player == null) {
            reset();
            return;
        }

        boolean spinActive = ServerFlightHandler.isSpin(player);
        float targetProgress = getRawProgress(player);
        smoothedProgress = Mth.lerp(spinActive ? ACTIVE_PROGRESS_LERP : IDLE_PROGRESS_LERP, smoothedProgress, targetProgress);

        if (!spinActive && isInactive()) {
            reset();
        }
    }

    public static float getZoomMultiplier(final float entryStrength, final float travelStrength) {
        if (isInactive()) {
            return 1.0F;
        }

        PhaseSample phase = getPhaseSample();
        float entryPunch = 1.0F - easeOutCubic(getPhaseProgress(0.0F, ENTRY_PUNCH_END));
        float zoom = 1.0F
                + entryPunch * entryStrength * ENTRY_ZOOM_MULTIPLIER
                - phase.travel() * travelStrength
                + phase.exit() * travelStrength * EXIT_ZOOM_REBOUND;
        return Mth.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
    }

    public static float getFovMultiplier(final float strength) {
        if (isInactive()) {
            return 1.0F;
        }

        float exit = getExitStrength();
        float travel = getPhaseProgress(FOV_RAMP_START, FOV_RAMP_END) * (1.0F - exit);
        float settle = exit * EXIT_FOV_SETTLE;
        return Mth.clamp(1.0F + travel * strength - settle * strength, MIN_FOV_MULTIPLIER, MAX_FOV_MULTIPLIER);
    }

    public static float getDetachedCameraOffset() {
        return isInactive() ? 0.0F : getTravelStrength() * DETACHED_CAMERA_OFFSET;
    }

    public static float getCameraLift() {
        if (isInactive()) {
            return 0.0F;
        }

        float exit = getExitStrength();
        return getTravelStrength() * CAMERA_LIFT - exit * EXIT_CAMERA_SETTLE;
    }

    public static float getPitchOffset(final Player player, final float partialTick) {
        if (isInactive()) {
            return 0.0F;
        }

        PhaseSample phase = getPhaseSample();
        float pitch = -phase.entry() * ENTRY_PITCH - phase.travel() * SUSTAIN_PITCH + phase.exit() * EXIT_REBOUND_PITCH;
        return pitch + sampleMotion(getAnimationTime(player, partialTick), PITCH_MOTION) * phase.travel();
    }

    public static float getRollOffset(final Player player, final float partialTick) {
        if (isInactive()) {
            return 0.0F;
        }

        PhaseSample phase = getPhaseSample();
        float time = getAnimationTime(player, partialTick);
        float sway = Mth.sin(time * ROLL_SWAY_FREQUENCY + ROLL_SWAY_PHASE) * ROLL_SWAY * phase.cruise();
        float shake = sampleMotion(time, ROLL_MOTION) * phase.travel();
        return sway + shake;
    }

    public static float getCameraBobOffset(final Player player, final float partialTick) {
        if (isInactive()) {
            return 0.0F;
        }

        PhaseSample phase = getPhaseSample();
        float time = getAnimationTime(player, partialTick);
        float bob = Mth.sin(time * BOB_FREQUENCY + BOB_PHASE) * BOB_AMPLITUDE * phase.cruise();
        float shake = sampleMotion(time, BOB_MOTION) * phase.travel();
        return bob + shake;
    }

    public static float getZoomLerpFactor() {
        if (isInactive()) {
            return ZOOM_LERP_MIN;
        }

        return Mth.lerp(getTravelStrength(), ZOOM_LERP_MIN, ZOOM_LERP_MAX);
    }

    private static float getRawProgress(final Player player) {
        if (!ServerFlightHandler.isSpin(player)) {
            return 0.0F;
        }

        FlightData data = FlightData.getData(player);
        int totalDuration = Math.max(ServerFlightHandler.SPIN_DURATION, 1);
        return Mth.clamp(1.0F - data.duration / (float) totalDuration, 0.0F, 1.0F);
    }

    private static PhaseSample getPhaseSample() {
        float entry = getEntryStrength();
        float exit = getExitStrength();
        float travel = entry * (1.0F - exit);
        float cruise = travel * getPhaseProgress(CRUISE_RAMP_START, CRUISE_RAMP_END);
        return new PhaseSample(entry, travel, cruise, exit);
    }

    private static float getEntryStrength() {
        return easeInCubic(getPhaseProgress(0.0F, ENTRY_PHASE_END));
    }

    private static float getExitStrength() {
        return easeOutCubic(getPhaseProgress(EXIT_PHASE_START, 1.0F));
    }

    private static float getTravelStrength() {
        return getEntryStrength() * (1.0F - getExitStrength());
    }

    private static float getPhaseProgress(final float start, final float end) {
        return Mth.clamp(Mth.inverseLerp(smoothedProgress, start, end), 0.0F, 1.0F);
    }

    private static float getAnimationTime(final Player player, final float partialTick) {
        return player.tickCount + partialTick;
    }

    private static float sampleMotion(final float time, final MotionProfile profile) {
        float sine = Mth.sin(time * profile.sineFrequency() + profile.phase()) * profile.sineAmplitude();
        float noise = noise(time * profile.noiseFrequency() + profile.phase() * 1.7F) * profile.noiseAmplitude();
        return sine + noise;
    }

    private static boolean isInactive() {
        return smoothedProgress <= INACTIVE_EPSILON;
    }

    private static float noise(final float value) {
        double raw = Math.sin(value * 12.9898D + 78.233D) * 43758.5453D;
        return (float) ((raw - Math.floor(raw)) * 2.0D - 1.0D);
    }

    private static float easeInCubic(final float value) {
        return value * value * value;
    }

    private static float easeOutCubic(final float value) {
        float inverse = 1.0F - value;
        return 1.0F - inverse * inverse * inverse;
    }

    private static void reset() {
        smoothedProgress = 0.0F;
    }

    private record MotionProfile(float sineFrequency, float sineAmplitude, float noiseFrequency, float noiseAmplitude, float phase) {}

    private record PhaseSample(float entry, float travel, float cruise, float exit) {}
}
