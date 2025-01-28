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
        MiscCodecs.Bounds sizeRange,
        int ticksUntilGrown,
        List<Modifier> modifiers,
        List<GrowthItem> growthItems,
        Optional<EntityPredicate> isNaturalGrowthStopped,
        Optional<EntityPredicate> growIntoRequirements,
        Optional<MiscCodecs.DestructionData> destructionData
) implements AttributeModifierSupplier {
    public static final ResourceKey<Registry<DragonStage>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_stages"));

    public static final Codec<DragonStage> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("is_default", false).forGetter(DragonStage::isDefault),
            MiscCodecs.bounds().fieldOf("size_range").forGetter(DragonStage::sizeRange),
            ExtraCodecs.intRange(Functions.secondsToTicks(1), Functions.daysToTicks(365)).fieldOf("ticks_until_grown").forGetter(DragonStage::ticksUntilGrown),
            Modifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(DragonStage::modifiers),
            GrowthItem.CODEC.listOf().optionalFieldOf("growth_items", List.of()).forGetter(DragonStage::growthItems),
            EntityPredicate.CODEC.optionalFieldOf("is_natural_growth_stopped").forGetter(DragonStage::isNaturalGrowthStopped),
            EntityPredicate.CODEC.optionalFieldOf("grow_into_requirements").forGetter(DragonStage::growIntoRequirements), // FIXME :: unused
            MiscCodecs.DestructionData.CODEC.optionalFieldOf("destruction_data").forGetter(DragonStage::destructionData)
    ).apply(instance, instance.stable(DragonStage::new)));

    public static final Codec<Holder<DragonStage>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonStage>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    private static @Nullable HolderSet<DragonStage> defaultStages;

    private static double minSize;
    private static double maxSize;

    public static void update(final RegistryAccess access) {
        Pair<Double, Double> sizes = getSizes(access);
        minSize = sizes.getFirst();
        maxSize = sizes.getSecond();
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

            // Validate that the block destruction size and the crushing size are within the bounds of the current dragon stage
            if (stage.value().destructionData().isPresent()) {
                MiscCodecs.DestructionData destructionData = stage.value().destructionData().get();

                if (destructionData.blockDestructionSize() > stage.value().sizeRange().max() || destructionData.blockDestructionSize() < stage.value().sizeRange().min()) {
                    validationError.append("\n- Block destruction size of [").append(key.location()).append("] is not within the bounds of the dragon stage");
                    areStagesValid.set(false);
                }

                if (destructionData.crushingSize() > stage.value().sizeRange().max() || destructionData.crushingSize() < stage.value().sizeRange().min()) {
                    validationError.append("\n- Crushing size of [").append(key.location()).append("] is not within the bounds of the dragon stage");
                    areStagesValid.set(false);
                }
            }
        });

        // Validate that the default stages have a connected size range
        HolderSet<DragonStage> defaultStages = getDefaultStages(access);
        // Sort the default stages by size range
        HolderSet<DragonStage> sortedDefaultStages = HolderSet.direct(defaultStages.stream().sorted(Comparator.comparingDouble(stage -> stage.value().sizeRange().min())).toList());

        if (!stagesHaveContinuousSizeRange(sortedDefaultStages, validationError, true)) {
            areStagesValid.set(false);
        }

        if (!areStagesValid.get()) {
            throw new IllegalStateException(validationError.toString());
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore for clarity
    public static boolean stagesHaveContinuousSizeRange(final HolderSet<DragonStage> sortedStages, final StringBuilder error, boolean isForDefaultStages) {
        for (int i = 0; i < sortedStages.size() - 1; i++) {
            if (sortedStages.get(i).value().sizeRange().max() != sortedStages.get(i + 1).value().sizeRange().min()) {
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

    public double ticksToSize(int ticks) {
        return (sizeRange().max() - sizeRange().min()) / ticksUntilGrown() * ticks;
    }

    public double getProgress(double size) {
        return ((size - sizeRange().min())) / (sizeRange().max() - sizeRange().min());
    }

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

    public static Component translatableName(final ResourceKey<DragonStage> dragonStage) {
        return Component.translatable(Translation.Type.STAGE.wrap(dragonStage.location()));
    }

    public double getBoundedSize(double size) {
        return Math.clamp(size, sizeRange().min(), sizeRange().max());
    }

    /** Returns the bounds between the smallest and largest dragon sizes */
    public static MiscCodecs.Bounds getBounds() {
        return new MiscCodecs.Bounds(minSize, maxSize);
    }

    public static double getStartingSize(final HolderSet<DragonStage> stages) {
        return stages.stream().min(Comparator.comparingDouble(stage -> stage.value().sizeRange().min()))
                .map(stage -> stage.value().sizeRange().min()).orElse(DragonStateHandler.NO_SIZE);
    }

    /**
     * Returns a stage from the provided set whose size range matches the provided size <br>
     * It is not a match if the size equals to the max. size of the stage <br>
     * (This is because if we provide such a size we usually want the next stage, not the current stage at max. size)
     */
    public static Holder<DragonStage> get(final HolderSet<DragonStage> stages, double size) {
        Holder<DragonStage> smallest = null;
        Holder<DragonStage> largest = null;

        for (Holder<DragonStage> stage : stages) {
            if (stage.value().sizeRange().matches(size) && stage.value().sizeRange().max() != size) {
                return stage;
            }

            if (smallest == null || stage.value().sizeRange().min() < smallest.value().sizeRange().min()) {
                smallest = stage;
            }

            if (largest == null || stage.value().sizeRange().min() > largest.value().sizeRange().min()) {
                largest = stage;
            }
        }

        //noinspection DataFlowIssue -> stage should not be null at this point
        if (size <= smallest.value().sizeRange().min()) {
            return smallest;
        }

        return largest;
    }

    /** TODO :: is this even usable? it could select a stage that is not valid for the current species
     * Tries to retrieve a valid stage for the provided size <br>
     * If no size range matches either the smallest or largest stage will be returned <br>
     * (Depending on whose min. size is closer to the provided size)
     */
    public static Holder<DragonStage> get(@Nullable final HolderLookup.Provider provider, double size) {
        double fallbackDifference = Double.MAX_VALUE;
        Holder<DragonStage> fallback = null;

        for (Holder.Reference<DragonStage> level : ResourceHelper.all(provider, REGISTRY)) {
            if (level.value().sizeRange().matches(size)) {
                return level;
            }

            double difference = Math.abs(level.value().sizeRange().min() - size);

            if (fallback == null || difference < fallbackDifference) {
                fallbackDifference = difference;
                fallback = level;
            }
        }

        if (fallback != null) {
            DragonSurvival.LOGGER.warn("No matching dragon level found for size [{}] - using [{}] as fallback", size, fallback.getRegisteredName());
            return fallback;
        }

        throw new IllegalStateException("There is no valid dragon level for the supplied size [" + size + "]");
    }

    private static Pair<Double, Double> getSizes(@Nullable final HolderLookup.Provider provider) {
        DragonStage smallest = null;
        DragonStage largest = null;

        for (Holder.Reference<DragonStage> level : ResourceHelper.all(provider, REGISTRY)) {
            if (smallest == null || level.value().sizeRange().min() < smallest.sizeRange().min()) {
                smallest = level.value();
            }

            if (largest == null || level.value().sizeRange().max() > largest.sizeRange().max()) {
                largest = level.value();
            }
        }

        //noinspection DataFlowIssue -> stages should not be null
        return Pair.of(smallest.sizeRange.min(), largest.sizeRange.max());
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

        double sizeToTicks = (sizeRange().max() - sizeRange().min()) / ticksUntilGrown();
        double missingSize = sizeRange().max() - size;
        Functions.Time time = Functions.Time.fromTicks((int) (missingSize / sizeToTicks));

        if (time.hasTime()) {
            ageInformation += " (" + time.format() + ")";
        }

        return ageInformation;
    }

    public String getTimeToGrowFormatted(boolean growthStopped) {
        if(growthStopped) {
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
