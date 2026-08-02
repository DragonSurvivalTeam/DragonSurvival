package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.SourceOfMagicScreen;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.server.containers.SourceOfMagicContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DSContainers {

    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(
            Registries.MENU,
            DragonSurvival.MODID
    );

    public static final RegistryObject<MenuType<SourceOfMagicContainer>> SOURCE_OF_MAGIC_CONTAINER = REGISTRY.register("dragon_nest", () -> IForgeMenuType.create(SourceOfMagicContainer::new));
    public static final RegistryObject<MenuType<DragonContainer>> DRAGON_CONTAINER = REGISTRY.register("dragon_container", () -> new MenuType<>(DragonContainer::new, FeatureFlags.DEFAULT_FLAGS));

    @EventBusSubscriber(modid = DragonSurvival.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {}

        @SubscribeEvent
        public static void registerScreens(final FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(SOURCE_OF_MAGIC_CONTAINER.get(), SourceOfMagicScreen::new);
                MenuScreens.register(DRAGON_CONTAINER.get(), DragonInventoryScreen::new);
            });
        }
    }
}
