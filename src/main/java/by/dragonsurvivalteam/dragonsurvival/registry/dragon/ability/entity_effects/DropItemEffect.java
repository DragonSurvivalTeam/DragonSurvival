package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DropItemEffect(EntityEquipmentPredicate equipmentPredicate, LevelBasedValue probability, Optional<Holder<SoundEvent>> sound) implements AbilityEntityEffect {

    public static final MapCodec<DropItemEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityEquipmentPredicate.CODEC.fieldOf("equipment_predicate").forGetter(DropItemEffect::equipmentPredicate),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(DropItemEffect::probability),
            SoundEvent.CODEC.optionalFieldOf("sound").forGetter(DropItemEffect::sound)
    ).apply(instance, DropItemEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            if (dragon.getRandom().nextDouble() > probability().calculate(ability.level())) {
                return;
            }

            // EntityEquipmentPredicate is a sub-predicate of EntityPredicate - maybe use that?  Or SlotsPredicate
            // The below should be simplified, but I can't think of how at the moment and feedback would be helpful
            List<ItemStack> itemsToDrop = new ArrayList<>();
            if (equipmentPredicate.head().isPresent() && equipmentPredicate.head().get().test(livingEntity.getItemBySlot(EquipmentSlot.HEAD))) {
                itemsToDrop.add(livingEntity.getItemBySlot(EquipmentSlot.HEAD));
                livingEntity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
            if (equipmentPredicate.chest().isPresent() && equipmentPredicate.chest().get().test(livingEntity.getItemBySlot(EquipmentSlot.CHEST))) {
                itemsToDrop.add(livingEntity.getItemBySlot(EquipmentSlot.CHEST));
                livingEntity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            }
            if (equipmentPredicate.legs().isPresent() && equipmentPredicate.legs().get().test(livingEntity.getItemBySlot(EquipmentSlot.LEGS))) {
                itemsToDrop.add(livingEntity.getItemBySlot(EquipmentSlot.LEGS));
                livingEntity.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
            }
            if (equipmentPredicate.feet().isPresent() && equipmentPredicate.feet().get().test(livingEntity.getItemBySlot(EquipmentSlot.FEET))) {
                itemsToDrop.add(livingEntity.getItemBySlot(EquipmentSlot.FEET));
                livingEntity.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
            }
            if (equipmentPredicate.body().isPresent() && equipmentPredicate.body().get().test(livingEntity.getItemBySlot(EquipmentSlot.BODY))) {
                itemsToDrop.add(livingEntity.getItemBySlot(EquipmentSlot.BODY));
                livingEntity.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
            }
            if (equipmentPredicate.mainhand().isPresent() && equipmentPredicate.mainhand().get().test(livingEntity.getItemBySlot(EquipmentSlot.MAINHAND))) {
                itemsToDrop.add(livingEntity.getItemBySlot(EquipmentSlot.MAINHAND));
                livingEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                // Below is possibly redundant
                livingEntity.stopUsingItem();
            }
            if (equipmentPredicate.offhand().isPresent() && equipmentPredicate.offhand().get().test(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND))) {
                itemsToDrop.add(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND));
                livingEntity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }

            Vec3 pos = livingEntity.position();
            ServerLevel level = dragon.serverLevel();

            for (ItemStack item : itemsToDrop) {
                // Unsure if this will keep the item durability & other data intact
                ItemEntity droppedStackEntity = new ItemEntity(level, pos.x(), pos.y(), pos.z(), item);
                level.addFreshEntity(droppedStackEntity);
            }

            sound.ifPresent(soundHolder -> dragon.level().playSound(null, target, soundHolder.value(), SoundSource.BLOCKS, 1, 1));
        }

    }


    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
