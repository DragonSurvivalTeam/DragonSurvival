package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.*;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DataReloadHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.ItemBlacklistPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DragonType implements AttributeModifierSupplier {
    public static final ResourceKey<Registry<DragonType>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_types"));

    public static final Codec<DragonType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("starting_size").forGetter(DragonType::startingSize),
            // No defined stages means all are applicable
            // TODO :: rename to stage_progression / custom_stage_progression or sth. like that?
            RegistryCodecs.homogeneousList(DragonStage.REGISTRY).optionalFieldOf("stages").forGetter(DragonType::stages),
            // No defined bodies means all are applicable
            RegistryCodecs.homogeneousList(DragonBody.REGISTRY).optionalFieldOf("bodies", HolderSet.empty()).forGetter(DragonType::bodies),
            RegistryCodecs.homogeneousList(DragonAbility.REGISTRY).optionalFieldOf("abilities", HolderSet.empty()).forGetter(DragonType::abilities),
            RegistryCodecs.homogeneousList(DragonPenalty.REGISTRY).optionalFieldOf("penalties", HolderSet.empty()).forGetter(DragonType::penalties),
            Modifier.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(DragonType::modifiers),
            DietEntry.CODEC.listOf().optionalFieldOf("diet", List.of()).forGetter(DragonType::diet),
            MiscDragonTextures.CODEC.fieldOf("misc_resources").forGetter(DragonType::miscResources)
    ).apply(instance, instance.stable(DragonType::new)));

    public static final Codec<Holder<DragonType>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonType>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    private final Optional<Double> startingSize;
    private final Optional<HolderSet<DragonStage>> stages;
    private final HolderSet<DragonBody> bodies;
    private final HolderSet<DragonAbility> abilities;
    private final HolderSet<DragonPenalty> penalties;
    private final List<Modifier> modifiers;
    private final List<DietEntry> dietEntries;
    private final MiscDragonTextures miscResources;

    private Map<Item, FoodProperties> diet;
    private long lastDietUpdate;

    public DragonType(final Optional<Double> startingSize, final Optional<HolderSet<DragonStage>> stages, final HolderSet<DragonBody> bodies, final HolderSet<DragonAbility> abilities, final HolderSet<DragonPenalty> penalties, List<Modifier> modifiers, final List<DietEntry> dietEntries, final MiscDragonTextures miscResources) {
        this.startingSize = startingSize;
        this.stages = stages;
        this.bodies = bodies;
        this.abilities = abilities;
        this.penalties = penalties;
        this.modifiers = modifiers;
        this.dietEntries = dietEntries;
        this.miscResources = miscResources;
    }

    public static void validate(@Nullable final HolderLookup.Provider provider) {
        StringBuilder validationError = new StringBuilder("The following types are incorrectly defined:");
        AtomicBoolean areTypesValid = new AtomicBoolean(true);

        ResourceHelper.keys(provider, REGISTRY).forEach(key -> {
            //noinspection OptionalGetWithoutIsPresent -> ignore
            Holder.Reference<DragonType> type = ResourceHelper.get(provider, key).get();

            if (type.value().stages().isPresent()) {
                if (!DragonStage.stagesHaveContinousSizeRange(type.value().stages().get(), validationError, false)) {
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

    public List<Item> getDietItems() {
        return List.copyOf(getDiet().keySet());
    }

    public @Nullable FoodProperties getDiet(final Item item) {
        return getDiet().get(item);
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
        return stages.orElseGet(() -> DragonStage.getDefaultStages(provider));
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

    @Override
    public List<Modifier> modifiers() {
        return modifiers;
    }

    public Optional<HolderSet<DragonStage>> stages() {
        return stages;
    }

    public HolderSet<DragonBody> bodies() {
        return bodies;
    }

    public HolderSet<DragonAbility> abilities() {
        return abilities;
    }

    public List<DietEntry> diet() {
        return dietEntries;
    }

    public MiscDragonTextures miscResources() {
        return miscResources;
    }

    public HolderSet<DragonPenalty> penalties() {
        return penalties;
    }

    private Map<Item, FoodProperties> getDiet() {
        if (diet == null || lastDietUpdate < DataReloadHandler.lastReload) {
            lastDietUpdate = System.currentTimeMillis();
            diet = DietEntry.map(dietEntries);
        }

        return diet;
    }
}
