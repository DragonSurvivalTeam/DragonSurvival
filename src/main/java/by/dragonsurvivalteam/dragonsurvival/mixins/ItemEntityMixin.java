package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Sync the fire immune status to the client to disable the rendering of the fire texture */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements IEntityWithComplexSpawn {
    public ItemEntityMixin(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    @Override
    public void writeSpawnData(@NotNull final RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(getData(DSDataAttachments.ENTITY_HANDLER).isFireImmune);
    }

    @Override
    public void readSpawnData(@NotNull final RegistryFriendlyByteBuf buffer) {
        getData(DSDataAttachments.ENTITY_HANDLER).isFireImmune = buffer.readBoolean();
    }

    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean dragonSurvival$makeFireImmune(boolean isFireImmune) {
        return isFireImmune || getData(DSDataAttachments.ENTITY_HANDLER).isFireImmune;
    }
}
