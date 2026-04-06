package by.dragonsurvivalteam.dragonsurvival.common.capability;

import com.mojang.datafixers.util.Either;
import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.TimeComponent;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDesiredGrowth;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncGrowth;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HarvestBonuses;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.InputData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonRidingHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.PlayerMessageUtil;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class DragonStateHandler extends EntityStateHandler {
    @Translation(comments = "You cannot turn into a human in this world")
    private static final String NO_HUMANS = Translation.Type.GUI.wrap("message.no_humans");

    public static final double NO_GROWTH = -1;

    private static final double AGE_LERP_SPEED = 0.1; // 10% per tick
    private static final double AGE_EPSILON = 0.01;

    public MultiMining multiMining = MultiMining.ENABLED;
    public LargeDragonDestruction largeDragonDestruction = LargeDragonDestruction.ENABLED;

    private static final Codec<HashMap<ResourceKey<DragonStage>, HashMap<Item, Integer>>> USED_GROWTH_ITEMS_CODEC =
            Codec.unboundedMap(
                    ResourceKey.codec(DragonStage.REGISTRY),
                    // Need to convert to an actual map to make it modifiable
                    Codec.unboundedMap(Item.CODEC.xmap(Holder::value, Item::builtInRegistryHolder), Codec.INT).xmap(HashMap::new, Function.identity())
            ).xmap(HashMap::new, Function.identity());
    // Gets reset once the growth reaches the starting growth of the dragon species
    // Currently also stores the usages of non-limited items (but doesn't limit them by doing so)
    // (Preferably it would not but the additional checks may not be worth it)
    private HashMap<ResourceKey<DragonStage>, HashMap<Item, Integer>> usedGrowthItems = new HashMap<>();

    public boolean isGrowthStopped;
    public boolean isGrowing = true;

    public int magicSource;
    public boolean isOnMagicSource;
    public boolean markedByEnderDragon;
    public boolean flightWasGranted;
    public boolean spinWasGranted;

    public boolean refreshBody;
    /** Currently only set when the dimension refresh occurs due to a size (scale) change */
    public boolean shouldFudgePosition;

    /** Last timestamp the server synchronized the player */
    public int lastSync;

    private final Map<ResourceKey<DragonSpecies>, Double> savedGrowth = new HashMap<>();
    private SkinData skinData = new SkinData();

    private Holder<DragonSpecies> dragonSpecies;
    private Holder<DragonBody> dragonBody;
    private Holder<DragonStage> dragonStage;

    private int passengerId = DragonRidingHandler.NO_PASSENGER;
    private double growth = NO_GROWTH;
    private double visualGrowth = NO_GROWTH;
    private double visualGrowthLastTick = NO_GROWTH;
    private double desiredGrowth = NO_GROWTH;

    private boolean destructionEnabled;

    public Pose previousPose;

    // Needed to calculate collision damage correctly when flying. See ServerFlightHandler.
    public Vec3 preCollisionDeltaMovement = Vec3.ZERO;

    private static final RandomSource RANDOM = RandomSource.create();

    public void setRandomValidStage(@Nullable final Player player) {
        if (dragonSpecies == null) {
            return;
        }

        HolderSet<DragonStage> stages = dragonSpecies.value().getStages(player != null ? player.registryAccess() : null);
        setStage(player, stages.getRandomElement(player != null ? player.getRandom() : RANDOM).orElseThrow());
    }

    /** Sets the stage and retains the current age */
    public void setStage(@Nullable final Player player, final Holder<DragonStage> dragonStage) {
        if (!dragonSpecies.value().getStages(player != null ? player.registryAccess() : null).contains(dragonStage)) {
            //noinspection DataFlowIssue -> key is present
            Functions.logOrThrow("The dragon stage [" + dragonStage.getKey().identifier() + "] is not valid for the dragon species [" + speciesId() + "]");
            return;
        }

        double boundedGrowth = dragonStage.value().getBoundedGrowth(growth);

        if (boundedGrowth == dragonStage.value().growthRange().max()) {
            // Ties go to the larger stage, so we need to be slightly below the maximum growth of the stage
            boundedGrowth -= Shapes.EPSILON;
        }

        if (this.dragonStage == null) {
            // No reason to slowly adjust to the growth in this case
            setDesiredGrowth(player, boundedGrowth);
            setGrowth(player, desiredGrowth);
        } else {
            setDesiredGrowth(player, boundedGrowth);
        }
    }

    /**
     * - Server-side: lerp to the actual growth <br>
     * - Client-side: update the visual growth
     */
    public void lerpGrowth(final Player player) {
        if (player.level().isClientSide() && visualGrowth - desiredGrowth == 0) return;
        if (!player.level().isClientSide() && growth - desiredGrowth == 0) return;

        // Check the marginal growth, not the desired growth, as otherwise you'll end up with your growth stunted if you
        // can't reach the maximum desired growth, even when there is room to grow to some percentage of the desired growth
        double growthForNextTick = Mth.lerp(AGE_LERP_SPEED, player.level().isClientSide() ? visualGrowth : growth, desiredGrowth);
        boolean isGrowthAllowed = DragonGrowthHandler.isGrowthAllowed(player, DragonStateProvider.getData(player), growthForNextTick);

        if (player.level().isClientSide()) {
            if (visualGrowth == NO_GROWTH) {
                visualGrowth = growth;
            }

            visualGrowthLastTick = visualGrowth;

            // Need to update the visualGrowthLastTick to prevent weird jittering due to partial tick interpolation, even when growth is blocked
            if (!isGrowthAllowed) return;

            if (Math.abs(visualGrowth - desiredGrowth) < AGE_EPSILON) {
                visualGrowth = desiredGrowth;
            } else {
                visualGrowth = Mth.lerp(AGE_LERP_SPEED, visualGrowth, desiredGrowth);
            }
        } else if (Math.abs(growth - desiredGrowth) < AGE_EPSILON) {
            if (!isGrowthAllowed) return;
            setGrowth(player, desiredGrowth);
        } else {
            if (!isGrowthAllowed) return;
            setGrowth(player, Mth.lerp(AGE_LERP_SPEED, growth, desiredGrowth));
        }
    }

    public void setGrowth(@Nullable final Player player, final double growth) {
        setGrowth(player, growth, false);
    }

    /**
     * @param forceUpdate Bypass the check that could result in skipping updating modifiers / synchronizing the state to the client <br>
     *                    Needed after de-serialization of the data (since that had no player context)
     */
    public void setGrowth(@Nullable final Player player, final double growth, final boolean forceUpdate) {
        double oldGrowth = this.growth;
        Holder<DragonStage> oldStage = dragonStage;
        updateGrowthAndStage(player != null ? player.registryAccess() : null, growth);

        if (player == null) {
            return;
        }

        if (dragonStage == null) {
            DSModifiers.updateGrowthModifiers(player, this);
            return;
        }

        if (!forceUpdate && oldGrowth == this.growth && oldStage != null && dragonStage.is(oldStage)) {
            // There is no need to refresh the dimensions / fudge the position in this case
            // the visual size is only for the client (rendering) and therefor doesn't cause position desync
            return;
        }

        // Update modifiers before refreshing the dimensions, as the growth modifiers may affect them
        DSModifiers.updateGrowthModifiers(player, this);
        shouldFudgePosition = true;
        player.refreshDimensions();
        shouldFudgePosition = false;

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncGrowth(serverPlayer.getId(), getGrowth()));
            MagicData.getData(player).handleAutoUpgrades(serverPlayer, InputData.growth((int) this.growth));
            DSAdvancementTriggers.BE_DRAGON.get().trigger(serverPlayer);
        }

        if (player.level().isClientSide()) {
            ClientProxy.sendClientData();
        }
    }

    public void setDesiredGrowth(@Nullable final Player player, double growth) {
        if (player == null) {
            desiredGrowth = growth;
            setGrowth(null, growth);
            return;
        }

        desiredGrowth = clampGrowth(player.registryAccess(), growth);

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncDesiredGrowth(serverPlayer.getId(), desiredGrowth));
        }
    }

    private double clampGrowth(@Nullable final HolderLookup.Provider provider, double growth) {
        MiscCodecs.Bounds bounds = DragonStage.getBounds();
        double newGrowth = Math.clamp(growth, bounds.min(), bounds.max());

        if (dragonSpecies == null) {
            return newGrowth;
        }

        Holder<DragonStage> stage = DragonStage.get(dragonSpecies.value().getStages(provider), newGrowth);
        return stage.value().getBoundedGrowth(newGrowth);
    }

    private void updateGrowthAndStage(@Nullable final HolderLookup.Provider provider, double growth) {
        if (growth == NO_GROWTH) {
            dragonStage = null;
            this.growth = NO_GROWTH;
            this.desiredGrowth = NO_GROWTH;
            return;
        }

        dragonStage = dragonSpecies != null ? DragonStage.get(dragonSpecies.value().getStages(provider), growth) : null;
        this.growth = clampGrowth(provider, growth);

        if (dragonSpecies != null && this.growth == dragonSpecies.value().getStartingGrowth(provider)) {
            // Allow the player to re-use growth items if their growth is reset
            // Don't clear if the player is a human in case the growth is saved
            // It will be cleared once they turn into a dragon, and its growth matches the requirements
            usedGrowthItems.clear();
        }
    }

    public List<Holder<DragonStage>> getStagesSortedByProgression(@Nullable final HolderLookup.Provider provider) {
        List<Holder<DragonStage>> stages = getStages(provider);
        List<Holder<DragonStage>> sortedStages = new ArrayList<>(stages);
        sortedStages.sort(Comparator.comparingDouble(stage -> stage.value().growthRange().min()));
        return sortedStages;
    }

    public List<Holder<DragonStage>> getStages(@Nullable final HolderLookup.Provider provider) {
        if (dragonSpecies.value().stages().isPresent()) {
            return dragonSpecies.value().stages().get().stream().toList();
        } else {
            return DragonStage.getDefaultStages(provider).stream().toList();
        }
    }

    /** Should only be called if the player is a dragon */
    public void incrementGrowthUses(final Item item) {
        Map<Item, Integer> items = usedGrowthItems.computeIfAbsent(stageKey(), key -> new HashMap<>());
        items.compute(item, (key, timesUsed) -> timesUsed == null ? 1 : timesUsed + 1);
    }

    /** Should only be called if the player is a dragon */
    public int getGrowthUses(final Item item) {
        Map<Item, Integer> items = usedGrowthItems.get(stageKey());

        if (items == null) {
            return 0;
        }

        Integer uses = items.get(item);

        if (uses == null) {
            return 0;
        }

        return uses;
    }

    public Holder<DragonSpecies> species() {
        return dragonSpecies;
    }

    /** Should only be called if the player is a dragon */
    public ResourceKey<DragonSpecies> speciesKey() {
        return species().getKey();
    }

    /** Should only be called if the player is a dragon */
    public Identifier speciesId() {
        return speciesKey().identifier();
    }

    public Holder<DragonStage> stage() {
        return dragonStage;
    }

    public Holder<DragonStage> stageFromDesiredSize(Player player) {
        return DragonStage.get(dragonSpecies.value().getStages(player.registryAccess()), desiredGrowth);
    }

    /** Should only be called if the player is a dragon */
    public ResourceKey<DragonStage> stageKey() {
        return stage().getKey();
    }

    /** Should only be called if the player is a dragon */
    public Identifier stageId() {
        return stageKey().identifier();
    }

    public Holder<DragonBody> body() {
        return dragonBody;
    }

    /** Should only be called if the player is a dragon */
    public ResourceKey<DragonBody> bodyKey() {
        return body().getKey();
    }

    /** Should only be called if the player is a dragon */
    public Identifier bodyId() {
        return bodyKey().identifier();
    }

    public void refreshMagicData(final ServerPlayer player, boolean forceRetainMagicData) {
        MagicData magic = MagicData.getData(player);

        if (!ServerConfig.saveAllAbilities && !forceRetainMagicData) {
            magic.refresh(player, dragonSpecies);
            flightWasGranted = false;
            spinWasGranted = false;
        } else {
            if (dragonSpecies == null || magic.dataForSpeciesIsEmpty(speciesKey())) {
                magic.refresh(player, dragonSpecies);
            } else {
                magic.setCurrentSpecies(player, speciesKey());
            }
        }

        TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
        magic.serialize(valueOutput);

        PacketDistributor.sendToPlayer(player, new SyncMagicData(valueOutput.buildResult()));
    }

    public void setSpecies(@Nullable final Player player, @Nullable final Holder<DragonSpecies> species, boolean savedForSoul) {
        Holder<DragonSpecies> oldSpecies = dragonSpecies;
        double oldGrowth = growth;
        dragonSpecies = species;

        boolean hasChanged = species != null && !DragonUtils.isSpecies(oldSpecies, species);

        if (hasChanged) {
            if (body() == null || !species.value().isValidForBody(body())) {
                if (player instanceof ServerPlayer serverPlayer) {
                    setBody(serverPlayer, DragonBody.getRandomUnlocked(serverPlayer));
                } else {
                    setBody(player, DragonBody.getRandom(player != null ? player.registryAccess() : null, species));
                }
            }

            // Also make sure we clamp our growth to a valid stage
            updateGrowthAndStage(player != null ? player.registryAccess() : null, getSavedDragonAge(speciesKey()));

            // The server doesn't need to check for skin preset refreshes; the client handles this
            if (FMLLoader.getCurrent().getDist().isClient()) {
                if (skinData.skinPresets.get().get(speciesKey()).isEmpty()) {
                    refreshSkinPresetForSpecies(dragonSpecies, dragonBody);
                    recompileCurrentSkin();
                }
            }
        }

        if (oldSpecies != null && !savedForSoul) {
            // Save the growth for the previous species if we have changed and it isn't due to a soul save
            savedGrowth.put(oldSpecies.getKey(), oldGrowth);
        } else if (oldSpecies != null) {
            // Clear out saved growth data if we are saving for soul, to prevent the player from getting their growth back and repeatedly saving to a soul
            savedGrowth.remove(oldSpecies.getKey());
        }

        if (player == null) {
            return;
        }

        if (hasChanged) {
            PenaltySupply.clear(player);
            DSModifiers.updateTypeModifiers(player, this);

            if (player instanceof ServerPlayer serverPlayer) {
                refreshMagicData(serverPlayer, false);
            }
        } else if (species == null) {
            PenaltySupply.clear(player);
            DSModifiers.clearModifiers(player);
        }
    }

    public void setSpecies(@Nullable final Player player, final Holder<DragonSpecies> species) {
        setSpecies(player, species, false);
    }

    /** Does *not* synchronize the change to the client */
    public void setBody(@Nullable final Player player, final Holder<DragonBody> dragonBody) {
        Holder<DragonBody> oldBody = this.dragonBody;
        this.dragonBody = dragonBody;
        boolean isSameBody = DragonUtils.isBody(oldBody, this.dragonBody);

        if (this.dragonBody != null && !isSameBody) {
            refreshBody = true;

            if (oldBody != null && this.dragonBody.value().model() != oldBody.value().model()) {
                // If the model has changed, just override the skin preset with the default one as a failsafe
                refreshSkinPresetForSpecies(dragonSpecies, this.dragonBody);

                recompileCurrentSkin();
            }
        }

        if (player == null) {
            return;
        }

        if (!isSameBody) {
            DSModifiers.updateBodyModifiers(player, this);
        }
    }

    /** Determines if the current dragon species can harvest the supplied block (with or without tools) (configured harvest bonuses are taken into account) */
    public boolean canHarvestWithPaw(final Player player, final BlockState state) {
        if (!ToolUtils.shouldUseDragonTools(player.getMainHandItem())) {
            // Player is holding a tool in the hotbar
            return HarvestBonuses.canHarvest(player, state, player.getMainHandItem());
        }

        return HarvestBonuses.canHarvest(player, state, ClawInventoryData.getData(player).getTool(state));
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public double getVisualScale(final Player player, float partialTick) {
        if (!DragonSurvival.PROXY.isOnRenderThread()) {
            Functions.logOrThrow("Visual scale update should only be used for rendering purposes!");
            return player.getAttributeValue(Attributes.SCALE);
        }

        // Missing attribute would result in an unstable environment / experience
        AttributeInstance instance = Objects.requireNonNull(player.getAttribute(Attributes.SCALE));
        double partialVisualGrowth = Mth.lerp(partialTick, visualGrowthLastTick, visualGrowth);

        if (DragonSurvival.PROXY.isFakePlayer(player)) {
            double scale = DragonSurvival.PROXY.getFakePlayerScale(player);
            return scale == -1 ? instance.getValue() : scale;
        }

        if (partialVisualGrowth == visualGrowth) {
            return instance.getValue();
        }

        return calculateScale(instance, partialVisualGrowth);
    }

    public float calculateScale(final AttributeInstance scale, final double growth) {
        List<AttributeModifier> attributeModifiers = stage().value().filterModifiers(scale);
        List<Modifier> modifiers = stage().value().modifiers().stream().filter(modifier -> modifier.attribute().is(Attributes.SCALE)).toList();

        return (float) Functions.calculateAttributeValue(scale, growth - stage().value().growthRange().min(), attributeModifiers, modifiers);
    }

    public double getGrowth() {
        return growth;
    }

    public double getDesiredGrowth() {
        return desiredGrowth;
    }

    public boolean isDragon() {
        return dragonSpecies != null && dragonBody != null && dragonStage != null;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public SkinData getSkinData() {
        return skinData;
    }

    public Identifier getModel() {
        return dragonBody.value().model();
    }

    public void setSkinPresetForType(final ResourceKey<DragonSpecies> dragonSpecies, SkinPreset preset) {
        skinData.skinPresets.get().put(dragonSpecies, preset);
    }

    public void setCurrentSkinPreset(final SkinPreset preset) {
        skinData.skinPresets.get().put(speciesKey(), preset);
        recompileCurrentSkin();
    }

    public SkinPreset getCurrentSkinPreset() {
        return skinData.skinPresets.get().get(speciesKey());
    }

    public void refreshSkinPresetForSpecies(final Holder<DragonSpecies> species, final Holder<DragonBody> body) {
        SkinPreset freshSkinPreset = new SkinPreset();
        freshSkinPreset.initDefaults(species, body != null ? body.value().model() : DragonBody.DEFAULT_MODEL);
        skinData.skinPresets.get().put(species.getKey(), freshSkinPreset);
    }

    public SkinPreset getSkinPresetForSpecies(final Holder<DragonSpecies> species, final Holder<DragonBody> body) {
        SkinPreset skinPreset = skinData.skinPresets.get().get(species.getKey());

        if (skinPreset.isEmpty()) {
            refreshSkinPresetForSpecies(species, body);
        }

        return skinData.skinPresets.get().get(species.getKey());
    }

    public void recompileCurrentSkin() {
        if (!isDragon()) {
            return;
        }

        skinData.compileSkin(stageKey());
    }

    public void setCurrentStageCustomization(final DragonStageCustomization customization) {
        skinData.skinPresets.get().get(speciesKey()).put(stageKey(), Lazy.of(() -> customization));
    }

    public DragonStageCustomization getCurrentStageCustomization() {
        Lazy<DragonStageCustomization> customizationLazy = skinData.get(speciesKey(), stageKey());
        if (customizationLazy == null) {
            DragonSurvival.LOGGER.error("Failed to get customization for species [{}] and stage [{}]. Returning empty customization.", speciesId(), stageId());
            return new DragonStageCustomization(); // Return a default customization if none exists
        }

        return customizationLazy.get();
    }

    public DragonStageCustomization getCustomizationForStageAndSpecies(final ResourceKey<DragonSpecies> species, final ResourceKey<DragonStage> stage) {
        return skinData.get(species, stage).get();
    }

    public void serialize(ValueOutput valueOutput, boolean isDragonSoul) {
        if (isDragon()) {
            valueOutput.putString(DRAGON_SPECIES, speciesId().toString());
            valueOutput.putString(DRAGON_BODY, bodyId().toString());
            valueOutput.putString(DRAGON_STAGE, stageId().toString());
            valueOutput.putDouble(GROWTH, growth);
            valueOutput.putBoolean(IS_GROWTH_STOPPED, isGrowthStopped);
            valueOutput.putBoolean(IS_GROWING, isGrowing);
            valueOutput.putBoolean(DESTRUCTION_ENABLED, destructionEnabled);
            valueOutput.putBoolean(MARKED_BY_ENDER_DRAGON, markedByEnderDragon);
            valueOutput.putBoolean(WINGS_WAS_GRANTED, flightWasGranted);
            valueOutput.putBoolean(SPIN_WAS_GRANTED, spinWasGranted);
        }

        // TODO :: these probably shouldn't be applied to other players (dragon soul)?
        //  but a player re-using the should keep them
        //  -> store entity uuid in dragon soul and have separate loading method for dragon soul data with entity context
        valueOutput.putString(MULTI_MINING, multiMining.name());
        valueOutput.putString(GIANT_DRAGON_DESTRUCTION, largeDragonDestruction.name());

        if (isDragonSoul && dragonSpecies != null) {
            // Only store the growth of the dragon the player is currently in if we are saving for the soul
            storeSavedAge(speciesKey(), valueOutput);
            // Also, clear the saved growth for the current species in this case to prevent keeping growth data post-soul save
            savedGrowth.remove(speciesKey());
        } else if (!isDragonSoul) {
            for (ResourceKey<DragonSpecies> type : ResourceHelper.keys(null, DragonSpecies.REGISTRY)) {
                boolean hasSavedGrowth = savedGrowth.containsKey(type);

                if (hasSavedGrowth) {
                    storeSavedAge(type, valueOutput);
                }
            }
        }

        valueOutput.store(USED_GROWTH_ITEMS, USED_GROWTH_ITEMS_CODEC, usedGrowthItems);
        valueOutput.putChild(SKIN_DATA, skinData);
        super.serialize(valueOutput);
    }

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {
        serialize(valueOutput, false);
    }

    public void deserialize(ValueInput valueInput, boolean isDragonSoul) {
        dragonSpecies = null;
        dragonBody = null;
        dragonStage = null;

        String speciesId = valueInput.getStringOr(DRAGON_SPECIES, null);
        if (speciesId != null) {
            ResourceKey<DragonSpecies> speciesKey = ResourceKey.create(DragonSpecies.REGISTRY, Identifier.parse(speciesId));
            dragonSpecies = ResourceHelper.get(valueInput.lookup(), speciesKey).orElse(null);
        }

        String bodyId = valueInput.getStringOr(DRAGON_BODY, null);
        if (bodyId != null) {
            ResourceKey<DragonBody> bodyKey = ResourceKey.create(DragonBody.REGISTRY, Identifier.parse(bodyId));
            dragonBody = ResourceHelper.get(valueInput.lookup(), bodyKey).orElse(null);
        }

        String stageId = valueInput.getStringOr(DRAGON_STAGE, null);
        if (stageId != null) {
            ResourceKey<DragonStage> stageKey = ResourceKey.create(DragonStage.REGISTRY, Identifier.parse(stageId));
            dragonStage = ResourceHelper.get(valueInput.lookup(), stageKey).orElse(null);
        }

        multiMining = Functions.getEnum(MultiMining.class, valueInput.getStringOr(MULTI_MINING, MultiMining.ENABLED.name()));
        largeDragonDestruction = Functions.getEnum(LargeDragonDestruction.class, valueInput.getStringOr(GIANT_DRAGON_DESTRUCTION, LargeDragonDestruction.ENABLED.name()));

        if (dragonSpecies != null) {
            if (dragonBody == null) {
                // This can happen if a dragon body gets removed; we pick a random one, which will cause a desync between clients
                // But this situation should only happen during testing; the end user should not be removing body types once real gameplay is occurring
                dragonBody = DragonBody.getRandom(valueInput.lookup(), dragonSpecies);
            }

            // Makes sure that the set growth matches the previously set stage
            setGrowth(null, valueInput.getDoubleOr(GROWTH, 0));
            desiredGrowth = growth;
            destructionEnabled = valueInput.getBooleanOr(DESTRUCTION_ENABLED, false);
            isGrowing = !valueInput.getBooleanOr(IS_GROWING, false) || valueInput.getBooleanOr(IS_GROWING, false);
            isGrowthStopped = valueInput.getBooleanOr(IS_GROWTH_STOPPED, false);
            markedByEnderDragon = valueInput.getBooleanOr(MARKED_BY_ENDER_DRAGON, false);
            flightWasGranted = valueInput.getBooleanOr(WINGS_WAS_GRANTED, false);
            spinWasGranted = valueInput.getBooleanOr(SPIN_WAS_GRANTED, false);
        }

        if (dragonSpecies != null) {
            if (isDragonSoul) {
                // Load the growth from the saved growth in the tag when loading from a soul, rather than referencing the saved stage status
                savedGrowth.put(speciesKey(), growth);
            } else {
                for (ResourceKey<DragonSpecies> type : ResourceHelper.keys(valueInput.lookup(), DragonSpecies.REGISTRY)) {
                    Optional<ValueInput> valueInputChild = valueInput.child(type.identifier() + SAVED_GROWTH_SUFFIX);

                    if (!valueInputChild.isEmpty()) {
                        savedGrowth.put(type, loadSavedStage(type, valueInput));
                    }
                }
            }
        }

        this.usedGrowthItems = valueInput.read(USED_GROWTH_ITEMS, USED_GROWTH_ITEMS_CODEC).orElse(new HashMap<>());

        skinData = new SkinData();
        skinData.deserialize(valueInput.childOrEmpty(SKIN_DATA), dragonBody);
        super.deserialize(valueInput.childOrEmpty(ENTITY_STATE));

        if (isDragon()) {
            refreshBody = true;
            getSkinData().compileSkin(stageKey());
        }
    }

    private double loadSavedStage(final ResourceKey<DragonSpecies> dragonSpecies, final ValueInput valueInput) {
        Optional<ValueInput> valueInputChild = valueInput.child(dragonSpecies.identifier() + SAVED_GROWTH_SUFFIX);

        if (valueInputChild.isEmpty()) {
            Optional<Holder.Reference<DragonSpecies>> optional = ResourceHelper.get(valueInput.lookup(), dragonSpecies);

            if (optional.isPresent()) {
                return optional.get().value().getStartingGrowth(valueInput.lookup());
            } else {
                DragonSurvival.LOGGER.warn("Cannot load saved growth for dragon species [{}] while deserializing NBT of [{}] due to the dragon type not existing. Falling back to the smallest growth.", dragonSpecies, valueInput);
                return DragonStage.getBounds().min();
            }
        }

        return valueInputChild.orElseThrow().getDoubleOr(GROWTH, 0);
    }

    private void storeSavedAge(final ResourceKey<DragonSpecies> speciesKey, final ValueOutput valueOutput) {
        // FIXME
        /*CompoundTag savedGrowthTag = new CompoundTag();
        savedGrowthTag.putDouble(GROWTH, getSavedDragonAge(speciesKey));
        valueOutput.putChild(speciesKey.identifier() + SAVED_GROWTH_SUFFIX, savedGrowthTag);*/
    }

    @Override
    public void deserialize(final ValueInput valueInput) {
        deserialize(valueInput, false);
    }

    public void revertToHumanForm(final Player player, boolean isDragonSoul) {
        if (ServerConfig.noHumansAllowed) {
            PlayerMessageUtil.sendSystemMessage(player, Component.translatable(NO_HUMANS), true);
            return;
        }

        // Drop everything in your claw slots
        ClawInventoryData.reInsertClawTools(player);

        setSpecies(player, null, isDragonSoul);
        setBody(player, null);
        setDesiredGrowth(player, NO_GROWTH);

        AltarData altarData = AltarData.getData(player);
        altarData.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
        altarData.hasUsedAltar = true;
    }

    public boolean needsSkinRecompilation() {
        return isDragon() && getSkinData().needsSkinCompilation.getOrDefault(stageKey(), true);
    }

    public double getSavedDragonAge(final ResourceKey<DragonSpecies> type) {
        return savedGrowth.getOrDefault(type, NO_GROWTH);
    }

    private static final int MAX_SHOWN = 5;

    public Pair<List<Either<FormattedText, TooltipComponent>>, Integer> getGrowthDescription(int currentScroll) {
        DragonStage stage = dragonStage.value();
        double percentage = Math.clamp(stage.getProgress(getGrowth()), 0, 1);
        String ageInformation = stage.getTimeToGrowFormattedWithPercentage(percentage, getGrowth(), isGrowing);

        List<TimeComponent> growthItems = new ArrayList<>();

        stage().value().growthItems().forEach(growthItem -> {
            // A bit of wasted processing since not all are shown
            growthItem.items().forEach(item -> growthItems.add(new TimeComponent(item.value(), growthItem.growthInTicks(), TimeComponent.GROWTH)));
        });

        int scroll = currentScroll;
        if (growthItems.size() <= MAX_SHOWN) {
            scroll = 0;
        } else {
            scroll = Math.clamp(scroll, 0, growthItems.size() - MAX_SHOWN);
        }

        int max = Math.min(growthItems.size(), scroll + MAX_SHOWN);

        List<Either<FormattedText, TooltipComponent>> components = new ArrayList<>();
        components.add(Either.left(Component.translatable(LangKey.GROWTH_STAGE).append(DragonStage.translatableName(stageKey()))));
        components.add(Either.left(Component.translatable(LangKey.GROWTH_AGE, ageInformation)));
        components.add(Either.left(Component.translatable(LangKey.GROWTH_AMOUNT, (int) getGrowth())));
        components.add(Either.left(Component.translatable(LangKey.GROWTH_INFO).append(Component.literal(" [" + Math.min(growthItems.size(), scroll + MAX_SHOWN) + " / " + growthItems.size() + "]").withStyle(ChatFormatting.DARK_GRAY))));

        for (int i = scroll; i < max; i++) {
            components.add(Either.right(growthItems.get(i)));
        }

        return Pair.of(components, scroll);
    }

    @Translation(comments = "Multi Mining")
    public enum MultiMining {
        @Translation(comments = "Enabled")
        ENABLED,
        @Translation(comments = "Disabled")
        DISABLED
    }

    @Translation(comments = "Large Dragon Destruction")
    public enum LargeDragonDestruction {
        @Translation(comments = "Enabled")
        ENABLED,
        @Translation(comments = "Disabled")
        DISABLED
    }

    // Used by the dragon soul item
    public static final String DRAGON_SPECIES = "dragon_species";
    public static final String DRAGON_STAGE = "dragon_stage";
    public static final String GROWTH = "growth";

    private static final String DRAGON_BODY = "dragon_body";

    private static final String ENTITY_STATE = "entity_state";
    private static final String SKIN_DATA = "skin_data";

    private static final String SAVED_GROWTH_SUFFIX = "_saved_growth";

    private static final String MULTI_MINING = "multi_mining";
    private static final String GIANT_DRAGON_DESTRUCTION = "giant_dragon_destruction";

    private static final String IS_GROWTH_STOPPED = "is_growth_stopped";
    private static final String MARKED_BY_ENDER_DRAGON = "marked_by_ender_dragon";
    private static final String SPIN_WAS_GRANTED = "spin_was_granted";
    private static final String WINGS_WAS_GRANTED = "wings_was_granted";
    private static final String IS_GROWING = "is_growing";
    private static final String DESTRUCTION_ENABLED = "destruction_enabled";
    private static final String USED_GROWTH_ITEMS = "used_growth_items";
}
