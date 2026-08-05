package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleUpdateAttributes", at = @At("TAIL"))
    private void dragonSurvival$refreshDragonDimensions(final ClientboundUpdateAttributesPacket packet, final CallbackInfo callback) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null || !(level.getEntity(packet.getEntityId()) instanceof Player player) || !DragonStateProvider.isDragon(player)) {
            return;
        }

        DSModifiers.restoreSyncedGrowthModifierNames(player, DragonStateProvider.getData(player));
        boolean scaleChanged = packet.getValues().stream().anyMatch(snapshot -> snapshot.getAttribute() == DSAttributes.SCALE.get());

        if (scaleChanged) {
            player.refreshDimensions();
        }
    }
}
