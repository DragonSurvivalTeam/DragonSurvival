package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.TimeComponent;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
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
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
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

public class DragonStateHandler extends EntityStateHandler {
    @Translation(comments = "You cannot turn into a human in this world")
    private static final String NO_HUMANS = Translation.Type.GUI.wrap("message.no_humans");

    public static final double NO_GROWTH = -1;

    private static final double AGE_LERP_SPEED = 0.1; // 10% per tick
    private static final double AGE_EPSILON = 0.01;

    public MultiMining multiMining = MultiMining.ENABLED;
    public LargeDragonDestruction largeDragonDestruction = LargeDragonDestruction.ENABLED;

    // Gets reset once the growth reaches the starting growth of the dragon species
    // Currently also stores the usages of non-limited items (but doesn't limit them by doing so)
    // (Preferably it would not but the additional checks may not be worth it)
    private final Map<ResourceKey<DragonStage>, Map<Item, Integer>> usedGrowthItems = new HashMap<>();

    public boolean isGrowthStopped;
    public boolean isGrowing = true;

    public int magicSource;
    public boolean isOnMagicSource;
    public boolean markedByEnderDragon;
    public boolean flightWasGranted;
    public boolean spinWasGranted;

    public boolean refreshBody;

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
            Functions.logOrThrow("The dragon stage [" + dragonStage.getKey().location() + "] is not valid for the dragon species [" + speciesId() + "]");
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
        if (player.level().isClientSide()) {
            if (visualGrowth == NO_GROWTH) {
                visualGrowth = growth;
            }

            visualGrowthLastTick = visualGrowth;

            if (Math.abs(visualGrowth - desiredGrowth) < AGE_EPSILON) {
                visualGrowth = desiredGrowth;
            } else {
                visualGrowth = Mth.lerp(AGE_LERP_SPEED, visualGrowth, desiredGrowth);
            }
        } else if (Math.abs(growth - desiredGrowth) < AGE_EPSILON) {
            setGrowth(player, desiredGrowth);
            DragonSizeHandler.overridePose(player);
        } else {
            setGrowth(player, Mth.lerp(AGE_LERP_SPEED, growth, desiredGrowth));
        }
    }

    public void setGrowth(@Nullable final Player player, final double growth) {
        setGrowth(player, growth, false);
    }

    /**
     * @param forceUpdate Bypass the check that could result in skipping updating modifiers / synchronizing the state to the client <br>
     * Needed after de-serialization of the data (since that had no player context)
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
        player.refreshDimensions();

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
    public ResourceLocation speciesId() {
        return speciesKey().location();
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
    public ResourceLocation stageId() {
        return stageKey().location();
    }

    public Holder<DragonBody> body() {
        return dragonBody;
    }

    /** Should only be called if the player is a dragon */
    public ResourceKey<DragonBody> bodyKey() {
        return body().getKey();
    }

    /** Should only be called if the player is a dragon */
    public ResourceLocation bodyId() {
        return bodyKey().location();
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

        PacketDistributor.sendToPlayer(player, new SyncMagicData(magic.serializeNBT(player.registryAccess())));
    }

    public void setSpecies(@Nullable final Player player, final Holder<DragonSpecies> species) {
        Holder<DragonSpecies> oldSpecies = dragonSpecies;
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

            if (skinData.skinPresets.get().get(speciesKey()).isEmpty()) {
                refreshSkinPresetForSpecies(dragonSpecies, dragonBody);
                recompileCurrentSkin();
            }
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

        if (partialVisualGrowth == visualGrowth || DragonSurvival.PROXY.isFakePlayer(player)) {
            return instance.getValue();
        }

        List<AttributeModifier> attributeModifiers = stage().value().filterModifiers(instance);
        List<Modifier> modifiers = stage().value().modifiers().stream().filter(modifier -> modifier.attribute().is(Attributes.SCALE)).toList();

        return Functions.calculateAttributeValue(instance, partialVisualGrowth - stage().value().growthRange().min(), attributeModifiers, modifiers);
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

    public ResourceLocation getModel() {
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
        return skinData.get(speciesKey(), stageKey()).get();
    }

    public DragonStageCustomization getCustomizationForStage(final ResourceKey<DragonStage> stage) {
        return skinData.get(speciesKey(), stage).get();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider, boolean isDragonSoul) {
        CompoundTag tag = new CompoundTag();

        if (isDragon()) {
            tag.putString(DRAGON_SPECIES, speciesId().toString());
            tag.putString(DRAGON_BODY, bodyId().toString());
            tag.putString(DRAGON_STAGE, stageId().toString());
            tag.putDouble(GROWTH, growth);
            tag.putBoolean(IS_GROWTH_STOPPED, isGrowthStopped);
            tag.putBoolean(IS_GROWING, isGrowing);
            tag.putBoolean(DESTRUCTION_ENABLED, destructionEnabled);
            tag.putBoolean(MARKED_BY_ENDER_DRAGON, markedByEnderDragon);
            tag.putBoolean(WINGS_WAS_GRANTED, flightWasGranted);
            tag.putBoolean(SPIN_WAS_GRANTED, spinWasGranted);
        }

        // TODO :: these probably shouldn't be applied to other players (dragon soul)?
        //  but a player re-using the should keep them
        //  -> store entity uuid in dragon soul and have separate loading method for dragon soul data with entity context
        tag.putString(MULTI_MINING, multiMining.name());
        tag.putString(GIANT_DRAGON_DESTRUCTION, largeDragonDestruction.name());

        if (isDragonSoul && dragonSpecies != null) {
            // Only store the growth of the dragon the player is currently in if we are saving for the soul
            storeSavedAge(speciesId(), tag);
        } else if (!isDragonSoul) {
            for (ResourceKey<DragonSpecies> type : ResourceHelper.keys(provider, DragonSpecies.REGISTRY)) {
                boolean hasSavedGrowth = savedGrowth.containsKey(type);

                if (hasSavedGrowth) {
                    storeSavedAge(type.location(), tag);
                }
            }
        }

        CompoundTag usedGrowthItems = new CompoundTag();

        this.usedGrowthItems.forEach((key, items) -> {
            CompoundTag perStage = new CompoundTag();

            items.forEach((item, count) -> {
                //noinspection deprecation,DataFlowIssue -> ignore / key is present
                perStage.putInt(item.builtInRegistryHolder().getKey().location().toString(), count);
            });

            usedGrowthItems.put(key.location().toString(), perStage);
        });

        tag.put(USED_GROWTH_ITEMS, usedGrowthItems);
        tag.put(SKIN_DATA, skinData.serializeNBT(provider));
        tag.put(ENTITY_STATE, super.serializeNBT(provider));

        return tag;
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        return serializeNBT(provider, false);
    }

    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag tag, boolean isDragonSoul) {
        ResourceKey<DragonSpecies> species = ResourceHelper.decodeKey(provider, DragonSpecies.REGISTRY, tag, DRAGON_SPECIES);

        if (species != null) {
            dragonSpecies = provider.holderOrThrow(species);
        } else {
            dragonSpecies = null;
        }

        ResourceKey<DragonBody> body = ResourceHelper.decodeKey(provider, DragonBody.REGISTRY, tag, DRAGON_BODY);

        if (body != null) {
            dragonBody = provider.holderOrThrow(body);
        } else {
            dragonBody = null;
        }

        ResourceKey<DragonStage> stage = ResourceHelper.decodeKey(provider, DragonStage.REGISTRY, tag, DRAGON_STAGE);

        if (stage != null) {
            dragonStage = provider.holderOrThrow(stage);
        } else {
            dragonStage = null;
        }

        multiMining = Functions.getEnum(MultiMining.class, tag.getString(MULTI_MINING));
        largeDragonDestruction = Functions.getEnum(LargeDragonDestruction.class, tag.getString(GIANT_DRAGON_DESTRUCTION));

        if (dragonSpecies != null) {
            if (dragonBody == null) {
                // This can happen if a dragon body gets removed; we pick a random one, which will cause a desync between clients
                // But this situation should only happen during testing; the end user should not be removing body types once real gameplay is occurring
                dragonBody = DragonBody.getRandom(provider, dragonSpecies);
            }

            // Makes sure that the set growth matches the previously set stage
            setGrowth(null, tag.getDouble(GROWTH));
            desiredGrowth = growth;
            destructionEnabled = tag.getBoolean(DESTRUCTION_ENABLED);
            isGrowing = !tag.contains(IS_GROWING) || tag.getBoolean(IS_GROWING);
            isGrowthStopped = tag.getBoolean(IS_GROWTH_STOPPED);
            markedByEnderDragon = tag.getBoolean(MARKED_BY_ENDER_DRAGON);
            flightWasGranted = tag.getBoolean(WINGS_WAS_GRANTED);
            spinWasGranted = tag.getBoolean(SPIN_WAS_GRANTED);
        }

        if (dragonSpecies != null) {
            if (isDragonSoul) {
                // Only load the growth of the dragon the player is currently in if we are loading for the soul
                savedGrowth.put(speciesKey(), loadSavedStage(provider, speciesKey(), tag));
            } else {
                for (ResourceKey<DragonSpecies> type : ResourceHelper.keys(provider, DragonSpecies.REGISTRY)) {
                    CompoundTag compound = tag.getCompound(speciesId() + SAVED_GROWTH_SUFFIX);

                    if (!compound.isEmpty()) {
                        savedGrowth.put(type, loadSavedStage(provider, type, tag));
                    }
                }
            }
        }

        this.usedGrowthItems.clear();

        CompoundTag usedGrowthItems = tag.getCompound(USED_GROWTH_ITEMS);
        usedGrowthItems.getAllKeys().forEach(growthItemStage -> {
            ResourceLocation stageResource = ResourceLocation.tryParse(growthItemStage);

            if (stageResource == null) {
                // Just to be safe - would normally not occur
                return;
            }

            CompoundTag perStage = usedGrowthItems.getCompound(growthItemStage);
            ResourceKey<DragonStage> stageKey = ResourceKey.create(DragonStage.REGISTRY, stageResource);

            perStage.getAllKeys().forEach(itemResource -> {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(itemResource));

                if (item != Items.AIR) {
                    this.usedGrowthItems.computeIfAbsent(stageKey, ignored -> new HashMap<>()).put(item, perStage.getInt(itemResource));
                }
            });
        });

        skinData = new SkinData();
        skinData.deserializeNBT(provider, tag.getCompound(SKIN_DATA));
        super.deserializeNBT(provider, tag.getCompound(ENTITY_STATE));

        if (isDragon()) {
            refreshBody = true;
            getSkinData().compileSkin(stageKey());
        }
    }

    private double loadSavedStage(@NotNull final HolderLookup.Provider provider, final ResourceKey<DragonSpecies> dragonSpecies, final CompoundTag tag) {
        CompoundTag compound = tag.getCompound(dragonSpecies.location() + SAVED_GROWTH_SUFFIX);

        if (compound.isEmpty()) {
            Optional<Holder.Reference<DragonSpecies>> optional = ResourceHelper.get(provider, dragonSpecies);

            if (optional.isPresent()) {
                return optional.get().value().getStartingGrowth(provider);
            } else {
                DragonSurvival.LOGGER.warn("Cannot load saved growth for dragon species [{}] while deserializing NBT of [{}] due to the dragon type not existing. Falling back to the smallest growth.", dragonSpecies, tag);
                return DragonStage.getBounds().min();
            }
        }

        return compound.getDouble(GROWTH);
    }

    private void storeSavedAge(final ResourceLocation dragonSpecies, final CompoundTag tag) {
        CompoundTag savedGrowthTag = new CompoundTag();
        savedGrowthTag.putDouble(GROWTH, growth);
        tag.put(dragonSpecies + SAVED_GROWTH_SUFFIX, savedGrowthTag);
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        deserializeNBT(provider, tag, false);
    }

    public void revertToHumanForm(final Player player, boolean isDragonSoul) {
        if (ServerConfig.noHumansAllowed) {
            player.displayClientMessage(Component.translatable(NO_HUMANS), true);
            return;
        }

        // Don't set the saved dragon growth if we are reverting from a soul, as we already are storing the growth of the dragon in the soul
        if (ServerConfig.saveGrowthStage && !isDragonSoul && dragonSpecies != null) {
            savedGrowth.put(speciesKey(), getGrowth());
        }

        // Drop everything in your claw slots
        ClawInventoryData.reInsertClawTools(player);

        setSpecies(player, null);
        setBody(player, null);
        setDesiredGrowth(player, NO_GROWTH);

        AltarData altarData = AltarData.getData(player);
        altarData.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
        altarData.hasUsedAltar = true;
    }

    public boolean needsSkinRecompilation() {
        return isDragon() && getSkinData().recompileSkin.getOrDefault(stageKey(), true);
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
    public static final String GROWTH = "growth";

    private static final String DRAGON_BODY = "dragon_body";
    private static final String DRAGON_STAGE = "dragon_stage";

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