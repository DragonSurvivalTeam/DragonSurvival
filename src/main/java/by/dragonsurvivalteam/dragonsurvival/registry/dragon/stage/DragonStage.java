package by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
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
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonStage(
        boolean isDefault,
        MiscCodecs.Bounds sizeRange,
        int ticksUntilGrown,
        List<Modifier> modifiers,
        List<MiscCodecs.GrowthItem> growthItems,
        Optional<EntityPredicate> isNaturalGrowthStopped,
        Optional<EntityPredicate> growIntoRequirements,
        Optional<MiscCodecs.DestructionData> destructionData
) implements AttributeModifierSupplier {
    public static final ResourceKey<Registry<DragonStage>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_stages"));

    public static final Codec<DragonStage> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("is_default", false).forGetter(DragonStage::isDefault),
            MiscCodecs.bounds().fieldOf("size_range").forGetter(DragonStage::sizeRange),
            ExtraCodecs.intRange(20, Functions.daysToTicks(365)).fieldOf("ticks_until_grown").forGetter(DragonStage::ticksUntilGrown),
            Modifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(DragonStage::modifiers),
            MiscCodecs.GrowthItem.CODEC.listOf().optionalFieldOf("growth_items", List.of()).forGetter(DragonStage::growthItems),
            EntityPredicate.CODEC.optionalFieldOf("is_natural_growth_stopped").forGetter(DragonStage::isNaturalGrowthStopped),
            EntityPredicate.CODEC.optionalFieldOf("grow_into_requirements").forGetter(DragonStage::growIntoRequirements),
            MiscCodecs.DestructionData.CODEC.optionalFieldOf("destruction_data").forGetter(DragonStage::destructionData)
    ).apply(instance, instance.stable(DragonStage::new)));

    public static final Codec<Holder<DragonStage>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonStage>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    /** Currently used for certain mechanics / animations */
    public static final double MAX_HANDLED_SIZE = 60; // TODO :: remove

    private static DragonStage smallest;
    private static DragonStage largest;

    public static void update(final RegistryAccess access) {
        Pair<DragonStage, DragonStage> sizes = getSizes(access);
        smallest = sizes.getFirst();
        largest = sizes.getSecond();

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

        if (!stagesHaveContinousSizeRange(sortedDefaultStages, validationError, true)) {
            areStagesValid.set(false);
        }

        if (!areStagesValid.get()) {
            throw new IllegalStateException(validationError.toString());
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore for clarity
    public static boolean stagesHaveContinousSizeRange(final HolderSet<DragonStage> sortedStages, final StringBuilder error, boolean isForDefaultStages) {
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

    public static List<Holder<DragonStage>> allStages(@Nullable final HolderLookup.Provider provider) {
        return ResourceHelper.keys(provider, REGISTRY).stream().map(key -> ResourceHelper.get(provider, key).orElseThrow().getDelegate()).toList();
    }

    public static Component translatableName(final ResourceKey<DragonStage> dragonStage) {
        return Component.translatable(Translation.Type.STAGE.wrap(dragonStage.location()));
    }

    public double getBoundedSize(double size) {
        return Math.clamp(size, sizeRange().min(), sizeRange().max());
    }

    /** Returns a valid size (meaning a size within the bounds of the smallest and largest dragon) */
    public static double getValidSize(double size) {
        return Math.clamp(size, smallest.sizeRange.min(), largest.sizeRange().max());
    }

    /** Returns the bounds between the smallest and largest dragon sizes */
    public static MiscCodecs.Bounds getBounds() {
        return new MiscCodecs.Bounds(smallest.sizeRange().min(), largest.sizeRange().max());
    }

    public static Holder<DragonStage> getStage(final HolderSet<DragonStage> stages, double size) {
        Holder<DragonStage> smallest = null;
        Holder<DragonStage> largest = null;

        for (Holder<DragonStage> stage : stages) {
            // Don't consider it a match if it is equal to the max size of the stage; we want to prefer the smaller stage always
            if (stage.value().sizeRange().matches(size) && stage.value().sizeRange().min() != size) {
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

    public static double getStartingSize(final HolderSet<DragonStage> stages) {
        return stages.stream().filter(stage -> stage.value().isDefault()).min(Comparator.comparingDouble(stage -> stage.value().sizeRange().min())).get().value().sizeRange().min();
    }

    public static Holder<DragonStage> get(@Nullable final HolderLookup.Provider provider, double size) {
        HolderLookup.RegistryLookup<DragonStage> registry;

        if (provider == null) {
            registry = CommonHooks.resolveLookup(REGISTRY);
        } else {
            registry = provider.lookupOrThrow(REGISTRY);
        }

        double fallbackDifference = Double.MAX_VALUE;
        Holder<DragonStage> fallback = null;

        //noinspection DataFlowIssue -> registry is expected to be present
        for (Holder.Reference<DragonStage> level : registry.listElements().toList()) {
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

    private static Pair<DragonStage, DragonStage> getSizes(@Nullable final HolderLookup.Provider provider) {
        HolderLookup.RegistryLookup<DragonStage> registry;

        if (provider == null) {
            registry = CommonHooks.resolveLookup(REGISTRY);
        } else {
            registry = provider.lookupOrThrow(REGISTRY);
        }

        DragonStage smallest = null;
        DragonStage largest = null;

        for (Holder.Reference<DragonStage> level : Objects.requireNonNull(registry).listElements().toList()) {
            if (smallest == null || level.value().sizeRange().min() < smallest.sizeRange().min()) {
                smallest = level.value();
            }

            if (largest == null || level.value().sizeRange().max() > largest.sizeRange().max()) {
                largest = level.value();
            }
        }

        return Pair.of(smallest, largest);
    }

    public static HolderSet<DragonStage> getDefaultStages(@Nullable final HolderLookup.Provider provider) {
        return HolderSet.direct(allStages(provider).stream().filter(stage -> stage.value().isDefault()).toList());
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
