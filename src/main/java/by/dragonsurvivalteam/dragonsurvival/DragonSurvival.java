package by.dragonsurvivalteam.dragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.*;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot.AddTableLootExtendedLootModifier;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot.DragonHeartLootModifier;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot.DragonOreLootModifier;
import by.dragonsurvivalteam.dragonsurvival.util.proxy.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.util.proxy.Proxy;
import by.dragonsurvivalteam.dragonsurvival.util.proxy.ServerProxy;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(DragonSurvival.MODID)
public class DragonSurvival {
    public static final String DISCORD_URL = "https://discord.gg/8SsB8ar";

    public static final String MODID = "dragonsurvival";
    public static final Logger LOGGER = LogManager.getLogger("Dragon Survival");
    public static Proxy PROXY;

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID);
    private static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<DragonOreLootModifier>> DRAGON_ORE = DragonSurvival.GLM.register("dragon_ore", DragonOreLootModifier.CODEC);
    private static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<DragonHeartLootModifier>> DRAGON_HEART = DragonSurvival.GLM.register("dragon_heart", DragonHeartLootModifier.CODEC);
    private static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddTableLootExtendedLootModifier>> ADD_TABLE_LOOT_EXTENDED = DragonSurvival.GLM.register("add_table_loot_extended", () -> AddTableLootExtendedLootModifier.CODEC);

    public DragonSurvival(final IEventBus bus, final ModContainer ignored) {
        PROXY = FMLLoader.getDist().isClient() ? new ClientProxy() : new ServerProxy();
        ConfigHandler.initConfig();

        DSDataAttachments.DS_ATTACHMENT_TYPES.register(bus);
        DSDataComponents.REGISTRY.register(bus);
        DSAttributes.DS_ATTRIBUTES.register(bus);
        DSEquipment.DS_ARMOR_MATERIALS.register(bus);
        // We need to register blocks before items, since otherwise the items will register before the item-blocks can be assigned
        DSBlocks.DS_BLOCKS.register(bus);
        DSItems.DS_ITEMS.register(bus);
        DSEffects.DS_MOB_EFFECTS.register(bus);
        DSContainers.DS_CONTAINERS.register(bus);
        DSCreativeTabs.DS_CREATIVE_MODE_TABS.register(bus);
        DSParticles.DS_PARTICLES.register(bus);
        DSSounds.DS_SOUNDS.register(bus);
        DSPotions.DS_POTIONS.register(bus);
        DSTileEntities.DS_TILE_ENTITIES.register(bus);
        DSEntities.DS_ENTITY_TYPES.register(bus);
        DSMapDecorationTypes.DS_MAP_DECORATIONS.register(bus);
        DSTrades.DS_POI_TYPES.register(bus);
        DSTrades.DS_VILLAGER_PROFESSIONS.register(bus);
        DSStructurePlacementTypes.DS_STRUCTURE_PLACEMENT_TYPES.register(bus);
        DSSubPredicates.REGISTRY.register(bus);
        DSAdvancementTriggers.DS_TRIGGERS.register(bus);
        DSCommands.ARGUMENT_TYPES.register(bus);
        GLM.register(bus);
    }

    /** Creates a {@link ResourceLocation} with the dragon survival namespace */
    public static ResourceLocation res(final String path) {
        return location(DragonSurvival.MODID, path);
    }

    public static ResourceLocation location(final String namespace, final String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}