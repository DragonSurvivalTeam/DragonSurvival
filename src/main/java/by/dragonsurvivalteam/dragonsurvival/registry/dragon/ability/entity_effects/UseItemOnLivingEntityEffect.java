package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record UseItemOnLivingEntityEffect(ItemStack item, LevelBasedValue probability, Optional<Holder<SoundEvent>> sound, Optional<EntityPredicate> validEntities) implements AbilityEntityEffect {
    public static final MapCodec<UseItemOnLivingEntityEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(UseItemOnLivingEntityEffect::item),
            LevelBasedValue.CODEC.optionalFieldOf("probability", LevelBasedValue.constant(1)).forGetter(UseItemOnLivingEntityEffect::probability),
            SoundEvent.CODEC.optionalFieldOf("sound").forGetter(UseItemOnLivingEntityEffect::sound),
            // TODO 1.22 :: Is this even needed / useful, considering the existing targeting logic?
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(UseItemOnLivingEntityEffect::validEntities)
    ).apply(instance, UseItemOnLivingEntityEffect::new));
    
    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            if (dragon.getRandom().nextDouble() > probability().calculate(ability.level())) {
                return;
            }

            if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
                return;
            }

            ServerLevel level = dragon.serverLevel();
            if (validEntities.isEmpty() || validEntities.get().matches(level, dragon.position(), target)) {
                InteractionResult ir = item.getItem().interactLivingEntity(item, dragon, livingEntity, InteractionHand.MAIN_HAND);

                if (ir.consumesAction()) {
                    sound.ifPresent(soundHolder -> dragon.level().playSound(null, target, soundHolder.value(), SoundSource.BLOCKS, 1, 1));
                }
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
