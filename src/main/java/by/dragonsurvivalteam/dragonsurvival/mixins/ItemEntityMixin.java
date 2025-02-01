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
        getExistingData(DSDataAttachments.ITEM).ifPresentOrElse(data -> buffer.writeBoolean(data.isFireImmune), () -> buffer.writeBoolean(false));
    }

    @Override
    public void readSpawnData(@NotNull final RegistryFriendlyByteBuf buffer) {
        if (/* Fire immune */ !buffer.readBoolean()) {
            return;
        }

        getData(DSDataAttachments.ITEM).isFireImmune = true;
    }

    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean dragonSurvival$makeFireImmune(boolean isFireImmune) {
        return isFireImmune || getExistingData(DSDataAttachments.ITEM).map(data -> data.isFireImmune).orElse(false);
    }
}
