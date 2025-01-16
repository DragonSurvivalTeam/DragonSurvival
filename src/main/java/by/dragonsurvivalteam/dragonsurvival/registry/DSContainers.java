package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.SourceOfMagicScreen;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.server.containers.SourceOfMagicContainer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DSContainers {

    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(
            BuiltInRegistries.MENU,
            DragonSurvival.MODID
    );

    public static final DeferredHolder<MenuType<?>, MenuType<SourceOfMagicContainer>> SOURCE_OF_MAGIC_CONTAINER = REGISTRY.register("dragon_nest", () -> IMenuTypeExtension.create(SourceOfMagicContainer::new));
    public static final DeferredHolder<MenuType<?>, MenuType<DragonContainer>> DRAGON_CONTAINER = REGISTRY.register("dragon_container", () -> new MenuType<>(DragonContainer::new, FeatureFlags.DEFAULT_FLAGS));

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SOURCE_OF_MAGIC_CONTAINER.get(), SourceOfMagicScreen::new);
        event.register(DRAGON_CONTAINER.get(), DragonInventoryScreen::new);
    }
}