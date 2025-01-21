package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.TimeComponent;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDesiredSize;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncSize;
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
    public static final double NO_SIZE = -1;

    private static final double SIZE_LERP_SPEED = 0.1; // 10% per tick
    private static final double SIZE_EPSILON = 0.01;

    public MultiMining multiMining = MultiMining.ENABLED;
    public LargeDragonDestruction largeDragonDestruction = LargeDragonDestruction.ENABLED;

    // Gets reset once the size reaches the starting size of the dragon species
    // Currently also stores the usages of non-limited items (but doesn't limit them by doing so)
    // (Preferably it would not but the additional checks may not be worth it)
    private final Map<ResourceKey<DragonStage>, Map<Item, Integer>> usedGrowthItems = new HashMap<>();

    public StarHeartItem.State starHeartState = StarHeartItem.State.INACTIVE;
    public boolean isGrowing = true;

    public int magicSource;
    public boolean isOnMagicSource;
    public boolean markedByEnderDragon;
    public boolean flightWasGranted;
    public boolean spinWasGranted;

    public boolean refreshBody;

    /** Last timestamp the server synchronized the player */
    public int lastSync;

    private final Map<ResourceKey<DragonSpecies>, Double> savedSizes = new HashMap<>();
    private SkinData skinData = new SkinData();

    private Holder<DragonSpecies> dragonSpecies;
    private Holder<DragonBody> dragonBody;
    private Holder<DragonStage> dragonStage;

    private int passengerId = DragonRidingHandler.NO_PASSENGER;
    private double size = NO_SIZE;
    private double visualSize = NO_SIZE;
    private double visualSizeLastTick = NO_SIZE;
    private double desiredSize = NO_SIZE;

    private boolean destructionEnabled;

    public Pose previousPose;

    // Needed to calculate collision damage correctly when flying. See ServerFlightHandler.
    public Vec3 preCollisionDeltaMovement = Vec3.ZERO;

    // Needed for some special refreshDimensions calculations (see EntityMixin)
    public boolean refreshedDimensionsFromSizeChange;

    private static final RandomSource RANDOM = RandomSource.create();

    public void setRandomValidStage(@Nullable final Player player) {
        if (dragonSpecies == null) {
            return;
        }

        HolderSet<DragonStage> stages = dragonSpecies.value().getStages(player != null ? player.registryAccess() : null);
        setStage(player, stages.getRandomElement(player != null ? player.getRandom() : RANDOM).orElseThrow());
    }

    /** Sets the stage and retains the current size */
    public void setStage(@Nullable final Player player, final Holder<DragonStage> dragonStage) {
        if (!dragonSpecies.value().getStages(player != null ? player.registryAccess() : null).contains(dragonStage)) {
            //noinspection DataFlowIssue -> key is present
            Functions.logOrThrow("The dragon stage [" + dragonStage.getKey().location() + "] is not valid for the dragon species [" + speciesId() + "]");
            return;
        }

        double boundedSize = dragonStage.value().getBoundedSize(size);

        if (boundedSize == dragonStage.value().sizeRange().max()) {
            // Ties go to the larger stage, so we need to be slightly below the maximum size of the stage
            boundedSize -= Shapes.EPSILON;
        }

        if (this.dragonStage == null) {
            // No reason to slowly adjust to the size in this case
            setDesiredSize(player, boundedSize);
            setSize(player, desiredSize);
        } else {
            setDesiredSize(player, boundedSize);
        }
    }

    /**
     * - Server-side: lerp to the actual size <br>
     * - Client-side: update the visual size
     */
    public void lerpSize(final Player player) {
        if (player.level().isClientSide()) {
            if (visualSize == NO_SIZE) {
                visualSize = size;
            }

            visualSizeLastTick = visualSize;

            if (Math.abs(visualSize - desiredSize) < SIZE_EPSILON) {
                visualSize = desiredSize;
            } else {
                visualSize = Mth.lerp(SIZE_LERP_SPEED, visualSize, desiredSize);
            }
        } else {
            if (Math.abs(size - desiredSize) < SIZE_EPSILON) {
                setSize(player, desiredSize);
                DragonSizeHandler.overridePose(player);
            } else {
                setSize(player, Mth.lerp(SIZE_LERP_SPEED, size, desiredSize));
            }
        }
    }

    public void setSize(@Nullable final Player player, final double size) {
        double oldSize = this.size;
        Holder<DragonStage> oldStage = dragonStage;
        updateSizeAndStage(player != null ? player.registryAccess() : null, size);

        if (player == null) {
            return;
        }

        if (dragonStage == null) {
            DSModifiers.updateSizeModifiers(player, this);
            return;
        }

        if (oldSize == this.size && oldStage != null && dragonStage.is(oldStage)) {
            return;
        }

        // Call updateSizeModifiers before we refreshDimensions, as the size modifiers may affect the dimensions
        DSModifiers.updateSizeModifiers(player, this);

        refreshedDimensionsFromSizeChange = true;
        player.refreshDimensions();
        refreshedDimensionsFromSizeChange = false;

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncSize(serverPlayer.getId(), getSize()));
            MagicData.getData(player).handleAutoUpgrades(serverPlayer, InputData.size((int) this.size));
            DSAdvancementTriggers.BE_DRAGON.get().trigger(serverPlayer);
        }

        if (player.level().isClientSide()) {
            ClientProxy.sendClientData();
        }
    }

    public void setDesiredSize(@Nullable final Player player, double size) {
        if (player == null) {
            desiredSize = size;
            setSize(null, size);
            return;
        }

        desiredSize = boundSize(player.registryAccess(), size);

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncDesiredSize(serverPlayer.getId(), desiredSize));
        }
    }

    private double boundSize(@Nullable final HolderLookup.Provider provider, double size) {
        double newSize = DragonStage.getValidSize(size);

        if (dragonSpecies == null) {
            return newSize;
        }

        Holder<DragonStage> stageToUseForSizeBounds = DragonStage.get(dragonSpecies.value().getStages(provider), newSize);
        newSize = stageToUseForSizeBounds.value().getBoundedSize(newSize);
        return newSize;
    }

    private void updateSizeAndStage(@Nullable final HolderLookup.Provider provider, double size) {
        if (size == NO_SIZE) {
            dragonStage = null;
            this.size = NO_SIZE;
            this.desiredSize = NO_SIZE;
            return;
        }

        dragonStage = dragonSpecies != null ? DragonStage.get(dragonSpecies.value().getStages(provider), size) : null;
        this.size = boundSize(provider, size);

        if (dragonSpecies != null && this.desiredSize == dragonSpecies.value().getStartingSize(provider)) {
            // Allow the player to re-use growth items if their growth is reset
            // Don't clear if the player is a human in case the growth is saved
            // It will be cleared once they turn into a dragon, and its size matches the requirements
            usedGrowthItems.clear();
        }
    }

    public List<Holder<DragonStage>> getStagesSortedByProgression(@Nullable final HolderLookup.Provider provider) {
        List<Holder<DragonStage>> stages = getStages(provider);
        List<Holder<DragonStage>> sortedStages = new ArrayList<>(stages);
        sortedStages.sort(Comparator.comparingDouble(stage -> stage.value().sizeRange().min()));
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
                setBody(player, DragonBody.random(player != null ? player.registryAccess() : null, species));
            }

            if (skinData.skinPresets.get().get(speciesKey()).isEmpty()) {
                refreshSkinPresetForSpecies(speciesKey());
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
                refreshSkinPresetForSpecies(speciesKey());

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
        double partialVisualSize = Mth.lerp(partialTick, visualSizeLastTick, visualSize);

        if (partialVisualSize == visualSize) {
            return player.getAttributeValue(Attributes.SCALE);
        }

        List<AttributeModifier> attributeModifiers = stage().value().filterModifiers(instance);
        List<Modifier> modifiers = stage().value().modifiers().stream().filter(modifier -> modifier.attribute().is(Attributes.SCALE)).toList();

        return Functions.calculateAttributeValue(instance.getBaseValue(), partialVisualSize, attributeModifiers, modifiers);
    }

    public double getSize() {
        return size;
    }

    public double getDesiredSize() {
        return desiredSize;
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

    public void refreshSkinPresetForSpecies(final ResourceKey<DragonSpecies> dragonSpecies) {
        SkinPreset freshSkinPreset = new SkinPreset();
        freshSkinPreset.initDefaults(dragonSpecies, dragonBody != null ? dragonBody.value().model() : DragonBody.DEFAULT_MODEL);
        skinData.skinPresets.get().put(dragonSpecies, freshSkinPreset);
    }

    public SkinPreset getSkinPresetForSpecies(final ResourceKey<DragonSpecies> dragonSpecies) {
        SkinPreset skinPreset = skinData.skinPresets.get().get(dragonSpecies);
        if(skinPreset.isEmpty()) {
            refreshSkinPresetForSpecies(dragonSpecies);
        }
        return skinData.skinPresets.get().get(dragonSpecies);
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
            tag.putDouble(SIZE, size);
            tag.putString(STAR_HEART_STATE, starHeartState.name());
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
            // Only store the size of the dragon the player is currently in if we are saving for the soul
            storeSavedSize(speciesId(), tag);
        } else if (!isDragonSoul) {
            for (ResourceKey<DragonSpecies> type : ResourceHelper.keys(provider, DragonSpecies.REGISTRY)) {
                boolean hasSavedSize = savedSizes.containsKey(type);

                if (hasSavedSize) {
                    storeSavedSize(type.location(), tag);
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
                // This can happen if a dragon body gets removed
                dragonBody = DragonBody.random(provider, dragonSpecies);
            }

            // Makes sure that the set size matches the previously set stage
            setSize(null, tag.getDouble(SIZE));
            desiredSize = size;
            destructionEnabled = tag.getBoolean(DESTRUCTION_ENABLED);
            isGrowing = !tag.contains(IS_GROWING) || tag.getBoolean(IS_GROWING);
            starHeartState = Functions.getEnum(StarHeartItem.State.class, tag.getString(STAR_HEART_STATE));
            markedByEnderDragon = tag.getBoolean(MARKED_BY_ENDER_DRAGON);
            flightWasGranted = tag.getBoolean(WINGS_WAS_GRANTED);
            spinWasGranted = tag.getBoolean(SPIN_WAS_GRANTED);
        }

        if (dragonSpecies != null) {
            if (isDragonSoul) {
                // Only load the size of the dragon the player is currently in if we are loading for the soul
                savedSizes.put(speciesKey(), loadSavedStage(provider, speciesKey(), tag));
            } else {
                for (ResourceKey<DragonSpecies> type : ResourceHelper.keys(provider, DragonSpecies.REGISTRY)) {
                    CompoundTag compound = tag.getCompound(speciesId() + SAVED_SIZE_SUFFIX);

                    if (!compound.isEmpty()) {
                        savedSizes.put(type, loadSavedStage(provider, type, tag));
                    }
                }
            }
        }

        this.usedGrowthItems.clear();

        CompoundTag usedGrowthItems = tag.getCompound(USED_GROWTH_ITEMS);
        usedGrowthItems.getAllKeys().forEach(key -> {
            ResourceLocation resource = ResourceLocation.tryParse(key);;

            if (resource == null) {
                // Just to be safe - would normally not occur
                return;
            }

            CompoundTag perStage = usedGrowthItems.getCompound(key);
            ResourceKey<DragonStage> stageKey = ResourceKey.create(DragonStage.REGISTRY, resource);

            perStage.getAllKeys().forEach(itemKey -> {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(key));

                if (item != Items.AIR) {
                    this.usedGrowthItems.computeIfAbsent(stageKey, ignored -> new HashMap<>()).put(item, perStage.getInt(itemKey));
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
        CompoundTag compound = tag.getCompound(dragonSpecies.location() + SAVED_SIZE_SUFFIX);

        if (compound.isEmpty()) {
            Optional<Holder.Reference<DragonSpecies>> optional = ResourceHelper.get(provider, dragonSpecies);

            if (optional.isPresent()) {
                return optional.get().value().getStartingSize(provider);
            } else {
                DragonSurvival.LOGGER.warn("Cannot load saved size for dragon species [{}] while deserializing NBT of [{}] due to the dragon type not existing. Falling back to the smallest size.", dragonSpecies, tag);
                return DragonStage.getBounds().min();
            }
        }

        return compound.getDouble(SIZE);
    }

    private void storeSavedSize(final ResourceLocation dragonSpecies, final CompoundTag tag) {
        CompoundTag savedSizeTag = new CompoundTag();
        savedSizeTag.putDouble(SIZE, size);
        tag.put(dragonSpecies + SAVED_SIZE_SUFFIX, savedSizeTag);
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        deserializeNBT(provider, tag, false);
    }

    public void revertToHumanForm(final Player player, boolean isDragonSoul) {
        // Don't set the saved dragon size if we are reverting from a soul, as we already are storing the size of the dragon in the soul
        if (ServerConfig.saveGrowthStage && !isDragonSoul && dragonSpecies != null) {
            savedSizes.put(speciesKey(), getSize());
        }

        // Drop everything in your claw slots
        DragonCommand.reInsertClawTools(player);

        setSpecies(player, null);
        setBody(player, null);
        setDesiredSize(player, NO_SIZE);

        AltarData altarData = AltarData.getData(player);
        altarData.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
        altarData.hasUsedAltar = true;
    }

    public double getSavedDragonSize(final ResourceKey<DragonSpecies> type) {
        return savedSizes.getOrDefault(type, NO_SIZE);
    }

    private static final int MAX_SHOWN = 5;

    public Pair<List<Either<FormattedText, TooltipComponent>>, Integer> getGrowthDescription(int currentScroll) {
        DragonStage stage = dragonStage.value();
        double percentage = Math.clamp(stage.getProgress(getSize()), 0, 1);
        String ageInformation = stage.getTimeToGrowFormattedWithPercentage(percentage, getSize(), isGrowing);

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
        components.add(Either.left(Component.translatable(LangKey.GROWTH_SIZE, (int)getSize())));
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
    
    private static final String DRAGON_SPECIES = "dragon_species";
    private static final String DRAGON_BODY = "dragon_body";
    private static final String DRAGON_STAGE = "dragon_stage";

    private static final String ENTITY_STATE = "entity_state";
    private static final String SKIN_DATA = "skin_data";

    private static final String SIZE = "size";
    private static final String SAVED_SIZE_SUFFIX = "_saved_size";

    private static final String MULTI_MINING = "multi_mining";
    private static final String GIANT_DRAGON_DESTRUCTION = "giant_dragon_destruction";

    private static final String STAR_HEART_STATE = "star_heart_state";
    private static final String MARKED_BY_ENDER_DRAGON = "marked_by_ender_dragon";
    private static final String SPIN_WAS_GRANTED = "spin_was_granted";
    private static final String WINGS_WAS_GRANTED = "wings_was_granted";
    private static final String IS_GROWING = "is_growing";
    private static final String DESTRUCTION_ENABLED = "destruction_enabled";
    private static final String USED_GROWTH_ITEMS = "used_growth_items";
}