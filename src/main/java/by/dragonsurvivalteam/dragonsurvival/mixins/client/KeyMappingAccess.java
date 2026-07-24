package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyMapping.class)
public interface KeyMappingAccess {
    @Accessor("ALL")
    static Map<String, KeyMapping> dragonSurvival$getAllKeymappings() {
        throw new AssertionError();
    }
}
