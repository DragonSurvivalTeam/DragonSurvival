package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.SourceOfMagicScreen;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.server.containers.SourceOfMagicContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.RegisterMenuScreensEvent;
import net.minecraftforge.common.extensions.IMenuTypeExtension;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

@EventBusSubscriber
public class DSContainers {

    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(
            Registries.MENU,
            DragonSurvival.MODID
    );

    public static final RegistryObject<MenuType<SourceOfMagicContainer>> SOURCE_OF_MAGIC_CONTAINER = REGISTRY.register("dragon_nest", () -> IMenuTypeExtension.create(SourceOfMagicContainer::new));
    public static final RegistryObject<MenuType<DragonContainer>> DRAGON_CONTAINER = REGISTRY.register("dragon_container", () -> new MenuType<>(DragonContainer::new, FeatureFlags.DEFAULT_FLAGS));

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SOURCE_OF_MAGIC_CONTAINER.get(), SourceOfMagicScreen::new);
        event.register(DRAGON_CONTAINER.get(), DragonInventoryScreen::new);
    }
}
