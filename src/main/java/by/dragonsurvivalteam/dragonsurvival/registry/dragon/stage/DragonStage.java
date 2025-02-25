package by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthItem;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.AttributeModifierSupplier;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonStage(
        boolean isDefault,
        MiscCodecs.Bounds growthRange,
        int ticksUntilGrown,
        List<Modifier> modifiers,
        List<GrowthItem> growthItems,
        Optional<EntityPredicate> isNaturalGrowthStopped,
        Optional<MiscCodecs.DestructionData> destructionData
) implements AttributeModifierSupplier {
    public static final ResourceKey<Registry<DragonStage>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_stage"));

    public static final Codec<DragonStage> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("is_default", false).forGetter(DragonStage::isDefault),
            MiscCodecs.bounds().fieldOf("growth_range").forGetter(DragonStage::growthRange),
            ExtraCodecs.intRange(1, Functions.daysToTicks(365)).fieldOf("ticks_until_grown").forGetter(DragonStage::ticksUntilGrown),
            Modifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(DragonStage::modifiers),
            GrowthItem.CODEC.listOf().optionalFieldOf("growth_items", List.of()).forGetter(DragonStage::growthItems),
            EntityPredicate.CODEC.optionalFieldOf("is_natural_growth_stopped").forGetter(DragonStage::isNaturalGrowthStopped),
            MiscCodecs.DestructionData.CODEC.optionalFieldOf("destruction_data").forGetter(DragonStage::destructionData)
    ).apply(instance, instance.stable(DragonStage::new)));

    public static final Codec<Holder<DragonStage>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonStage>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    private static @Nullable HolderSet<DragonStage> defaultStages;

    private static double minGrowth;
    private static double maxGrowth;

    public static void update(final RegistryAccess access) {
        Pair<Double, Double> sizes = calculateGrowthBounds(access);
        minGrowth = sizes.getFirst();
        maxGrowth = sizes.getSecond();
        defaultStages = null;

        validate(access);
    }

    private static void validate(final RegistryAccess access) {
        boolean areBuiltInLevelsValid = true;
        StringBuilder builtInCheck = new StringBuilder("The following required built-in dragon levels are missing:");

        //noinspection ConstantValue -> ignore for clarity
        areBuiltInLevelsValid = areBuiltInLevelsValid && isValid(builtInCheck, access, DragonStages.newborn);
        areBuiltInLevelsValid = areBuiltInLevelsValid && isValid(builtInCheck, access, DragonStages.young);
        areBuiltInLevelsValid = areBuiltInLevelsValid && isValid(builtInCheck, access, DragonStages.adult);

        if (!areBuiltInLevelsValid) {
            throw new IllegalStateException(builtInCheck.toString());
        }

        StringBuilder validationError = new StringBuilder("The following stages are incorrectly defined:");
        AtomicBoolean areStagesValid = new AtomicBoolean(true);

        ResourceHelper.keys(access, REGISTRY).forEach(key -> {
            //noinspection OptionalGetWithoutIsPresent -> ignore
            Holder.Reference<DragonStage> stage = ResourceHelper.get(access, key).get();

            // Validate that the block destruction growth and the crushing growth are within the bounds of the current dragon stage
            if (stage.value().destructionData().isPresent()) {
                MiscCodecs.DestructionData destructionData = stage.value().destructionData().get();

                if (destructionData.blockDestructionGrowth() > stage.value().growthRange().max() || destructionData.blockDestructionGrowth() < stage.value().growthRange().min()) {
                    validationError.append("\n- Block destruction growth of [").append(key.location()).append("] is not within the bounds of the dragon stage");
                    areStagesValid.set(false);
                }

                if (destructionData.crushingGrowth() > stage.value().growthRange().max() || destructionData.crushingGrowth() < stage.value().growthRange().min()) {
                    validationError.append("\n- Crushing growth of [").append(key.location()).append("] is not within the bounds of the dragon stage");
                    areStagesValid.set(false);
                }
            }
        });

        // Validate that the default stages have a connected growth range
        HolderSet<DragonStage> defaultStages = getDefaultStages(access);
        // Sort the default stages by growth range
        HolderSet<DragonStage> sortedDefaultStages = HolderSet.direct(defaultStages.stream().sorted(Comparator.comparingDouble(stage -> stage.value().growthRange().min())).toList());

        if (!areStagesConnected(sortedDefaultStages, validationError, true)) {
            areStagesValid.set(false);
        }

        if (!areStagesValid.get()) {
            throw new IllegalStateException(validationError.toString());
        }
    }

    /** Checks if the max. growth of each stage lines up with the min. growth of the next stage */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore for clarity
    public static boolean areStagesConnected(final HolderSet<DragonStage> sortedStages, final StringBuilder error, boolean isForDefaultStages) {
        for (int i = 0; i < sortedStages.size() - 1; i++) {
            if (sortedStages.get(i).value().growthRange().max() != sortedStages.get(i + 1).value().growthRange().min()) {
                error.append(isForDefaultStages ? "\n- Default stages [" : "\n- Stages [").append(sortedStages.get(i).getRegisteredName()).append("] and [").append(sortedStages.get(i + 1).getRegisteredName()).append("] are not connected");
                return false;
            }
        }

        return true;
    }

    private static boolean isValid(final StringBuilder builder, @Nullable final HolderLookup.Provider provider, final ResourceKey<DragonStage> stageKey) {
        Optional<Holder.Reference<DragonStage>> optional = ResourceHelper.get(provider, stageKey);

        if (optional.isPresent()) {
            return true;
        } else {
            builder.append("\n- ").append(stageKey.location());
            return false;
        }
    }

    public double ticksToGrowth(int ticks) {
        return (growthRange().max() - growthRange().min()) / ticksUntilGrown() * ticks;
    }

    public double getProgress(double growth) {
        return ((growth - growthRange().min())) / (growthRange().max() - growthRange().min());
    }

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

    public static Component translatableName(final ResourceKey<DragonStage> dragonStage) {
        return Component.translatable(Translation.Type.STAGE.wrap(dragonStage.location()));
    }

    public double getBoundedGrowth(double growth) {
        return Math.clamp(growth, growthRange().min(), growthRange().max());
    }

    /** Returns the bounds between the smallest and largest dragon growth */
    public static MiscCodecs.Bounds getBounds() {
        return new MiscCodecs.Bounds(minGrowth, maxGrowth);
    }

    public static double getStartingGrowth(final HolderSet<DragonStage> stages) {
        return stages.stream().min(Comparator.comparingDouble(stage -> stage.value().growthRange().min()))
                .map(stage -> stage.value().growthRange().min()).orElse(DragonStateHandler.NO_GROWTH);
    }

    /**
     * Returns a stage from the provided set whose growth range matches the provided growth <br>
     * It is not a match if the growth equals to the max. growth of the stage <br>
     * (This is because if we provide such a growth we usually want the next stage, not the current stage at max. growth)
     */
    public static Holder<DragonStage> get(final HolderSet<DragonStage> stages, double growth) {
        Holder<DragonStage> smallest = null;
        Holder<DragonStage> largest = null;

        for (Holder<DragonStage> stage : stages) {
            if (stage.value().growthRange().matches(growth) && stage.value().growthRange().max() != growth) {
                return stage;
            }

            if (smallest == null || stage.value().growthRange().min() < smallest.value().growthRange().min()) {
                smallest = stage;
            }

            if (largest == null || stage.value().growthRange().min() > largest.value().growthRange().min()) {
                largest = stage;
            }
        }

        //noinspection DataFlowIssue -> stage should not be null at this point
        if (growth <= smallest.value().growthRange().min()) {
            return smallest;
        }

        return largest;
    }

    /**
     * TODO :: is this even usable? it could select a stage that is not valid for the current species
     * Tries to retrieve a valid stage for the provided size <br>
     * If no size range matches either the smallest or largest stage will be returned <br>
     * (Depending on whose min. size is closer to the provided size)
     */
    public static Holder<DragonStage> get(@Nullable final HolderLookup.Provider provider, double growth) {
        double fallbackDifference = Double.MAX_VALUE;
        Holder<DragonStage> fallback = null;

        for (Holder.Reference<DragonStage> level : ResourceHelper.all(provider, REGISTRY)) {
            if (level.value().growthRange().matches(growth)) {
                return level;
            }

            double difference = Math.abs(level.value().growthRange().min() - growth);

            if (fallback == null || difference < fallbackDifference) {
                fallbackDifference = difference;
                fallback = level;
            }
        }

        if (fallback != null) {
            DragonSurvival.LOGGER.warn("No matching dragon level found for growth [{}] - using [{}] as fallback", growth, fallback.getRegisteredName());
            return fallback;
        }

        throw new IllegalStateException("There is no valid dragon level for the supplied growth [" + growth + "]");
    }

    private static Pair<Double, Double> calculateGrowthBounds(@Nullable final HolderLookup.Provider provider) {
        DragonStage smallest = null;
        DragonStage largest = null;

        for (Holder.Reference<DragonStage> level : ResourceHelper.all(provider, REGISTRY)) {
            if (smallest == null || level.value().growthRange().min() < smallest.growthRange().min()) {
                smallest = level.value();
            }

            if (largest == null || level.value().growthRange().max() > largest.growthRange().max()) {
                largest = level.value();
            }
        }

        //noinspection DataFlowIssue -> stages should not be null
        return Pair.of(smallest.growthRange.min(), largest.growthRange.max());
    }

    public static HolderSet<DragonStage> getDefaultStages(@Nullable final HolderLookup.Provider provider) {
        if (defaultStages == null) {
            defaultStages = HolderSet.direct(ResourceHelper.all(provider, REGISTRY).stream().filter(stage -> stage.value().isDefault()).toList());
        }

        return defaultStages;
    }

    public String getTimeToGrowFormattedWithPercentage(double percentage, double size, boolean isGrowing) {
        String ageInformation = NumberFormat.getPercentInstance().format(percentage);

        if (!isGrowing) {
            return ageInformation + " (§4--:--:--§r)";
        }

        double sizeToTicks = (growthRange().max() - growthRange().min()) / ticksUntilGrown();
        double missingSize = growthRange().max() - size;
        Functions.Time time = Functions.Time.fromTicks((int) (missingSize / sizeToTicks));

        if (time.hasTime()) {
            ageInformation += " (" + time.format() + ")";
        }

        return ageInformation;
    }

    public String getTimeToGrowFormatted(boolean growthStopped) {
        if (growthStopped) {
            return "§4--:--:--§r";
        }

        Functions.Time time = Functions.Time.fromTicks(ticksUntilGrown());
        return time.format();
    }

    @Override
    public ModifierType getModifierType() {
        return ModifierType.DRAGON_STAGE;
    }
}
