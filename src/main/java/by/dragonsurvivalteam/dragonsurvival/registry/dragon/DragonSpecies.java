package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthIcon;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscDragonTextures;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.ItemBlacklistPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DragonSpecies implements AttributeModifierSupplier {
    public static final ResourceKey<Registry<DragonSpecies>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_species"));

    public static final Codec<DragonSpecies> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("starting_size").forGetter(DragonSpecies::startingSize),
            // No defined stages means all are applicable
            RegistryCodecs.homogeneousList(DragonStage.REGISTRY).optionalFieldOf("custom_stage_progression").forGetter(DragonSpecies::stages),
            // No defined bodies means all are applicable
            RegistryCodecs.homogeneousList(DragonBody.REGISTRY).optionalFieldOf("bodies", HolderSet.empty()).forGetter(DragonSpecies::bodies),
            RegistryCodecs.homogeneousList(DragonAbility.REGISTRY).optionalFieldOf("abilities", HolderSet.empty()).forGetter(DragonSpecies::abilities),
            RegistryCodecs.homogeneousList(DragonPenalty.REGISTRY).optionalFieldOf("penalties", HolderSet.empty()).forGetter(DragonSpecies::penalties),
            MiscDragonTextures.CODEC.fieldOf("misc_resources").forGetter(DragonSpecies::miscResources)
    ).apply(instance, instance.stable(DragonSpecies::new)));

    public static final Codec<Holder<DragonSpecies>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonSpecies>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    private final Optional<Double> startingSize;
    private final Optional<HolderSet<DragonStage>> customStageProgression;
    private final HolderSet<DragonBody> bodies;
    private final HolderSet<DragonAbility> abilities;
    private final HolderSet<DragonPenalty> penalties;
    private final MiscDragonTextures miscResources;

    public DragonSpecies(final Optional<Double> startingSize, final Optional<HolderSet<DragonStage>> customStageProgression, final HolderSet<DragonBody> bodies, final HolderSet<DragonAbility> abilities, final HolderSet<DragonPenalty> penalties, final MiscDragonTextures miscResources) {
        this.startingSize = startingSize;
        this.customStageProgression = customStageProgression;
        this.bodies = bodies;
        this.abilities = abilities;
        this.penalties = penalties;
        this.miscResources = miscResources;
    }

    public static void validate(@Nullable final HolderLookup.Provider provider) {
        StringBuilder validationError = new StringBuilder("The following types are incorrectly defined:");
        AtomicBoolean areTypesValid = new AtomicBoolean(true);

        ResourceHelper.keys(provider, REGISTRY).forEach(key -> {
            //noinspection OptionalGetWithoutIsPresent -> ignore
            Holder.Reference<DragonSpecies> type = ResourceHelper.get(provider, key).get();

            if (type.value().stages().isPresent()) {
                if (!DragonStage.stagesHaveContinuousSizeRange(type.value().stages().get(), validationError, false)) {
                    areTypesValid.set(false);
                }
            }
        });

        if (!areTypesValid.get()) {
            throw new IllegalStateException(validationError.toString());
        }
    }

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

    public boolean isItemBlacklisted(final Item item) {
        for (Holder<DragonPenalty> penalty : penalties) {
            if (penalty.value().effect() instanceof ItemBlacklistPenalty blacklist && blacklist.isBlacklisted(item)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ModifierType getModifierType() {
        return ModifierType.DRAGON_TYPE;
    }

    /**
     * Returns the configured starting size or the smallest size of the configured stages <br>
     * If no configured stages are present it will return the smallest size of the default stages
     */
    public double getStartingSize(@Nullable final HolderLookup.Provider provider) {
        return startingSize.orElseGet(() -> DragonStage.getStartingSize(getStages(provider)));
    }

    public HolderSet<DragonStage> getStages(@Nullable final HolderLookup.Provider provider) {
        return customStageProgression.orElseGet(() -> DragonStage.getDefaultStages(provider));
    }

    public GrowthIcon getGrowthIcon(final Holder<DragonStage> stage) {
        for (GrowthIcon growthIcon : miscResources.growthIcons()) {
            if (growthIcon.dragonStage() == stage.getKey()) {
                return growthIcon;
            }
        }

        return MiscDragonTextures.DEFAULT_GROWTH_ICON;
    }

    public ResourceLocation getHoverGrowthIcon(final Holder<DragonStage> stage) {
        for (GrowthIcon growthIcon : miscResources.growthIcons()) {
            if (growthIcon.dragonStage() == stage.getKey()) {
                return growthIcon.hoverIcon();
            }
        }

        return MiscDragonTextures.DEFAULT_GROWTH_HOVER_ICON;
    }

    public Optional<Double> startingSize() {
        return startingSize;
    }

    public Optional<HolderSet<DragonStage>> stages() {
        return customStageProgression;
    }

    public HolderSet<DragonBody> bodies() {
        return bodies;
    }

    public HolderSet<DragonAbility> abilities() {
        return abilities;
    }

    public MiscDragonTextures miscResources() {
        return miscResources;
    }

    public HolderSet<DragonPenalty> penalties() {
        return penalties;
    }
}
