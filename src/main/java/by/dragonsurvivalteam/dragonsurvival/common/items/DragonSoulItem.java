package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.PlayerLoginHandler;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonSoulItem extends Item {
    @Translation(type = Translation.Type.MISC, comments = "Empty Dragon Soul")
    private static final String EMPTY_DRAGON_SOUL = Translation.Type.ITEM.wrap("empty_dragon_soul");

    @Translation(type = Translation.Type.MISC, comments = "■§7 This vessel holds the dragon's soul. Use it to become a dragon. Replaces your current stats if you are a dragon.\n")
    private static final String DESCRIPTION = Translation.Type.DESCRIPTION.wrap("dragon_soul");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Type:§r %s\n§6■ Growth Stage:§r %s\n§6■ Size:§r %s\n")
    private static final String INFO = Translation.Type.DESCRIPTION.wrap("dragon_soul.info");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Can Spin§r")
    private static final String HAS_SPIN = Translation.Type.DESCRIPTION.wrap("dragon_soul.has_spin");

    @Translation(type = Translation.Type.MISC, comments = "§6■ Can Fly§r")
    private static final String HAS_FLIGHT = Translation.Type.DESCRIPTION.wrap("dragon_soul.has_flight");

    @Translation(type = Translation.Type.MISC, comments = "■§7 An empty dragon's soul. With this item, you can store all your dragon's characteristics. After using it, you become human.")
    private static final String IS_EMPTY = Translation.Type.DESCRIPTION.wrap("dragon_soul.empty");

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

    private static int getCustomModelData(final CompoundTag tag) {
        // FIXME
        /*
        AbstractDragonType dragonType = DragonTypes.newDragonTypeInstance(tag.getString("type"));

        if (dragonType == null) {
            return 0;
        }

        return switch (dragonType) {
            case ForestDragonType ignored -> 1;
            case CaveDragonType ignored -> 2;
            case SeaDragonType ignored -> 3;
            default -> 0;
        };*/

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

        if (stack.has(DataComponents.CUSTOM_DATA)) {
            if (handler.isDragon()) {
                // Swap the player's dragon data with the item's NBT
                CompoundTag storedDragonData = stack.get(DataComponents.CUSTOM_DATA).copyTag();
                CompoundTag currentDragonData = handler.serializeNBT(level.registryAccess(), true);
                handler.deserializeNBT(level.registryAccess(), storedDragonData, true);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(currentDragonData));
                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(getCustomModelData(currentDragonData)));
                PlayerLoginHandler.syncCompleteAll(player);
            } else {
                CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
                handler.deserializeNBT(level.registryAccess(), tag, true);
                PlayerLoginHandler.syncCompleteAll(player);
                stack.set(DataComponents.CUSTOM_DATA, null);
                stack.set(DataComponents.CUSTOM_MODEL_DATA, null);
            }
        } else if (handler.isDragon()) {
            CompoundTag currentDragonData = handler.serializeNBT(level.registryAccess(), true);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(currentDragonData));
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(getCustomModelData(currentDragonData)));
            handler.revertToHumanForm(player, true);
            PlayerLoginHandler.syncCompleteAll(player);
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDER_DRAGON_GROWL, entity.getSoundSource(), 1.0F, 1.0F);

        for (int i = 0; i < 10; i++) { // TODO :: use other generic particle?
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
            CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).getUnsafe();
            tooltips.add(Component.translatable(DESCRIPTION));

            ResourceKey<DragonType> species = getSpecies(provider, tag);
            Component name;

            if (species != null) {
                name = Component.translatable(Translation.Type.DRAGON_SPECIES.wrap(species.location()));
            } else {
                // FIXME :: translatable
                name = Component.literal("Invalid dragon type");
            }

            double size = tag.getDouble(SIZE); // TODO :: should it store the stage as well?
            Holder<DragonStage> stage = DragonStage.get(provider, size);

            //noinspection DataFlowIssue -> key is present
            tooltips.add(Component.translatable(INFO, name, DragonStage.translatableName(stage.getKey()), String.format("%.0f", size)));

            if (tag.getBoolean(IS_SPIN_LEARNED)) {
                tooltips.add(Component.translatable(HAS_SPIN));
            }

            if (tag.getBoolean(IS_FLIGHT_LEARNED)) {
                tooltips.add(Component.translatable(HAS_FLIGHT));
            }
        } else {
            tooltips.add(Component.translatable(IS_EMPTY));
        }
    }

    @Override
    public void onUseTick(@NotNull final Level level, @NotNull final LivingEntity entity, @NotNull final ItemStack soul, int remainingUseDuration) {
        super.onUseTick(level, entity, soul, remainingUseDuration); // TODO :: use other generic sound?
        entity.playSound(SoundEvents.SOUL_ESCAPE.value(), (float) (0.3 + 0.3 * entity.getRandom().nextInt(2)), entity.getRandom().nextFloat() - entity.getRandom().nextFloat() * 0.2f + 1.0f);
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    @Override
    public @NotNull String getDescriptionId(@NotNull final ItemStack stack) {
        RegistryAccess access = DragonSurvival.PROXY.getAccess();

        if (access != null && stack.has(DataComponents.CUSTOM_DATA)) {
            //noinspection DataFlowIssue, deprecation -> tag isn't modified, no need to create a copy
            ResourceKey<DragonType> species = getSpecies(access, stack.get(DataComponents.CUSTOM_DATA).getUnsafe());

            if (species != null) { // TODO :: handle translation (probably annotation on type?)
                return Translation.Type.ITEM.wrap(species.location().getNamespace(), species.location().getPath() + ".dragon_soul");
            }
        }

        return EMPTY_DRAGON_SOUL;
    }

    private @Nullable ResourceKey<DragonType> getSpecies(final HolderLookup.Provider provider, final CompoundTag tag) {
        DataResult<Pair<ResourceKey<DragonType>, Tag>> result = ResourceKey.codec(DragonType.REGISTRY).decode(provider.createSerializationContext(NbtOps.INSTANCE), tag.get(SPECIES));

        if (result.isSuccess()) {
            return result.getOrThrow().getFirst();
        }

        return null;
    }

    public static final String SPECIES = "species";
    public static final String SIZE = "size";
    public static final String IS_SPIN_LEARNED = "is_spin_learned";
    public static final String IS_FLIGHT_LEARNED = "is_flight_learned";
}
