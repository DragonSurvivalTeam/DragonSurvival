package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ItemData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

public record SmeltItemEffect(Optional<ItemPredicate> itemPredicate, Optional<LevelBasedValue> progress, boolean dropsExperience) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Smelts items§r %s as fast as a furnace")
    private static final String CUSTOM_SPEED = Translation.Type.GUI.wrap("smelting_effect.custom_speed");

    @Translation(comments = "§6■ Smelts items§r at the speed of a furnace")
    private static final String FURNACE_SPEED = Translation.Type.GUI.wrap("smelting_effect.furnace_speed");

    @Translation(comments = "§6■ Smelts items§r instantly")
    private static final String INSTANT = Translation.Type.GUI.wrap("smelting_effect.instant");

    public static final MapCodec<SmeltItemEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemPredicate.CODEC.optionalFieldOf("item_predicate").forGetter(SmeltItemEffect::itemPredicate),
            LevelBasedValue.CODEC.optionalFieldOf("progress").forGetter(SmeltItemEffect::progress),
            Codec.BOOL.optionalFieldOf("grants_experience", true).forGetter(SmeltItemEffect::dropsExperience)
    ).apply(instance, SmeltItemEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack stack = itemEntity.getItem();

        if (itemPredicate.map(predicate -> !predicate.test(stack)).orElse(false)) {
            return;
        }

        SingleRecipeInput input = new SingleRecipeInput(stack);
        RecipeHolder<SmeltingRecipe> recipe = dragon.level().recipeAccess().getRecipeFor(RecipeType.SMELTING, input, dragon.level()).orElse(null);
        ItemStack result = recipe != null ? recipe.value().assemble(input) : ItemStack.EMPTY;

        if (recipe == null || result.isEmpty()) {
            return;
        }

        if (progress.isPresent()) {
            ItemData data = itemEntity.getData(DSDataAttachments.ITEM);
            data.smeltingProgress += progress.get().calculate(ability.level());
            data.smeltingTime = recipe.value().cookingTime() * stack.getCount();

            // There may be some race conditions with the progress reset packet sent from 'ItemData'
            // But for that you'd have to wait until it is almost ready to send the packet and then time the breath to it
            // Causing both packets to potentially switch in order when the client receives them - in general probably unlikely to happen
            PacketDistributor.sendToPlayersNear(dragon.level(), null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 16, new SyncData(itemEntity.getId(), DSDataAttachments.ITEM.getId(), DSDataAttachments.serializeToCompoundTag(data, itemEntity.registryAccess())));

            if (data.smeltingProgress < data.smeltingTime) {
                return;
            }

            data.smeltingProgress = 0;
        }

        itemEntity.setItem(result.copyWithCount(result.getCount() * stack.getCount()));

        if (!dropsExperience) {
            return;
        }

        float experience = recipe.value().experience() * stack.getCount();
        int experienceToDrop = Mth.floor(experience);
        float remainder = Mth.frac(experience);

        if (remainder != 0.0F && dragon.getRandom().nextFloat() < remainder) {
            experienceToDrop++;
        }

        if (experienceToDrop > 0 && dragon.level() instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(serverLevel, itemEntity.position(), experienceToDrop);
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        if (this.progress.isPresent()) {
            double progress = this.progress.get().calculate(ability.level());

            if (progress == 1) {
                return List.of(Component.translatable(FURNACE_SPEED));
            } else {
                return List.of(Component.translatable(CUSTOM_SPEED, DSColors.dynamicValue(NumberFormat.getPercentInstance().format(progress))));
            }
        } else {
            return List.of(Component.translatable(INSTANT));
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
