package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.TimeComponent;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SubCap;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.mixins.EntityAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDesiredSize;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncSize;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.*;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.InputData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.*;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class DragonStateHandler extends EntityStateHandler {
    public static final double NO_SIZE = -1;
    public static final double SIZE_LERP_SPEED = 0.1; // 10% per tick
    private static final double SIZE_EPSILON = 0.001;

    @SuppressWarnings("unchecked")
    public final Supplier<SubCap>[] caps = new Supplier[]{this::getSkinData};
    private final Map<ResourceKey<DragonSpecies>, Double> savedSizes = new HashMap<>();

    public StarHeartItem.State starHeartState = StarHeartItem.State.INACTIVE;
    public boolean isGrowing = true;

    public int magicSource;
    public boolean isOnMagicSource;

    public boolean refreshBody;

    /** Last timestamp the server synchronized the player */
    public int lastSync;
    private final SkinCap skinData = new SkinCap(this);

    private Holder<DragonSpecies> dragonSpecies;
    private Holder<DragonBody> dragonBody;
    private Holder<DragonStage> dragonStage;

    private int passengerId = -1;
    private double size = NO_SIZE;
    private double visualSize = NO_SIZE;
    private double visualSizeLastTick = NO_SIZE;
    private double desiredSize = NO_SIZE;

    private boolean destructionEnabled;

    // Needed to calculate collision damage correctly when flying. See ServerFlightHandler.
    public Vec3 preCollisionDeltaMovement = Vec3.ZERO;

    /** Sets the stage and retains the current size */
    public void setStage(@Nullable final Player player, final Holder<DragonStage> dragonStage) {
        if (!dragonSpecies.value().getStages(player != null ? player.registryAccess() : null).contains(dragonStage)) {
            //noinspection DataFlowIssue -> key is present
            Functions.logOrThrow("The dragon stage [" + dragonStage.getKey().location() + "] is not valid for the dragon species [" + dragonSpecies.getKey().location() + "]");
            return;
        }

        double boundedSize = dragonStage.value().getBoundedSize(size);
        if(boundedSize == dragonStage.value().sizeRange().min()) {
            boundedSize += 0.0001f; // Ties go to the lower stage, so we need to be slightly above the minimum size
        }
        setDesiredSize(player, boundedSize);
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

        player.refreshDimensions();
        double sizeDifference = this.size - oldSize;

        if (sizeDifference > 0) {
            // Push the player away from a block they might collide with due to the size change (to avoid getting stuck on said block)
            double pushForce = sizeDifference * 0.03;
            Vec3 push = Vec3.ZERO;

            // We don't want to accumulate pushes in the same direction from different blocks
            // Therefor we track the positions the player will be pushed to
            boolean negativeXPushed = false;
            boolean negativeZPushed = false;
            boolean positiveXPushed = false;
            boolean positiveZPushed = false;

            for (BlockPos position : BlockPosHelper.betweenClosed(player.getBoundingBox())) {
                if (player.isColliding(position, player.level().getBlockState(position))) {
                    // Calculate the nearest face of the block to the player
                    Vec3 center = Vec3.atCenterOf(position);
                    double directionX = player.getX() - center.x();
                    double directionZ = player.getZ() - center.z();

                    Vec3 nearestFace;

                    if (Math.abs(directionX) > Math.abs(directionZ)) {
                        nearestFace = new Vec3(Math.signum(directionX), 0, 0);
                    } else {
                        nearestFace = new Vec3(0, 0, Math.signum(directionZ));
                    }

                    // Determine the push direction
                    if (!negativeXPushed && nearestFace.x() < 0) {
                        push = push.add(nearestFace);
                        negativeXPushed = true;
                    } else if (!positiveXPushed && nearestFace.x() > 0) {
                        push = push.add(nearestFace);
                        positiveXPushed = true;
                    } else if (!negativeZPushed && nearestFace.z() < 0) {
                        push = push.add(nearestFace);
                        negativeZPushed = true;
                    } else if (!positiveZPushed && nearestFace.z() > 0) {
                        push = push.add(nearestFace);
                        positiveZPushed = true;
                    }
                }

                if (negativeXPushed && positiveXPushed && negativeZPushed && positiveZPushed) {
                    break;
                }
            }

            if (pushForce > 0 && push.length() > 0) {
                player.moveTo(player.position().add(push.normalize().scale(pushForce)));
            }
        }

        ((EntityAccessor) player).dragonSurvival$reapplyPosition();

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncSize(serverPlayer.getId(), getSize()));
            MagicData.getData(player).handleAutoUpgrades(serverPlayer, InputData.size((int) this.size));
            DSAdvancementTriggers.BE_DRAGON.get().trigger(serverPlayer);
            DSModifiers.updateSizeModifiers(serverPlayer, this);
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

        Holder<DragonStage> stageToUseForSizeBounds = DragonStage.getStage(dragonSpecies.value().getStages(provider), newSize);
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

        dragonStage = dragonSpecies != null ? DragonStage.getStage(dragonSpecies.value().getStages(provider), size) : null;
        this.size = boundSize(provider, size);
    }

    public List<Holder<DragonStage>> getStagesSortedByProgression(@Nullable final HolderLookup.Provider provider) {
        List<Holder<DragonStage>> stages = getStages(provider);
        List<Holder<DragonStage>> sortedStages = new ArrayList<>(stages);
        sortedStages.sort(Comparator.comparingDouble(stage -> stage.value().sizeRange().min()));
        return sortedStages;
    }

    public List<Holder<DragonStage>> getStages(@Nullable final HolderLookup.Provider provider) {
        if(dragonSpecies.value().stages().isPresent()) {
            return dragonSpecies.value().stages().get().stream().toList();
        } else {
            return DragonStage.getDefaultStages(provider).stream().toList();
        }
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

    public void refreshDataOnTypeChange(final ServerPlayer player) {
        PenaltySupply.getData(player).clear();
        MagicData magic = MagicData.getData(player);

        if (!ServerConfig.saveAllAbilities) {
            magic.refresh(player, species());
        } else {
            if (magic.dataForSpeciesIsEmpty(speciesKey())) {
                magic.refresh(player, species());
            } else {
                magic.setCurrentSpecies(speciesKey());
            }
        }

        PacketDistributor.sendToPlayer(player, new SyncMagicData(magic.serializeNBT(player.registryAccess())));
    }

    public void setType(@Nullable final Player player, final Holder<DragonSpecies> species) {
        Holder<DragonSpecies> oldSpecies = dragonSpecies;
        dragonSpecies = species;

        if (!(player instanceof ServerPlayer serverPlayer)) {
            // Magic data and attribute modifiers are handled server-side
            return;
        }

        if (species != null && (oldSpecies == null || !oldSpecies.is(species))) {
            DSModifiers.updateTypeModifiers(serverPlayer, this);
            refreshDataOnTypeChange(serverPlayer);
        } else if (species == null) {
            DSModifiers.clearModifiers(serverPlayer);
        }
    }

    public void setBody(@Nullable final Player player, final Holder<DragonBody> dragonBody) {
        Holder<DragonBody> oldBody = this.dragonBody;
        this.dragonBody = dragonBody;
        boolean isSameBody = DragonUtils.isBody(oldBody, this.dragonBody);

        if (this.dragonBody != null && !isSameBody) {
            refreshBody = true;

            // If the model has changed, just override the skin preset with the default one as a failsafe
            if (oldBody != null && this.dragonBody.value().customModel() != oldBody.value().customModel()) {
                SkinPreset freshPreset = new SkinPreset();
                freshPreset.initDefaults(dragonSpecies.getKey(), this.dragonBody.value().customModel());
                skinData.skinPresets.get().put(speciesKey(), freshPreset);
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
            return HarvestBonuses.canHarvest(player, state, true);
        }

        if (ClawInventoryData.getData(player).hasValidClawTool(state)) {
            return true;
        }

        return HarvestBonuses.canHarvest(player, state, false);
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public void setDestructionEnabled(boolean destructionEnabled) {
        this.destructionEnabled = destructionEnabled;
    }

    public double getVisualSize(float partialTick) {
        if (!DragonSurvival.PROXY.isOnRenderThread()) {
            Functions.logOrThrow("Visual size should only be retrieved for rendering-purposes");
            return size;
        }

        return Mth.lerp(partialTick, visualSizeLastTick, visualSize);
    }

    public double getSize() {
        return size;
    }

    public double getDesiredSize() {
        return desiredSize;
    }

    public boolean getDestructionEnabled() {
        return destructionEnabled;
    }

    public boolean isDragon() {
        return dragonSpecies != null && dragonBody != null && dragonStage != null;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public SkinCap getSkinData() {
        return skinData;
    }

    public ResourceLocation getCurrentCustomModel() {
        return dragonBody.value().customModel();
    }

    public String getCustomModelName() {
        String customModelPath = getCurrentCustomModel().getPath();
        return customModelPath.substring(customModelPath.lastIndexOf("/") + 1, customModelPath.indexOf("."));
    }

    public void initializeSkinDataToDefaults() {
        skinData.skinPresets.get().get(dragonSpecies.getKey()).initDefaults(dragonSpecies.getKey(), dragonBody.value().customModel());
    }

    public void setSkinPresetForType(ResourceKey<DragonSpecies> dragonSpecies, SkinPreset preset) {
        skinData.skinPresets.get().put(dragonSpecies, preset);
    }

    public void setCurrentSkinPreset(SkinPreset preset) {
        skinData.skinPresets.get().put(dragonSpecies.getKey(), preset);
        recompileCurrentSkin();
    }

    public SkinPreset getCurrentSkinPreset() {
        return skinData.skinPresets.get().get(dragonSpecies.getKey());
    }

    public SkinPreset getSkinPresetForSpecies(ResourceKey<DragonSpecies> dragonSpecies) {
        return skinData.skinPresets.get().get(dragonSpecies);
    }

    public void recompileCurrentSkin() {
        skinData.compileSkin(dragonStage);
    }

    public void setCurrentStageCustomization(DragonStageCustomization customization) {
        skinData.skinPresets.get().get(dragonSpecies.getKey()).put(dragonStage.getKey(), Lazy.of(() -> customization));
    }

    public DragonStageCustomization getCurrentStageCustomization() {
        return skinData.get(dragonSpecies.getKey(), dragonStage.getKey()).get();
    }

    public DragonStageCustomization getCustomizationForStage(ResourceKey<DragonStage> stage) {
        return skinData.get(dragonSpecies.getKey(), stage).get();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider, boolean isSavingForSoul) {
        CompoundTag tag = new CompoundTag();
        tag.putString(DRAGON_BODY, dragonBody != null ? Objects.requireNonNull(dragonBody.getKey()).location().toString() : "none");
        tag.putString(DRAGON_STAGE, dragonStage != null ? Objects.requireNonNull(dragonStage.getKey()).location().toString() : "none");
        tag.putString(DRAGON_SPECIES, dragonSpecies != null ? Objects.requireNonNull(dragonSpecies.getKey()).location().toString() : "none");

        if (isDragon()) {
            tag.putDouble("size", getSize());
            tag.putBoolean("destructionEnabled", getDestructionEnabled());
            tag.putBoolean(IS_GROWING, isGrowing);
            tag.putInt(STAR_HEART_STATE, starHeartState.ordinal());
        }

        if (isSavingForSoul && species() != null) {
            // Only store the size of the dragon the player is currently in if we are saving for the soul
            storeSavedSize(speciesKey(), tag);
        } else if (!isSavingForSoul) {
            for (ResourceKey<DragonSpecies> type : ResourceHelper.keys(provider, DragonSpecies.REGISTRY)) {
                boolean hasSavedSize = savedSizes.containsKey(type);
                if(hasSavedSize) {
                    storeSavedSize(type, tag);
                }
            }
        }

        for (int i = 0; i < caps.length; i++) {
            tag.put("cap_" + i, caps[i].get().serializeNBT(provider));
        }

        tag.put(ENTITY_STATE, super.serializeNBT(provider));

        return tag;
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        return serializeNBT(provider, false);
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag, boolean isLoadingForSoul) { // TODO :: make a different method for soul
        String storedDragonSpecies = tag.getString(DRAGON_SPECIES);

        if (!storedDragonSpecies.isEmpty()) {
            provider.holder(BuiltInDragonSpecies.key(ResourceLocation.parse(storedDragonSpecies)))
                    .ifPresentOrElse(realDragonSpecies -> this.dragonSpecies = realDragonSpecies,
                            () -> DragonSurvival.LOGGER.warn("Cannot set dragon species [{}] while deserializing NBT of [{}] due to the dragon type not existing", storedDragonSpecies, tag));
        }


        String storedDragonBody = tag.getString(DRAGON_BODY);

        if (!storedDragonBody.isEmpty()) {
            provider.holder(DragonBodies.key(ResourceLocation.parse(storedDragonBody)))
                    .ifPresentOrElse(dragonBody -> this.dragonBody = dragonBody,
                            () -> DragonSurvival.LOGGER.warn("Cannot set dragon body [{}] while deserializing NBT of [{}] due to the dragon body not existing", storedDragonBody, tag));
        }

        String storedDragonStage = tag.getString(DRAGON_STAGE);

        if (!storedDragonStage.isEmpty()) {
            provider.holder(DragonStages.key(ResourceLocation.parse(storedDragonStage)))
                    .ifPresentOrElse(dragonStage -> this.dragonStage = dragonStage,
                            () -> DragonSurvival.LOGGER.warn("Cannot set dragon stage [{}] while deserializing NBT of [{}] due to the dragon stage not existing", dragonStage, tag));
        }

        if (dragonSpecies != null) {
            if (dragonBody == null) {
                // This can happen if a dragon body gets removed
                dragonBody = DragonBody.random(provider);
            }

            // Makes sure that the set size matches the previously set stage
            setSize(null, tag.getDouble(SIZE));
            desiredSize = size;
            setDestructionEnabled(tag.getBoolean("destructionEnabled"));
            isGrowing = !tag.contains(IS_GROWING) || tag.getBoolean(IS_GROWING);
            starHeartState = StarHeartItem.State.values()[tag.getInt(STAR_HEART_STATE)];
        }

        if (dragonSpecies != null) {
            if (isLoadingForSoul) {
                // Only load the size of the dragon the player is currently in if we are loading for the soul
                //noinspection DataFlowIssue -> key is present
                savedSizes.put(dragonSpecies.getKey(), loadSavedStage(provider, dragonSpecies.getKey(), tag));
            } else {
                for (ResourceKey<DragonSpecies> type : ResourceHelper.keys(provider, DragonSpecies.REGISTRY)) {
                    CompoundTag compound = tag.getCompound(dragonSpecies.toString() + SAVED_SIZE_SUFFIX);
                    if (!compound.isEmpty()) {
                        savedSizes.put(type, loadSavedStage(provider, type, tag));
                    }
                }
            }
        }

        for (int i = 0; i < caps.length; i++) {
            if (tag.contains("cap_" + i)) {
                caps[i].get().deserializeNBT(provider, (CompoundTag) tag.get("cap_" + i));
            }
        }

        super.deserializeNBT(provider, tag.getCompound(ENTITY_STATE));

        if (isDragon()) {
            refreshBody = true;
            getSkinData().compileSkin(stage());
        }
    }

    private double loadSavedStage(@NotNull final HolderLookup.Provider provider, final ResourceKey<DragonSpecies> dragonSpecies, final CompoundTag tag) {
        CompoundTag compound = tag.getCompound(dragonSpecies.toString() + SAVED_SIZE_SUFFIX);

        if (compound.isEmpty()) {
            Optional<Holder.Reference<DragonSpecies>> optional = ResourceHelper.get(provider, dragonSpecies);

            if (optional.isPresent()) {
                return optional.get().value().getStartingSize(provider);
            } else {
                DragonSurvival.LOGGER.warn("Cannot load saved size for dragon species [{}] while deserializing NBT of [{}] due to the dragon type not existing. Falling back to default newborn size.", dragonSpecies, tag);
                return ResourceHelper.get(provider, DragonStages.newborn).orElseThrow().value().sizeRange().min();
            }
        }

        return compound.getDouble(SIZE);
    }

    private void storeSavedSize(final ResourceKey<DragonSpecies> type, final CompoundTag tag) {
        CompoundTag savedSizeTag = new CompoundTag();
        savedSizeTag.putDouble(SIZE, size);

        tag.put(type.toString() + SAVED_SIZE_SUFFIX, savedSizeTag);
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        deserializeNBT(provider, tag, false);
    }

    public void revertToHumanForm(final Player player, boolean isRevertingFromSoul) {
        // Don't set the saved dragon size if we are reverting from a soul, as we already are storing the size of the dragon in the soul
        if (ServerConfig.saveGrowthStage && !isRevertingFromSoul && dragonSpecies != null) {
            savedSizes.put(dragonSpecies.getKey(), getSize());
        }

        // Drop everything in your claw slots
        DragonCommand.reInsertClawTools(player);

        setType(player, null);
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
            growthItem.items().forEach(item -> growthItems.add(new TimeComponent(item.value(), growthItem.growthInTicks())));
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

    public static final String DRAGON_SPECIES = "dragon_species";
    public static final String DRAGON_BODY = "dragon_body";
    public static final String DRAGON_STAGE = "dragon_stage";
    public static final String ENTITY_STATE = "entity_state";

    public static final String SIZE = "size";
    public static final String SAVED_SIZE_SUFFIX = "_saved_size";

    public static final String STAR_HEART_STATE = "star_heart_state";
    public static final String IS_GROWING = "is_growing";
}