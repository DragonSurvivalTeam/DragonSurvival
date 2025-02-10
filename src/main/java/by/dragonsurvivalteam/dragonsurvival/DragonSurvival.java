package by.dragonsurvivalteam.dragonsurvival;

import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSCommands;
import by.dragonsurvivalteam.dragonsurvival.registry.DSConditions;
import by.dragonsurvivalteam.dragonsurvival.registry.DSContainers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSCreativeTabs;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEquipment;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DSLootItemConditions;
import by.dragonsurvivalteam.dragonsurvival.registry.DSLootModifiers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSMapDecorationTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.DSStructurePlacementTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSubPredicates;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTrades;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.util.proxy.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.util.proxy.Proxy;
import by.dragonsurvivalteam.dragonsurvival.util.proxy.ServerProxy;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DragonSurvival.MODID)
public class DragonSurvival {
    public static final String DISCORD_URL = "https://discord.gg/8SsB8ar";

    /** See {@link MissingTextureAtlasSprite#MISSING_TEXTURE_LOCATION} */
    public static final ResourceLocation MISSING_TEXTURE = ResourceLocation.withDefaultNamespace("missingno");

    public static final String MODID = "dragonsurvival";
    public static final Logger LOGGER = LogManager.getLogger("Dragon Survival");
    public static Proxy PROXY;

    public DragonSurvival(final IEventBus bus, final ModContainer ignored) {
        PROXY = FMLLoader.getDist().isClient() ? new ClientProxy() : new ServerProxy();
        ConfigHandler.initConfig();

        DSDataAttachments.REGISTRY.register(bus);
        DSDataComponents.REGISTRY.register(bus);
        DSAttributes.REGISTRY.register(bus);
        DSEquipment.REGISTRY.register(bus);
        DSBlocks.REGISTRY.register(bus); // Needs to happen before items
        DSItems.REGISTRY.register(bus);
        DSEffects.REGISTRY.register(bus);
        DSContainers.REGISTRY.register(bus);
        DSCreativeTabs.REGISTRY.register(bus);
        DSParticles.REGISTRY.register(bus);
        DSSounds.REGISTRY.register(bus);
        DSBlockEntities.REGISTRY.register(bus);
        DSEntities.REGISTRY.register(bus);
        DSMapDecorationTypes.REGISTRY.register(bus);
        DSTrades.POI_REGISTRY.register(bus);
        DSTrades.PROFESSION_REGISTRY.register(bus);
        DSStructurePlacementTypes.REGISTRY.register(bus);
        DSSubPredicates.REGISTRY.register(bus);
        DSAdvancementTriggers.REGISTRY.register(bus);
        DSCommands.REGISTRY.register(bus);
        DSLootItemConditions.REGISTRY.register(bus);
        DSLootModifiers.REGISTRY.register(bus);
        DSConditions.REGISTRY.register(bus);
    }

    /** Creates a {@link ResourceLocation} with the dragon survival namespace */
    public static ResourceLocation res(final String path) {
        return location(DragonSurvival.MODID, path);
    }

    public static ResourceLocation location(final String namespace, final String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}