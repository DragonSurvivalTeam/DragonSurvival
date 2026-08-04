package by.dragonsurvivalteam.dragonsurvival.mixins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientboundLoginPacket.class)
public class ClientboundLoginPacketMixin {
    // FIXME :: 1.21.1 backport -> need to check what mojang does with trims (datapack registry?) and why ours don't work
    //          using a full registry to encode causes issues since the client does not have access to tags (yet?)
    //          OnDatapackSyncEvent manual sync + no network codec causing automatic login sync?
//    @ModifyArg(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readWithCodec(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;)Ljava/lang/Object;"))
//    private static DynamicOps<Tag> modifyRegistryOps(final DynamicOps<Tag> pOps) {
//        // FIXME :: Client handling -> there doesn't seem to be any registry we could use that has more info
//        return pOps;
//    }
//
//    // FIXME :: 1.21.1 backport -> does not fix the connection issue yet due to missing tags
//    @Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeWithCodec(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/lang/Object;)V"))
//    private <T> void dragonSurvival$useFullRegistryOps(final FriendlyByteBuf buffer, final DynamicOps<Tag> ops, final Codec<T> codec, final T value) {
//        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
//
//        if (server != null && value instanceof RegistryAccess.Frozen access) {
//            // 'BUILTIN_CONTEXT_OPS' does not contain the operations to decode / encode datapack registries (?)
//            // Having to do this likely means we are setting something up incorrectly in terms of our datapack registries
//            RegistryOps<Tag> serverOps = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess());
//            //noinspection deprecation,unchecked -> ignore / cast is safe
//            buffer.writeWithCodec(serverOps, (Codec<RegistryAccess.Frozen>) codec, access);
//        } else {
//            //noinspection deprecation -> ignore
//            buffer.writeWithCodec(ops, codec, value);
//        }
//    }
}