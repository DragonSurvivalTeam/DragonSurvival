package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.compat.attachments.AttachmentType;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.LightningHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DSDataAttachments {
    public static final ResourceLocation REGISTRY_NAME = DragonSurvival.res("attachment_type");
    public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(REGISTRY_NAME, DragonSurvival.MODID);
    public static final Supplier<IForgeRegistry<AttachmentType<?>>> ATTACHMENT_TYPES = REGISTRY.makeRegistry(RegistryBuilder::new);

    public static final RegistryObject<AttachmentType<EntityStateHandler>> ENTITY_HANDLER = REGISTRY.register("entity_handler", () -> AttachmentType.serializable(EntityStateHandler::new).build());
    public static final RegistryObject<AttachmentType<DragonStateHandler>> DRAGON_HANDLER = REGISTRY.register("dragon_handler", () -> AttachmentType.serializable(DragonStateHandler::new).copyOnDeath().build());
    public static final RegistryObject<AttachmentType<PlayerData>> PLAYER_DATA = REGISTRY.register("player_data", () -> AttachmentType.serializable(PlayerData::new).copyOnDeath().build());

    public static final RegistryObject<AttachmentType<ItemData>> ITEM = REGISTRY.register("item_data", () -> AttachmentType.serializable(ItemData::new).build());

    public static final RegistryObject<AttachmentType<SummonData>> SUMMON = REGISTRY.register("summon_data", () -> AttachmentType.serializable(SummonData::new).build());

    public static final RegistryObject<AttachmentType<LightningHandler>> LIGHTNING_BOLT = REGISTRY.register("lightning_bolt_data", () -> AttachmentType.serializable(LightningHandler::new).build());
    public static final RegistryObject<AttachmentType<MovementData>> MOVEMENT = REGISTRY.register("movement_data", () -> AttachmentType.serializable(MovementData::new).build());
    public static final RegistryObject<AttachmentType<FlightData>> FLIGHT = REGISTRY.register("flight_data", () -> AttachmentType.serializable(FlightData::new).copyOnDeath().build());
    public static final RegistryObject<AttachmentType<ClawInventoryData>> CLAW_INVENTORY = REGISTRY.register("claw_inventory_data", () -> AttachmentType.serializable(ClawInventoryData::new).copyOnDeath().build());
    public static final RegistryObject<AttachmentType<TreasureRestData>> TREASURE_REST = REGISTRY.register("treasure_rest_data", () -> AttachmentType.serializable(TreasureRestData::new).build());
    public static final RegistryObject<AttachmentType<AltarData>> ALTAR = REGISTRY.register("altar_data", () -> AttachmentType.serializable(AltarData::new).copyOnDeath().build());
    public static final RegistryObject<AttachmentType<EffectsMaintainedThroughDeath>> EFFECTS_MAINTAINED_THROUGH_DEATH = REGISTRY.register("effects_maintained_through_death", () -> AttachmentType.serializable(EffectsMaintainedThroughDeath::new).copyOnDeath().build());

    public static final RegistryObject<AttachmentType<EnderDragonDamageHistory>> ENDER_DRAGON_DAMAGE_HISTORY = REGISTRY.register("ender_dragon_damage_history", () -> AttachmentType.serializable(EnderDragonDamageHistory::new).build());
    public static final RegistryObject<AttachmentType<PlacedEndPlatforms>> PLACED_END_PLATFORMS = REGISTRY.register("placed_end_platforms", () -> AttachmentType.serializable(PlacedEndPlatforms::new).build());

    public static final RegistryObject<AttachmentType<HunterData>> HUNTER = REGISTRY.register("hunter_data", () -> AttachmentType.serializable(HunterData::new).build());
    public static final RegistryObject<AttachmentType<PenaltySupply>> PENALTY_SUPPLY = REGISTRY.register("penalty_supply", () -> AttachmentType.serializable(PenaltySupply::new).build());
    public static final RegistryObject<AttachmentType<MagicData>> MAGIC = REGISTRY.register("magic_data", () -> AttachmentType.serializable(MagicData::new).copyOnDeath().build());
    // Not serialized because these effects should not persist after logout.
    public static final RegistryObject<AttachmentType<OnAttackEffects>> ON_ATTACK_EFFECTS = REGISTRY.register("on_attack_effects", () -> AttachmentType.builder(OnAttackEffects::new).build());
    public static final RegistryObject<AttachmentType<SwimData>> SWIM = REGISTRY.register("swim_data", () -> AttachmentType.serializable(SwimData::new).build());

    // Storage types
    public static final RegistryObject<AttachmentType<ModifiersWithDuration>> MODIFIERS_WITH_DURATION = REGISTRY.register("modifiers_with_duration", () -> AttachmentType.serializable(ModifiersWithDuration::new).build());
    public static final RegistryObject<AttachmentType<DamageModifications>> DAMAGE_MODIFICATIONS = REGISTRY.register("damage_modifications", () -> AttachmentType.serializable(DamageModifications::new).build());
    public static final RegistryObject<AttachmentType<EffectModifications>> EFFECT_MODIFICATIONS = REGISTRY.register("effect_modifications", () -> AttachmentType.serializable(EffectModifications::new).build());
    public static final RegistryObject<AttachmentType<HarvestBonuses>> HARVEST_BONUSES = REGISTRY.register("harvest_bonuses", () -> AttachmentType.serializable(HarvestBonuses::new).build());
    public static final RegistryObject<AttachmentType<SummonedEntities>> SUMMONED_ENTITIES = REGISTRY.register("summoned_entities", () -> AttachmentType.serializable(SummonedEntities::new).build());
    public static final RegistryObject<AttachmentType<GlowData>> GLOW = REGISTRY.register("glow_data", () -> AttachmentType.serializable(GlowData::new).build());
    public static final RegistryObject<AttachmentType<OxygenBonuses>> OXYGEN_BONUSES = REGISTRY.register("oxygen_bonuses", () -> AttachmentType.serializable(OxygenBonuses::new).build());
    public static final RegistryObject<AttachmentType<BlockVisionData>> BLOCK_VISION = REGISTRY.register("block_vision_data", () -> AttachmentType.serializable(BlockVisionData::new).build());
    public static final RegistryObject<AttachmentType<FearData>> FEAR = REGISTRY.register("fear_data", () -> AttachmentType.serializable(FearData::new).build());
    public static final RegistryObject<AttachmentType<ClimbableData>> CLIMBABLE_DATA = REGISTRY.register("climbable_data", () -> AttachmentType.serializable(ClimbableData::new).build());

    /** Does not return empty storages */
    public static <T> List<Storage<? extends T>> getStorages(final Entity entity, final Class<T> type) {
        List<Storage<? extends T>> storages = new ArrayList<>();

        REGISTRY.getEntries().forEach(entry -> {
            if (AttachmentManager.getExistingData(entity, entry.get()).orElse(null) instanceof Storage<?> storage) {
                if (storage.isEmpty()) {
                    return;
                }

                if (storage.isType(type)) {
                    //noinspection unchecked -> type is checked
                    storages.add((Storage<? extends T>) storage);
                }
            }
        });

        return storages;
    }

    /** Does not return empty storages */
    public static List<Storage<?>> getStorages(final Entity entity) {
        List<Storage<?>> storages = new ArrayList<>();

        REGISTRY.getEntries().forEach(entry -> {
            if (AttachmentManager.getExistingData(entity, entry.get()).orElse(null) instanceof Storage<?> storage) {
                if (storage.isEmpty()) {
                    return;
                }

                storages.add(storage);
            }
        });

        return storages;
    }
}
