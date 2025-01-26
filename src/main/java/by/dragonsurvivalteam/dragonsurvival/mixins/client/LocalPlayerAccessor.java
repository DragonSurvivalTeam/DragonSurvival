package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {
    @Invoker("suffocatesAt")
    boolean dragonSurvival$suffocatesAt(BlockPos pos);
}
