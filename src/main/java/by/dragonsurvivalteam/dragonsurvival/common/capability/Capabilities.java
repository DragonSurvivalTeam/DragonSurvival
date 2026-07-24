package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.capabilities.EntityCapability;
import net.minecraftforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber
public class Capabilities {
    // Acts as API for other mods to query dragon data
    public static final EntityCapability<DragonStateHandler, Void> DRAGON_CAPABILITY = EntityCapability.createVoid(
            DragonSurvival.res("dragon_capability"),
            DragonStateHandler.class);

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerEntity(DRAGON_CAPABILITY, EntityType.PLAYER, new DragonStateProvider());
    }
}