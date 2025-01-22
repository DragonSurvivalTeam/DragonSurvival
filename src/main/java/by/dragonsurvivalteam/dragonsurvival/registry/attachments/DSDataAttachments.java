package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.LightningHandler;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class DSDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DragonSurvival.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EntityStateHandler>> ENTITY_HANDLER = REGISTRY.register("entity_handler", () -> AttachmentType.serializable(EntityStateHandler::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DragonStateHandler>> DRAGON_HANDLER = REGISTRY.register("dragon_handler", () -> AttachmentType.serializable(DragonStateHandler::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SummonData>> SUMMON = REGISTRY.register("summon_data", () -> AttachmentType.serializable(SummonData::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LightningHandler>> LIGHTNING_BOLT = REGISTRY.register("lightning_bolt_data", () -> AttachmentType.serializable(LightningHandler::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MovementData>> MOVEMENT = REGISTRY.register("movement_data", () -> AttachmentType.builder(MovementData::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FlightData>> FLIGHT = REGISTRY.register("flight_data", () -> AttachmentType.serializable(FlightData::new).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ClawInventoryData>> CLAW_INVENTORY = REGISTRY.register("claw_inventory_data", () -> AttachmentType.serializable(ClawInventoryData::new).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TreasureRestData>> TREASURE_REST = REGISTRY.register("treasure_rest_data", () -> AttachmentType.serializable(TreasureRestData::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AltarData>> ALTAR = REGISTRY.register("altar_data", () -> AttachmentType.serializable(AltarData::new).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EffectsMaintainedThroughDeath>> EFFECTS_MAINTAINED_THROUGH_DEATH = REGISTRY.register("effects_maintained_through_death", () -> AttachmentType.serializable(EffectsMaintainedThroughDeath::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EnderDragonDamageHistory>> ENDER_DRAGON_DAMAGE_HISTORY = REGISTRY.register("ender_dragon_damage_history", () -> AttachmentType.serializable(EnderDragonDamageHistory::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlacedEndPlatforms>> PLACED_END_PLATFORMS = REGISTRY.register("placed_end_platforms", () -> AttachmentType.serializable(PlacedEndPlatforms::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HunterData>> HUNTER = REGISTRY.register("hunter_data", () -> AttachmentType.serializable(HunterData::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PenaltySupply>> PENALTY_SUPPLY = REGISTRY.register("penalty_supply", () -> AttachmentType.serializable(PenaltySupply::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MagicData>> MAGIC = REGISTRY.register("magic_data", () -> AttachmentType.serializable(MagicData::new).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OnAttackEffects>> ON_ATTACK_EFFECTS = REGISTRY.register("on_attack_effects", () -> AttachmentType.builder(OnAttackEffects::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SwimData>> SWIM = REGISTRY.register("swim_data", () -> AttachmentType.builder(SwimData::new).build());

    // Storage types
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ModifiersWithDuration>> MODIFIERS_WITH_DURATION = REGISTRY.register("modifiers_with_duration", () -> AttachmentType.serializable(ModifiersWithDuration::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DamageModifications>> DAMAGE_MODIFICATIONS = REGISTRY.register("damage_modifications", () -> AttachmentType.serializable(DamageModifications::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EffectModifications>> EFFECT_MODIFICATIONS = REGISTRY.register("effect_modifications", () -> AttachmentType.serializable(EffectModifications::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HarvestBonuses>> HARVEST_BONUSES = REGISTRY.register("harvest_bonuses", () -> AttachmentType.serializable(HarvestBonuses::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SummonedEntities>> SUMMONED_ENTITIES = REGISTRY.register("summoned_entities", () -> AttachmentType.serializable(SummonedEntities::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GlowData>> GLOW = REGISTRY.register("glow_data", () -> AttachmentType.builder(GlowData::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OxygenBonuses>> OXYGEN_BONUSES = REGISTRY.register("oxygen_bonuses", () -> AttachmentType.serializable(OxygenBonuses::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BlockVisionData>> BLOCK_VISION = REGISTRY.register("block_vision_data", () -> AttachmentType.serializable(BlockVisionData::new).build());

    /** Does not return empty storages */
    public static <T> List<Storage<? extends T>> getStorages(final Entity entity, final Class<T> type) {
        List<Storage<? extends T>> storages = new ArrayList<>();

        REGISTRY.getEntries().forEach(entry -> {
            if (entity.getExistingData(entry.get()).orElse(null) instanceof Storage<?> storage) {
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
            if (entity.getExistingData(entry.get()).orElse(null) instanceof Storage<?> storage) {
                if (storage.isEmpty()) {
                    return;
                }

                storages.add(storage);
            }
        });

        return storages;
    }
}
