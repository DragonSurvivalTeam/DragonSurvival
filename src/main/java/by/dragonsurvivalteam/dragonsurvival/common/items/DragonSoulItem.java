package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonSpeciesTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.PlayerLoginHandler;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonSoulItem extends Item {
    @Translation(comments = "Empty Dragon Soul")
    private static final String EMPTY_DRAGON_SOUL = Translation.Type.ITEM.wrap("empty_dragon_soul");

    @Translation(comments = " Soul")
    private static final String SOUL = Translation.Type.DESCRIPTION.wrap("dragon_soul.soul");

    @Translation(comments = "■§7 This vessel holds the dragon's soul. Use it to become a dragon. Replaces your current stats if you are a dragon.\n")
    private static final String DESCRIPTION = Translation.Type.DESCRIPTION.wrap("dragon_soul");

    @Translation(comments = "§6■ Species:§r %s\n§6■ Growth Stage:§r %s\n§6■ Growth:§r %s\n")
    private static final String INFO = Translation.Type.DESCRIPTION.wrap("dragon_soul.info");

    @Translation(comments = "■§7 An empty dragon's soul. With this item, you can store all your dragon's characteristics. After using it, you become human.")
    private static final String IS_EMPTY = Translation.Type.DESCRIPTION.wrap("dragon_soul.empty");

    @Translation(comments = "Invalid dragon type")
    private static final String INVALID_DRAGON_TYPE = Translation.Type.DESCRIPTION.wrap("dragon_soul.invalid_type");

    @ConfigRange(min = 0)
    @Translation(key = "dragon_soul_cooldown", type = Translation.Type.CONFIGURATION, comments = "Cooldown (in ticks) (20 ticks = 1 second) that occurs after using the dragon soul")
    @ConfigOption(side = ConfigSide.SERVER, category = {"items", "dragon_soul"}, key = "dragon_soul_cooldown")
    public static int COOLDOWN = Functions.secondsToTicks(60);

    public DragonSoulItem(final Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull final Level level, @NotNull final Player player, @NotNull final InteractionHand hand) {
        if (DragonStateProvider.isDragon(player) || player.getItemInHand(hand).has(DataComponents.CUSTOM_DATA)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        } else {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
    }

    // TODO :: make compatible with custom species
    private static int getCustomModelData(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        ResourceKey<DragonSpecies> species = ResourceHelper.decodeKey(provider, DragonSpecies.REGISTRY, tag, DragonStateHandler.DRAGON_SPECIES);

        if (species == null) {
            return 0;
        }

        Holder<DragonSpecies> dragonSpecies = provider.holderOrThrow(species);

        if (dragonSpecies.is(DSDragonSpeciesTags.FOREST_DRAGONS)) {
            return 1;
        } else if (dragonSpecies.is(DSDragonSpeciesTags.CAVE_DRAGONS)) {
            return 2;
        } else if (dragonSpecies.is(DSDragonSpeciesTags.SEA_DRAGONS)) {
            return 3;
        }

        return 0;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull final ItemStack stack, @NotNull final Level level, @NotNull final LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return stack;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            DSAdvancementTriggers.USE_DRAGON_SOUL.get().trigger(serverPlayer);
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        MagicData magicData  = MagicData.getData(player);

        if (player instanceof ServerPlayer serverPlayer) {
            if (stack.has(DataComponents.CUSTOM_DATA)) {
                if (handler.isDragon()) {
                    CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();

                    // Swap the player's dragon data with the item's NBT
                    CompoundTag storedDragonData = tag.getCompound(DRAGON);
                    CompoundTag currentDragonData = handler.serializeNBT(level.registryAccess(), true);
                    // Preserve spin/flight grant state
                    boolean flightGranted = handler.flightWasGranted;
                    boolean spinGranted = handler.spinWasGranted;
                    handler.deserializeNBT(level.registryAccess(), storedDragonData, true);
                    handler.flightWasGranted = flightGranted;
                    handler.spinWasGranted = spinGranted;

                    CompoundTag storedAbilityData = tag.getCompound(ABILITIES);
                    CompoundTag currentAbilityData = magicData.serializeNBTForCurrentSpecies(level.registryAccess());

                    handler.refreshMagicData(serverPlayer, true);
                    magicData.deserializeNBTForCurrentSpecies(level.registryAccess(), storedAbilityData);

                    CompoundTag combinedData = new CompoundTag();
                    combinedData.put(DRAGON, currentDragonData);
                    combinedData.put(ABILITIES, currentAbilityData);

                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(combinedData));
                    stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(getCustomModelData(level.registryAccess(), currentDragonData)));
                } else {
                    CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();

                    // Preserve spin/flight grant state
                    boolean flightGranted = handler.flightWasGranted;
                    boolean spinGranted = handler.spinWasGranted;
                    handler.deserializeNBT(level.registryAccess(), tag.getCompound(DRAGON), true);
                    handler.flightWasGranted = flightGranted;
                    handler.spinWasGranted = spinGranted;

                    handler.refreshMagicData(serverPlayer, true);
                    magicData.deserializeNBTForCurrentSpecies(level.registryAccess(), tag.getCompound(ABILITIES));

                    stack.set(DataComponents.CUSTOM_DATA, null);
                    stack.set(DataComponents.CUSTOM_MODEL_DATA, null);
                }
            } else if (handler.isDragon()) {
                CompoundTag currentDragonData = handler.serializeNBT(level.registryAccess(), true);
                CompoundTag currentAbilityData = magicData.serializeNBTForCurrentSpecies(level.registryAccess());
                CompoundTag combinedData = new CompoundTag();

                combinedData.put(DRAGON, currentDragonData);
                combinedData.put(ABILITIES, currentAbilityData);

                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(combinedData));
                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(getCustomModelData(level.registryAccess(), currentDragonData)));
                handler.revertToHumanForm(player, true);
            }
        }

        player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN);

        PlayerLoginHandler.syncHandler(player);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDER_DRAGON_GROWL, entity.getSoundSource(), 1.0F, 1.0F);

        for (int i = 0; i < 10; i++) {
            level.addParticle(ParticleTypes.SOUL, entity.getX() + (level.random.nextDouble() - 0.5D) * 0.5D, entity.getY() + (level.random.nextDouble() - 0.5D) * 0.5D, entity.getZ() + (level.random.nextDouble() - 0.5D) * 0.5D, (level.random.nextDouble() - 0.5D) * 0.5D, level.random.nextDouble() * 0.5D, (level.random.nextDouble() - 0.5D) * 0.5D);
        }

        return stack;
    }

    /** See {@link by.dragonsurvivalteam.dragonsurvival.client.extensions.ShakeWhenUsedExtension} */
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull final ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(@NotNull final ItemStack stack, @NotNull final LivingEntity entity) {
        return Functions.secondsToTicks(2);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @NotNull final TooltipContext context, @NotNull final List<Component> tooltips, @NotNull final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltips, flag);
        HolderLookup.Provider provider = context.registries();

        if (provider == null) {
            return;
        }

        if (stack.has(DataComponents.CUSTOM_DATA)) {
            //noinspection DataFlowIssue, deprecation -> tag isn't modified, no need to create a copy
            CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).getUnsafe().getCompound(DRAGON);
            tooltips.add(Component.translatable(DESCRIPTION));

            ResourceKey<DragonSpecies> species = ResourceHelper.decodeKey(provider, DragonSpecies.REGISTRY, tag, DragonStateHandler.DRAGON_SPECIES);
            Component name;

            if (species != null) {
                name = Component.translatable(Translation.Type.DRAGON_SPECIES.wrap(species.location()));
            } else {
                name = Component.translatable(INVALID_DRAGON_TYPE);
            }

            double growth = tag.getDouble(DragonStateHandler.GROWTH);
            Holder<DragonStage> stage = DragonStage.get(provider, growth);

            //noinspection DataFlowIssue -> key is present
            tooltips.add(Component.translatable(INFO, name, DragonStage.translatableName(stage.getKey()), String.format("%.0f", growth)));
        } else {
            tooltips.add(Component.translatable(IS_EMPTY));
        }
    }

    @Override
    public void onUseTick(@NotNull final Level level, @NotNull final LivingEntity entity, @NotNull final ItemStack soul, int remainingUseDuration) {
        super.onUseTick(level, entity, soul, remainingUseDuration);
        entity.playSound(SoundEvents.SOUL_ESCAPE.value(), (float) (0.3 + 0.3 * entity.getRandom().nextInt(2)), entity.getRandom().nextFloat() - entity.getRandom().nextFloat() * 0.2f + 1.0f);
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    @Override
    public @NotNull Component getName(@NotNull final ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            //noinspection DataFlowIssue, deprecation -> tag isn't modified, no need to create a copy
            ResourceKey<DragonSpecies> species = ResourceHelper.decodeKey(null, DragonSpecies.REGISTRY, stack.get(DataComponents.CUSTOM_DATA).getUnsafe(), DragonStateHandler.DRAGON_SPECIES);

            if (species != null) {
                return Component.translatable(Translation.Type.DRAGON_SPECIES.wrap(species.location())).append(Component.translatable(SOUL));
            }
        }

        return Component.translatable(EMPTY_DRAGON_SOUL);
    }

    private static final String DRAGON = "dragon";
    private static final String ABILITIES = "soul";
}
