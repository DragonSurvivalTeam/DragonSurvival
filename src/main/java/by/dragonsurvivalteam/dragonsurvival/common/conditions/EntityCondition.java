package by.dragonsurvivalteam.dragonsurvival.common.conditions;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.CustomPredicates;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.EntityCheckPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.NearbyEntityPredicate;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;

public class EntityCondition {
    public static EntityPredicate defaultNaturalGrowthBlocker() {
        return EntityPredicate.Builder.entity().subPredicate(
                DragonPredicate.Builder.dragon().growthStopped(true).build()
        ).build();
    }

    public static EntityPredicate isLiving() {
        return isType(EntityCheckPredicate.Type.LIVING_ENTITY);
    }

    public static EntityPredicate isType(final EntityCheckPredicate.Type type) {
        return EntityPredicate.Builder.entity().subPredicate(EntityCheckPredicate.Builder.start().type(type).build()).build();
    }

    public static EntityPredicate isOnBlock(final TagKey<Block> tag) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(tag))).build();
    }

    public static EntityPredicate isOnBlock(final Block... blocks) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks))).build();
    }

    public static EntityPredicate isOnBlock(final TagKey<Block> block, final Property<?> property, final String value) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(
                BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))
        )).build();
    }

    public static EntityPredicate isOnBlock(final TagKey<Block> block, final Property<Integer> property, final int value) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(
                BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))
        )).build();
    }

    public static EntityPredicate isOnBlock(final TagKey<Block> block, final Property<Boolean> property, final boolean value) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(
                BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))
        )).build();
    }

    public static <T extends Comparable<T> & StringRepresentable> EntityPredicate isOnBlock(final TagKey<Block> block, final Property<T> property, final T value) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(
                BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))
        )).build();
    }

    public static EntityPredicate isOnBlock(final Block block, final Property<?> property, final String value) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(
                BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))
        )).build();
    }

    public static EntityPredicate isOnBlock(final Block block, final Property<Integer> property, final int value) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(
                BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))
        )).build();
    }

    public static EntityPredicate isOnBlock(final Block block, final Property<Boolean> property, final boolean value) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(
                BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))
        )).build();
    }

    public static <T extends Comparable<T> & StringRepresentable> EntityPredicate isOnBlock(final Block block, final Property<T> property, final T value) {
        return EntityPredicate.Builder.entity().steppingOn(LocationPredicate.Builder.location().setBlock(
                BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, value))
        )).build();
    }

    public static EntityPredicate isInBlock(final TagKey<Block> tag) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(tag))).build();
    }

    public static EntityPredicate isInFluid(final HolderSet<Fluid> fluids) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setFluid(FluidPredicate.Builder.fluid().of(fluids))).build();
    }

    public static EntityPredicate isEyeInFluid(final Holder<FluidType> fluid) {
        return EntityPredicate.Builder.entity().subPredicate(CustomPredicates.Builder.start().eyeInFluid(fluid).build()).build();
    }

    public static EntityPredicate isInRain() {
        return EntityPredicate.Builder.entity().subPredicate(CustomPredicates.Builder.start().raining(true).build()).build();
    }

    public static EntityPredicate isInRainOrSnow() {
        return EntityPredicate.Builder.entity().subPredicate(CustomPredicates.Builder.start().rainingOrSnowing(true).build()).build();
    }

    public static EntityPredicate isInSunlight(int sunLightLevel) {
        return EntityPredicate.Builder.entity().subPredicate(CustomPredicates.Builder.start().sunLightLevel(sunLightLevel).build()).build();
    }

    public static EntityPredicate isInLight(int lightLevel) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setLight(LightPredicate.Builder.light().setComposite(MinMaxBounds.Ints.atLeast(lightLevel)))).build();
    }

    public static EntityPredicate isOnGround(boolean isOnGround) {
        return EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnGround(isOnGround)).build();
    }

    public static EntityPredicate isMarked(boolean isMarked) {
        return EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().markedByEnderDragon(isMarked).build()).build();
    }

    public static EntityPredicate flightWasGranted(boolean flightWasGranted) {
        return EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().flightWasGranted(flightWasGranted).build()).build();
    }

    public static EntityPredicate spinWasGranted(boolean spinWasGranted) {
        return EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().spinWasGranted(spinWasGranted).build()).build();
    }

    public static EntityPredicate isNearbyTo(int radius, final EntityType<?>... types) {
        return EntityPredicate.Builder.entity().subPredicate(CustomPredicates.Builder.start().isNearbyEntity(NearbyEntityPredicate.of(radius, EntityType.BEE)).build()).build();
    }

    public static EntityPredicate inDimension(final ResourceKey<Level> dimension) {
        return EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setDimension(dimension)).build();
    }

    @SafeVarargs
    public static EntityPredicate hasEffect(final Holder<MobEffect>... effects) {
        MobEffectsPredicate.Builder builder = MobEffectsPredicate.Builder.effects();

        for (Holder<MobEffect> effect : effects) {
            builder.and(effect);
        }

        return EntityPredicate.Builder.entity().effects(builder).build();
    }

    public static EntityPredicate isItemEquipped(final EquipmentSlot equipmentSlot, final TagKey<Item> tag) {
        EntityEquipmentPredicate.Builder builder = EntityEquipmentPredicate.Builder.equipment();

        switch (equipmentSlot) {
            case MAINHAND -> builder.mainhand(ItemPredicate.Builder.item().of(tag));
            case OFFHAND -> builder.offhand(ItemPredicate.Builder.item().of(tag));
            case FEET -> builder.feet(ItemPredicate.Builder.item().of(tag));
            case LEGS -> builder.legs(ItemPredicate.Builder.item().of(tag));
            case CHEST -> builder.chest(ItemPredicate.Builder.item().of(tag));
            case HEAD -> builder.head(ItemPredicate.Builder.item().of(tag));
            case BODY -> builder.body(ItemPredicate.Builder.item().of(tag));
            default -> throw new IllegalArgumentException("Invalid equipment slot: " + equipmentSlot);
        }

        return EntityPredicate.Builder.entity().equipment(builder.build()).build();
    }

    public static EntityPredicate isType(final EntityType<?> type) {
        return EntityPredicate.Builder.entity().of(type).build();
    }

    public static EntityPredicate isType(final TagKey<EntityType<?>> tag) {
        return EntityPredicate.Builder.entity().of(tag).build();
    }

    public static EntityPredicate isDragon() {
        return EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().build()).build();
    }
}
