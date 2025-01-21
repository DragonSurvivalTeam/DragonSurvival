package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.Tags;
import software.bernie.geckolib.util.RenderUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Functions {
    public static int daysToTicks(double days) {
        return hoursToTicks(days) * 24;
    }

    public static int hoursToTicks(double hours) {
        return minutesToTicks(hours) * 60;
    }

    public static int minutesToTicks(double minutes) {
        return secondsToTicks(minutes) * 60;
    }

    public static int secondsToTicks(double seconds) {
        return (int)(seconds * 20);
    }

    public static double ticksToHours(int ticks) {
        return ticksToMinutes(ticks) / 60d;
    }

    public static double ticksToMinutes(int ticks) {
        return ticksToSeconds(ticks) / 60d;
    }

    public static double ticksToSeconds(int ticks) {
        return ticks / 20d;
    }

    public record Time(int hours, int minutes, int seconds) {
        private static final NumberFormat FORMAT = NumberFormat.getInstance();

        static {
            FORMAT.setMinimumIntegerDigits(2);
        }

        public static Time fromTicks(int ticks) {
            int hours = (int) (Functions.ticksToHours(ticks));
            int minutes = (int) (Functions.ticksToMinutes(ticks - Functions.hoursToTicks(hours)));
            int seconds = (int) (Functions.ticksToSeconds(ticks - Functions.hoursToTicks(hours) - Functions.minutesToTicks(minutes)));
            return new Time(hours, minutes, seconds);
        }

        public boolean hasTime() {
            return hours != 0 || minutes != 0 || seconds != 0;
        }

        public String format() {
            return format(hours) + ":" + format(minutes) + ":" + format(seconds);
        }

        public String format(int number) {
            return FORMAT.format(Math.abs(number));
        }
    }

    /** See {@link Functions#chance(RandomSource, int)} */
    public static boolean chance(final Player player, int chance) {
        return chance(player.getRandom(), chance);
    }

    /** rolls between 1 and 100 (incl.) (chance of 0 will always return false) */
    public static boolean chance(final RandomSource random, int chance) {
        return 1 + random.nextInt(100) < chance;
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
     *
     * @param value     Input angle
     * @param center    Center angle of the range arc
     * @param halfRange Half of the range arc. <= 0 always returns center, >= 180 always returns value (wrapped).
     * @param pullCoeff Pull coefficient. Clamped to 0..1 (no limit..hard limit)
     * @return Value, limited to be within +-halfRange of center.
     * @see Functions#limitAngleDelta(double, double, double)
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
        return Math.clamp(inverseLerp(value, start, end), 0, 1);
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

    public static float getScale(final Player player, double size) {
        // The formula is generated based on input / output pairs of various sizes which looked correct
        // The three values can be used to fine-tune the dragon model with the hitbox across the different sizes
        // 1 = middle-sized dragons / 2 = large dragons / 3 = small dragons
        return (float) ((0.010 * Math.pow(size, 1.14) + 0.3) * player.getAttributeValue(Attributes.SCALE));
    }

    public static double getSunPosition(final Entity entity) {
        float sunAngle = entity.level().getSunAngle(1);
        float angleTarget = sunAngle < (float) Math.PI ? 0 : (float) Math.PI * 2f;
        sunAngle = sunAngle + (angleTarget - sunAngle) * 0.2f;
        // 1 means it's a time of 6000 (sun is at the highest point)
        return Mth.cos(sunAngle);
    }

    public static double calculateAttributeValue(final double base, final double level, final List<AttributeModifier> attributeModifiers, final List<Modifier> modifiers) {
        List<Double> addition = new ArrayList<>();
        List<Double> multiplyBase = new ArrayList<>();
        List<Double> multiplyTotal = new ArrayList<>();

        for (AttributeModifier modifier : attributeModifiers) {
            switch (modifier.operation()) {
                case ADD_VALUE -> addition.add(modifier.amount());
                case ADD_MULTIPLIED_BASE -> multiplyBase.add(modifier.amount());
                case ADD_MULTIPLIED_TOTAL -> multiplyTotal.add(modifier.amount());
            }
        }

        for (Modifier modifier : modifiers) {
            switch (modifier.operation()) {
                case ADD_VALUE -> addition.add(modifier.calculate(level));
                case ADD_MULTIPLIED_BASE -> multiplyBase.add(modifier.calculate(level));
                case ADD_MULTIPLIED_TOTAL -> multiplyTotal.add(modifier.calculate(level));
            }
        }

        double calculationBase = base;

        for (double amount: addition) {
            calculationBase += amount;
        }

        double result = calculationBase;

        for (double amount: multiplyBase) {
            result += calculationBase * amount;
        }

        for (double amount : multiplyTotal) {
            result *= 1 + amount;
        }

        return result;
    }

    public static <T> MutableComponent translateHolderSet(final HolderSet<T> set, final Translation.Type type) {
        //noinspection DataFlowIssue -> key is present
        return translateHolderSet(set, entry -> type.wrap(entry.getKey().location()));
    }

    public static <T> MutableComponent translateHolderSet(final HolderSet<T> set, final Function<Holder<T>, String> translationKey) {
        if (set instanceof HolderSet.Named<T> named) {
            return DSColors.dynamicValue(Component.translatable(Tags.getTagTranslationKey(named.key())));
        }

        MutableComponent list = null;

        for (Holder<T> entry : set) {
            MutableComponent name = DSColors.dynamicValue(Component.translatable(translationKey.apply(entry)));

            if (list == null) {
                list = name;
            } else {
                list.append(Component.literal(", ").withStyle(ChatFormatting.GRAY)).append(name);
            }
        }

        return Objects.requireNonNullElse(list, Component.empty());
    }

    public static int lerpColor(final List<Integer> colors) {
        if (colors.isEmpty()) {
            return DSColors.NONE;
        }

        if (colors.size() == 1) {
            return colors.getFirst();
        }

        float sizeIndex = DragonSurvival.PROXY.getTimer() * colors.size();
        int currentIndex = (int) (Math.floor(sizeIndex) % colors.size());
        int nextIndex = (currentIndex + 1) % colors.size();

        return FastColor.ARGB32.lerp(sizeIndex - currentIndex, DSColors.withAlpha(colors.get(currentIndex), 255), DSColors.withAlpha(colors.get(nextIndex), 255));
    }

    /** Makes sure to return an enum value (instead of an exception) */
    public static <T extends Enum<T>> T getEnum(final Class<T> type, final String name) {
        try {
            return Enum.valueOf(type, name);
        } catch (NullPointerException | IllegalArgumentException ignored) {
            return type.getEnumConstants()[0];
        }
    }

    public static <T extends Enum<T>> T cycleEnum(final T type) {
        int ordinal = type.ordinal();

        Class<T> declaringClass = type.getDeclaringClass();
        T[] values = declaringClass.getEnumConstants();

        if (ordinal == values.length - 1) {
            ordinal = 0;
        } else {
            ordinal++;
        }

        return values[ordinal];
    }

    public static void logOrThrow(final String message) {
        if (FMLLoader.isProduction()) {
            DragonSurvival.LOGGER.error(message);
        } else {
            throw new IllegalStateException(message);
        }
    }
}