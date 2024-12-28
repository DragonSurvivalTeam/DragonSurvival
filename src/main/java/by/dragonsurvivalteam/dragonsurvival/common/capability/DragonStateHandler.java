package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.GrowthComponent;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SubCap;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDesiredSize;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncSize;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.*;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonTypes;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class DragonStateHandler extends EntityStateHandler {
    public static final int NO_SIZE = -1;

    @SuppressWarnings("unchecked")
    public final Supplier<SubCap>[] caps = new Supplier[]{this::getSkinData};
    private final Map<ResourceKey<DragonType>, Double> savedSizes = new HashMap<>();

    // --- Other --- //

    public boolean isGrowing = true;
    public StarHeartItem.State starHeartState = StarHeartItem.State.INACTIVE;

    public boolean refreshBody;

    /** Last timestamp the server synchronized the player */
    public int lastSync;
    private final SkinCap skinData = new SkinCap(this);

    private Holder<DragonType> dragonType;
    private Holder<DragonBody> dragonBody;
    private Holder<DragonStage> dragonStage;

    private int passengerId = -1;

    private static final double SIZE_LERP_SPEED = 0.1;
    private double size = NO_SIZE;
    private double visualSize = NO_SIZE;
    private double visualSizeLastTick = NO_SIZE;
    private double desiredSize = NO_SIZE;

    private boolean destructionEnabled;

    // Needed to calculate collision damage correctly when flying. See ServerFlightHandler.
    public Vec3 preCollisionDeltaMovement = Vec3.ZERO;

    /** Sets the stage and retains the current size */
    public void setStage(@Nullable final Player player, final Holder<DragonStage> dragonStage) {
        if (!dragonType.value().getStages(player != null ? player.registryAccess() : null).contains(dragonStage)) {
            //noinspection DataFlowIssue -> key is present
            throw new IllegalArgumentException("The dragon stage [" + dragonStage.getKey().location() + "] is not valid for the dragon type [" + dragonType.getKey().location() + "]");
        }

        setSize(player, dragonStage.value().getBoundedSize(size));
    }

    /** Used serverside to lerp true size values, and clientside to lerp visual size values. */
    public void lerpSize(final Player player) {
        if(player.level().isClientSide) {
            if(visualSize == NO_SIZE) {
                visualSize = size;
            }

            visualSizeLastTick = visualSize;
            visualSize = Mth.lerp(SIZE_LERP_SPEED, visualSize, desiredSize);
        } else {
            double newSize = Mth.lerp(SIZE_LERP_SPEED, size, desiredSize);
            setSize(player, newSize);
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

        MagicData magicData = MagicData.getData(player);
        magicData.handleGrowthAbilityUpgrades(player, this.size);

        player.refreshDimensions();

        if (this.size > oldSize) {
            // Push the player away from a block they might collide with due to the size change
            // Without this they will get stuck on blocks they walk into while their size changes
            // The limit of 0.001 is a random value - it's so that when using growth items the player won't be teleported by x blocks
            double pushForce = (this.size - oldSize) + player.getDeltaMovement().horizontalDistance();
            pushForce *= 0.05;
            Vec3 push = Vec3.ZERO;

            // Multiply the force by the number of collisions. This is because in scenarios where we are colliding with multiple walls (being in a corner)
            // we need more force than normal to push them away. But we don't want this increaseed force when only colliding with one block, because that
            // will cause the player to stutter as they slide against a wall (if they are running towards it)
            int numCollisions = 0;
            for (BlockPos position : BlockPosHelper.betweenClosed(player.getBoundingBox())) {
                if (player.isColliding(position, player.level().getBlockState(position))) {

                    Vec3 center = Vec3.atCenterOf(position);
                    double directionX = player.getX() - center.x();
                    double directionZ = player.getZ() - center.z();

                    numCollisions++;

                    // Need to collect the pushes otherwise running into the corner of two blocks causes issues
                    push = push.add(directionX, 0, directionZ);
                }
            }

            if(push.length() > 0 && pushForce > 0) {
                player.moveTo(player.position().add(push.normalize().scale(pushForce * numCollisions)));
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncSize(serverPlayer.getId(), getSize()));
            DSAdvancementTriggers.BE_DRAGON.get().trigger(serverPlayer);
            DSModifiers.updateSizeModifiers(serverPlayer, this);
        }

        if (player.level().isClientSide()) {
            ClientProxy.sendClientData();
        }
    }

    public void setDesiredSize(@Nullable final Player player, double size) {
        if(player == null) {
            desiredSize = size;
            setSize(null, size);
            return;
        }

        desiredSize = boundSize(player.registryAccess(), size);
        if(player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncDesiredSize(serverPlayer.getId(), desiredSize));
        }
    }

    private double boundSize(@Nullable final HolderLookup.Provider provider, double size) {
        double newSize = DragonStage.getValidSize(size);
        Holder<DragonStage> stageToUseForSizeBounds = DragonStage.getStage(dragonType.value().getStages(provider), newSize);
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

        dragonStage = DragonStage.getStage(dragonType.value().getStages(provider), size);
        this.size = boundSize(provider, size);
    }

    public List<Holder<DragonStage>> getStagesSortedByProgression(@Nullable final HolderLookup.Provider provider) {
        List<Holder<DragonStage>> stages = getStages(provider);
        List<Holder<DragonStage>> sortedStages = new ArrayList<>(stages);
        sortedStages.sort(Comparator.comparingDouble(stage -> stage.value().sizeRange().min()));
        return sortedStages;
    }

    public List<Holder<DragonStage>> getStages(@Nullable final HolderLookup.Provider provider) {
        if(dragonType.value().stages().isPresent()) {
            return dragonType.value().stages().get().stream().toList();
        } else {
            return DragonStage.getDefaultStages(provider).stream().toList();
        }
    }

    // TODO :: use optional for these?
    public Holder<DragonType> getType() {
        return dragonType;
    }

    public Holder<DragonStage> getStage() {
        return dragonStage;
    }

    public Holder<DragonType> getDragonType() {
        return dragonType;
    }

    public Holder<DragonBody> getBody() {
        return dragonBody;
    }

    public void refreshDataOnTypeChange(final Player player) {
        PenaltySupply.getData(player).clear();
        MagicData.getData(player).refresh(getType(), player);
        skinData.skinPreset.initDefaults(this);
    }

    public void setType(final Holder<DragonType> type, final Player player) {
        Holder<DragonType> oldType = dragonType;
        setType(type);

        if (type != null) {
            // TODO :: save abilities per type
            if (oldType == null || !oldType.is(type)) {
                DSModifiers.updateTypeModifiers(player, this);
                refreshDataOnTypeChange(player);
            }
        } else {
            DSModifiers.clearModifiers(player);
        }
    }

    /** Only used for rendering related code - to properly set the type (and update modifiers) use {@link DragonStateHandler#setType(Holder, Player)} */
    public void setType(final Holder<DragonType> type) {
        dragonType = type;
    }

    public void setBody(final Holder<DragonBody> body, Player player) {
        Holder<DragonBody> oldBody = dragonBody;
        setBody(body);

        if (!DragonUtils.isBody(oldBody, dragonBody)) {
            DSModifiers.updateBodyModifiers(player, this);
        }
    }

    /** Only used for rendering (does not update modifiers) */
    public void setBody(final Holder<DragonBody> body) {
        if (body == null) {
            dragonType = null;
            return;
        }

        if (dragonBody == null || !DragonUtils.isBody(body, dragonBody)) {
            dragonBody = body;
            refreshBody = true;
        }
    }

    /** Determines if the current dragon type can harvest the supplied block (with or without tools) (configured harvest bonuses are taken into account) */
    public boolean canHarvestWithPaw(final Player player, final BlockState state) {
        if (isDragon() && ClawInventoryData.getData(player).hasValidClawTool(state)) {
            return true;
        }

        return canHarvestWithPawNoTools(player, state);
    }

    /** Determines if the current dragon type can harvest the supplied block without a tool (configured harvest bonuses are taken into account) */
    public boolean canHarvestWithPawNoTools(final Player player, final BlockState state) {
        if (!isDragon()) {
            return false;
        }

        return getDragonHarvestLevel(player, state) >= ToolUtils.getRequiredHarvestLevel(state);
    }

    public int getDragonHarvestLevel(final Player player, @Nullable final BlockState state) {
        if (!isDragon()) {
            return 0;
        }

        int harvestLevel = 0;

        if (state != null) {
            harvestLevel += player.getExistingData(DSDataAttachments.HARVEST_BONUSES).map(data -> data.get(state)).orElse(0);
        }

        return harvestLevel;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public void setDestructionEnabled(boolean destructionEnabled) {
        this.destructionEnabled = destructionEnabled;
    }

    public double getVisualSize(float partialTick) {
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
        return dragonType != null && dragonBody != null && dragonStage != null;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public SkinCap getSkinData() {
        return skinData;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider, boolean isSavingForSoul) {
        CompoundTag tag = new CompoundTag();
        tag.putString(DRAGON_BODY, dragonBody != null ? Objects.requireNonNull(dragonBody.getKey()).location().toString() : "none");
        tag.putString(DRAGON_STAGE, dragonStage != null ? Objects.requireNonNull(dragonStage.getKey()).location().toString() : "none");
        tag.putString(DRAGON_TYPE, dragonType != null ? Objects.requireNonNull(dragonType.getKey()).location().toString() : "none");

        if (isDragon()) {
            tag.putDouble("size", getSize());
            tag.putBoolean("destructionEnabled", getDestructionEnabled());
            tag.putBoolean(IS_GROWING, isGrowing);
            tag.putInt(STAR_HEART_STATE, starHeartState.ordinal());
        }

        if (isSavingForSoul && getType() != null) {
            // Only store the size of the dragon the player is currently in if we are saving for the soul
            storeSavedSize(getType().getKey(), tag);
        } else if (!isSavingForSoul) {
            for (ResourceKey<DragonType> type : ResourceHelper.keys(provider, DragonType.REGISTRY)) {
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
        String storedDragonType = tag.getString(DRAGON_TYPE);

        if (!storedDragonType.isEmpty()) {
            provider.holder(DragonTypes.key(ResourceLocation.parse(storedDragonType)))
                    .ifPresentOrElse(realDragonType -> this.dragonType = realDragonType,
                            () -> DragonSurvival.LOGGER.warn("Cannot set dragon type [{}] while deserializing NBT of [{}] due to the dragon type not existing", storedDragonType, tag));
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

        if (dragonType != null) {
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

        if(dragonType != null) {
            if (isLoadingForSoul) {
                // Only load the size of the dragon the player is currently in if we are loading for the soul
                //noinspection DataFlowIssue -> key is present
                savedSizes.put(dragonType.getKey(), loadSavedStage(provider, dragonType.getKey(), tag));
            } else if (!isLoadingForSoul) {
                for (ResourceKey<DragonType> type : ResourceHelper.keys(provider, DragonType.REGISTRY)) {
                    CompoundTag compound = tag.getCompound(dragonType.toString() + SAVED_SIZE_SUFFIX);
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
            getSkinData().compileSkin(getStage());
        }
    }

    private double loadSavedStage(@NotNull final HolderLookup.Provider provider, final ResourceKey<DragonType> dragonType, final CompoundTag tag) {
        CompoundTag compound = tag.getCompound(dragonType.toString() + SAVED_SIZE_SUFFIX);

        if (compound.isEmpty()) {
            Optional<Holder.Reference<DragonType>> optional = ResourceHelper.get(provider, dragonType);

            if (optional.isPresent()) {
                return optional.get().value().getStartingSize(provider);
            } else {
                DragonSurvival.LOGGER.warn("Cannot load saved size for dragon type [{}] while deserializing NBT of [{}] due to the dragon type not existing. Falling back to default newborn size.", dragonType, tag);
                return ResourceHelper.get(provider, DragonStages.newborn).orElseThrow().value().sizeRange().min();
            }
        }

        return compound.getDouble(SIZE);
    }

    private void storeSavedSize(final ResourceKey<DragonType> type, final CompoundTag tag) {
        CompoundTag savedSizeTag = new CompoundTag();
        savedSizeTag.putDouble(SIZE, size);

        tag.put(type.toString() + SAVED_SIZE_SUFFIX, savedSizeTag);
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        deserializeNBT(provider, tag, false);
    }

    public void revertToHumanForm(Player player, boolean isRevertingFromSoul) {
        // Don't set the saved dragon size if we are reverting from a soul, as we already are storing the size of the dragon in the soul
        if (ServerConfig.saveGrowthStage && !isRevertingFromSoul) {
            savedSizes.put(dragonType.getKey(), getSize());
        }

        // Drop everything in your claw slots
        DragonCommand.reInsertClawTools(player);

        setType(null);
        setBody(null, player);
        setDesiredSize(player, NO_SIZE);

        AltarData altarData = AltarData.getData(player);
        altarData.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
        altarData.hasUsedAltar = true;
    }

    public double getSavedDragonSize(ResourceKey<DragonType> type) {
        return savedSizes.getOrDefault(type, -1D);
    }

    public String getTypeNameLowerCase() {
        return ResourceHelper.getNameLowercase(dragonType);
    }

    public ResourceLocation getFoodIcons() {
        return dragonType.value().miscResources().foodSprites();
    }

    private static final int MAX_SHOWN = 5;

    public Pair<List<Either<FormattedText, TooltipComponent>>, Integer> getGrowthDescription(int currentScroll) {
        DragonStage stage = dragonStage.value();
        double percentage = Math.clamp(stage.getProgress(getSize()), 0, 1);
        String ageInformation = stage.getTimeToGrowFormattedWithPercentage(percentage, getSize(), isGrowing);

        List<GrowthComponent> growthItems = new ArrayList<>();

        getStage().value().growthItems().forEach(growthItem -> {
            // A bit of wasted processing since not all are shown
            growthItem.items().forEach(item -> growthItems.add(new GrowthComponent(item.value(), growthItem.growthInTicks())));
        });

        int scroll = currentScroll;
        if (growthItems.size() <= MAX_SHOWN) {
            scroll = 0;
        } else {
            scroll = Math.clamp(scroll, 0, growthItems.size() - MAX_SHOWN);
        }

        int max = Math.min(growthItems.size(), scroll + MAX_SHOWN);

        List<Either<FormattedText, TooltipComponent>> components = new ArrayList<>();
        components.add(Either.left(Component.translatable(LangKey.GROWTH_STAGE).append(DragonStage.translatableName(Objects.requireNonNull(getStage().getKey())))));
        components.add(Either.left(Component.translatable(LangKey.GROWTH_AGE, ageInformation)));
        components.add(Either.left(Component.translatable(LangKey.GROWTH_INFO).append(Component.literal(" [" + Math.min(growthItems.size(), scroll + MAX_SHOWN) + " / " + growthItems.size() + "]").withStyle(ChatFormatting.DARK_GRAY))));

        for (int i = scroll; i < max; i++) {
            components.add(Either.right(growthItems.get(i)));
        }

        return Pair.of(components, scroll);
    }

    public static final String DRAGON_TYPE = "dragon_type";
    public static final String DRAGON_BODY = "dragon_body";
    public static final String DRAGON_STAGE = "dragon_stage";
    public static final String ENTITY_STATE = "entity_state";

    public static final String SIZE = "size";
    public static final String SAVED_SIZE_SUFFIX = "_saved_size";

    public static final String STAR_HEART_STATE = "star_heart_state";
    public static final String IS_GROWING = "is_growing";
}