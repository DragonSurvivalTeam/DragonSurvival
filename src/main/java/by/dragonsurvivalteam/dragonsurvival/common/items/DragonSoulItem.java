package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DragonSoulData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.PlayerLoginHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class DragonSoulItem extends BlockItem {
    @ConfigRange(min = 0)
    @Translation(key = "dragon_soul_cooldown", type = Translation.Type.CONFIGURATION, comments = "Cooldown (in ticks) (20 ticks = 1 second) that occurs after using the dragon soul")
    @ConfigOption(side = ConfigSide.SERVER, category = {"items", "dragon_soul"}, key = "dragon_soul_cooldown")
    public static int COOLDOWN = Functions.secondsToTicks(60);

    @Translation(key = "enable_dragon_soul_placement", type = Translation.Type.CONFIGURATION, comments = "Enables the placement of dragon souls")
    @ConfigOption(side = ConfigSide.SERVER, category = {"items", "dragon_soul"}, key = "enable_dragon_soul_placement")
    public static boolean ENABLE_DRAGON_SOUL_PLACEMENT = true;

    @Translation(comments = "Empty Dragon Soul")
    private static final String EMPTY_DRAGON_SOUL = Translation.Type.ITEM.wrap("empty_dragon_soul");

    @Translation(comments = " Soul")
    private static final String SOUL = Translation.Type.DESCRIPTION.wrap("dragon_soul.soul");

    @Translation(comments = {
            "■§7 This vessel holds the dragon's soul. Use it to become a dragon. Replaces your current stats if you are a dragon.§r",
            "■§7 You can place it as a block while crouching (a behaviour you can disable by pressing %s§7)\n"
    })
    private static final String DESCRIPTION = Translation.Type.DESCRIPTION.wrap("dragon_soul");

    @Translation(comments = "§6■ Species:§r %s\n§6■ Growth Stage:§r %s\n§6■ Growth:§r %s\n")
    private static final String INFO = Translation.Type.DESCRIPTION.wrap("dragon_soul.info");

    @Translation(comments = "■§7 An empty dragon's soul. With this item, you can store all your dragon's characteristics. After using it, you become human.")
    private static final String IS_EMPTY = Translation.Type.DESCRIPTION.wrap("dragon_soul.empty");

    @Translation(comments = "Invalid dragon type")
    private static final String INVALID_DRAGON_TYPE = Translation.Type.DESCRIPTION.wrap("dragon_soul.invalid_type");

    public DragonSoulItem(final Properties properties) {
        super(DSBlocks.DRAGON_SOUL.get(), properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull final Level level, @NotNull final Player player, @NotNull final InteractionHand hand) {
        if (!player.hasEffect(DSEffects.EXHAUSTED_SOUL) && (DragonStateProvider.isDragon(player) || player.getItemInHand(hand).has(DSDataComponents.DRAGON_SOUL))) {
            player.startUsingItem(hand);
            // FIXME
            //return InteractionResultHolder.success(player.getItemInHand(hand));
            return InteractionResult.SUCCESS;
        } else {
            // FIXME
            // return InteractionResultHolder.fail(player.getItemInHand(hand));
            return InteractionResult.FAIL;
        }
    }

    @Override
    public @NotNull InteractionResult place(@NotNull final BlockPlaceContext context) {
        if (!ENABLE_DRAGON_SOUL_PLACEMENT) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();

        if (player != null && (!player.isCrouching() || !player.getData(DSDataAttachments.PLAYER_DATA).enabledDragonSoulPlacement)) {
            return InteractionResult.PASS;
        }

        DragonSoulData data = context.getItemInHand().get(DSDataComponents.DRAGON_SOUL);

        if (data == null) {
            return InteractionResult.PASS;
        }

        return super.place(context);
    }

    /** '0' means there is no data and '1' means it contains a soul */
    private static int getCustomModelData(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        ResourceKey<DragonSpecies> species = ResourceHelper.decodeKey(provider, DragonSpecies.REGISTRY, tag, DragonStateHandler.DRAGON_SPECIES);

        if (species == null) {
            return 0;
        }

        return 1;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull final ItemStack stack, @NotNull final Level level, @NotNull final LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return stack;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            DSAdvancementTriggers.USE_DRAGON_SOUL.get().trigger(serverPlayer);
        }

        DragonSoulData data = stack.get(DSDataComponents.DRAGON_SOUL);

        DragonStateHandler handler = DragonStateProvider.getData(player);
        MagicData magicData = MagicData.getData(player);

        // Make sure dragon data is present, in case other mods add custom data to items
        if (data != null && !data.dragonData().isEmpty()) {
            if (handler.isDragon()) {
                // Swap the player's dragon data with the item's NBT
                TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
                handler.serialize(valueOutput, true);
                CompoundTag currentDragonData = valueOutput.buildResult();

                // Preserve spin/flight grant state
                boolean flightGranted = handler.flightWasGranted;
                boolean spinGranted = handler.spinWasGranted;
                ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data.dragonData());
                handler.deserialize(valueInput, true);
                handler.flightWasGranted = flightGranted;
                handler.spinWasGranted = spinGranted;

                TagValueOutput valueOutputAbility = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
                magicData.serializeForCurrentSpecies(valueOutputAbility);
                CompoundTag currentAbilityData = valueOutputAbility.buildResult();

                magicData.setCurrentSpecies(player, handler.speciesKey());
                ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data.abilityData());
                magicData.deserializeForCurrentSpecies(input);

                PenaltySupply.clear(player);

                stack.set(DSDataComponents.DRAGON_SOUL, new DragonSoulData(currentDragonData, currentAbilityData, player.getScale()));
                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), List.of(getCustomModelData(level.registryAccess(), currentDragonData))));
            } else {
                // Preserve spin/flight grant state
                boolean flightGranted = handler.flightWasGranted;
                boolean spinGranted = handler.spinWasGranted;
                ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data.dragonData());
                handler.deserialize(valueInput, true);
                handler.flightWasGranted = flightGranted;
                handler.spinWasGranted = spinGranted;

                magicData.setCurrentSpecies(player, handler.speciesKey());
                ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data.abilityData());
                magicData.deserializeForCurrentSpecies(input);

                stack.remove(DSDataComponents.DRAGON_SOUL);
                stack.remove(DataComponents.CUSTOM_MODEL_DATA);
            }
        } else if (handler.isDragon()) {
            TagValueOutput valueOutputDragon = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            handler.serialize(valueOutputDragon, true);
            CompoundTag currentDragonData = valueOutputDragon.buildResult();
            TagValueOutput valueOutputAbility = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            magicData.serializeForCurrentSpecies(valueOutputAbility);
            CompoundTag currentAbilityData = valueOutputAbility.buildResult();

            stack.set(DSDataComponents.DRAGON_SOUL, new DragonSoulData(currentDragonData, currentAbilityData, player.getScale()));
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), List.of(getCustomModelData(level.registryAccess(), currentDragonData))));
            handler.revertToHumanForm(player, true);
        }

        if (!player.isCreative()) {
            player.addEffect(new MobEffectInstance(DSEffects.EXHAUSTED_SOUL, COOLDOWN, 0, false, true, true));
        }

        if (player instanceof ServerPlayer serverPlayer) {
            SyncComplete.handleDragonSync(serverPlayer, false);
            PlayerLoginHandler.syncHandler(serverPlayer);
        }

        if (handler.isDragon()) {
            handler.setGrowth(player, handler.getGrowth(), true);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
            magicData.serialize(valueOutput);
            PacketDistributor.sendToPlayer(serverPlayer, new SyncMagicData(valueOutput.buildResult()));
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDER_DRAGON_GROWL, entity.getSoundSource(), 0.6f, 1);

        for (int i = 0; i < 10; i++) {
            level.addParticle(ParticleTypes.SOUL, entity.getX() + (level.random.nextDouble() - 0.5D) * 0.5D, entity.getY() + (level.random.nextDouble() - 0.5D) * 0.5D, entity.getZ() + (level.random.nextDouble() - 0.5D) * 0.5D, (level.random.nextDouble() - 0.5D) * 0.5D, level.random.nextDouble() * 0.5D, (level.random.nextDouble() - 0.5D) * 0.5D);
        }

        return stack;
    }

    /** See {@link by.dragonsurvivalteam.dragonsurvival.client.extensions.ShakeWhenUsedExtension} */
    @Override
    // FIXME :: ItemUseAnimation.CUSTOM is gone?
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull final ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(@NotNull final ItemStack stack, @NotNull final LivingEntity entity) {
        return Functions.secondsToTicks(2);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        HolderLookup.Provider provider = context.registries();

        if (provider == null) {
            return;
        }

        if (stack.has(DSDataComponents.DRAGON_SOUL)) {
            CompoundTag handlerData = getHandlerData(stack);
            tooltipAdder.accept(Component.translatable(DESCRIPTION, DSColors.dynamicValue(DragonSurvival.PROXY.getDragonSoulPlacementKeybind())));

            ResourceKey<DragonSpecies> species = ResourceHelper.decodeKey(provider, DragonSpecies.REGISTRY, handlerData, DragonStateHandler.DRAGON_SPECIES);
            Component name;

            if (species != null) {
                name = Component.translatable(Translation.Type.DRAGON_SPECIES.wrap(species.identifier()));
            } else {
                name = Component.translatable(INVALID_DRAGON_TYPE);
            }

            double growth = handlerData.getDouble(DragonStateHandler.GROWTH).orElseThrow();
            Holder<DragonStage> stage = DragonStage.get(provider, growth);

            //noinspection DataFlowIssue -> key is present
            tooltipAdder.accept(Component.translatable(INFO, name, DragonStage.translatableName(stage.getKey()), String.format("%.0f", growth)));
        } else {
            tooltipAdder.accept(Component.translatable(IS_EMPTY));
        }
    }

    @Override
    public void onUseTick(@NotNull final Level level, @NotNull final LivingEntity entity, @NotNull final ItemStack soul, int remainingUseDuration) {
        super.onUseTick(level, entity, soul, remainingUseDuration);
        entity.playSound(SoundEvents.SOUL_ESCAPE.value(), (float) (0.2 + 0.15 * entity.getRandom().nextInt(2)), entity.getRandom().nextFloat() - entity.getRandom().nextFloat() * 0.2f + 1.0f);
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return stack.has(DSDataComponents.DRAGON_SOUL);
    }

    @Override
    public @NotNull Component getName(@NotNull final ItemStack stack) {
        ResourceKey<DragonSpecies> species = getSpecies(stack, null);

        if (species == null) {
            return Component.translatable(EMPTY_DRAGON_SOUL);
        }

        return Component.translatable(Translation.Type.DRAGON_SPECIES.wrap(species.identifier())).append(Component.translatable(SOUL));
    }

    public CompoundTag getHandlerData(final ItemStack stack) {
        return stack.getOrDefault(DSDataComponents.DRAGON_SOUL, DragonSoulData.EMPTY).dragonData();
    }

    public @Nullable ResourceKey<DragonSpecies> getSpecies(final ItemStack stack, final HolderLookup.Provider provider) {
        return ResourceHelper.decodeKey(provider, DragonSpecies.REGISTRY, getHandlerData(stack), DragonStateHandler.DRAGON_SPECIES);
    }

    public @Nullable ResourceKey<DragonStage> getStage(final ItemStack stack, final HolderLookup.Provider provider) {
        return ResourceHelper.decodeKey(provider, DragonStage.REGISTRY, getHandlerData(stack), DragonStateHandler.DRAGON_STAGE);
    }
}
