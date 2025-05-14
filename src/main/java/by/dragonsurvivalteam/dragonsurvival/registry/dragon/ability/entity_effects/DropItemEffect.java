package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record DropItemEffect(Optional<ItemPredicate> itemPredicate, LevelBasedValue probability, Optional<Holder<SoundEvent>> sound, Optional<EntityPredicate> validEntities) implements AbilityEntityEffect {

    public static final MapCodec<DropItemEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemPredicate.CODEC.optionalFieldOf("item_predicate").forGetter(DropItemEffect::itemPredicate),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(DropItemEffect::probability),
            SoundEvent.CODEC.optionalFieldOf("sound").forGetter(DropItemEffect::sound),
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(DropItemEffect::validEntities)
    ).apply(instance, DropItemEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            if (dragon.getRandom().nextDouble() > probability().calculate(ability.level())) {
                return;
            }

            if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
                return;
            }

            ServerLevel level = dragon.serverLevel();
            if (validEntities.isEmpty() || validEntities.get().matches(level, dragon.position(), target)) {

                /** Add a CODEC here to allow for a list of things to drop instead? */
                ItemStack stack = livingEntity.getMainHandItem();

                if (itemPredicate.map(predicate -> !predicate.test(stack)).orElse(false)) {
                    return;
                }

                Vec3 pos = livingEntity.position();

                ItemStack droppedStack = new ItemStack(stack.getItem(), 1);
                ItemEntity droppedStackEntity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), droppedStack);

                level.addFreshEntity(droppedStackEntity);
                livingEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                /** Below is possibly redundant */
                livingEntity.stopUsingItem();

                sound.ifPresent(soundHolder -> dragon.level().playSound(null, target, soundHolder.value(), SoundSource.BLOCKS, 1, 1));
            }
        }

    }


    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
