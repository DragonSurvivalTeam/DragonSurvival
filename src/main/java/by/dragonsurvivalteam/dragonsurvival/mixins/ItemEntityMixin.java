package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Sync the fire immune status to the client to disable the rendering of the fire texture */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements IEntityAdditionalSpawnData {
    public ItemEntityMixin(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    @Override
    public void writeSpawnData(@NotNull final FriendlyByteBuf buffer) {
        AttachmentManager.getExistingData(this, DSDataAttachments.ITEM).ifPresentOrElse(data -> buffer.writeBoolean(data.isFireImmune), () -> buffer.writeBoolean(false));
    }

    @Override
    public void readSpawnData(@NotNull final FriendlyByteBuf buffer) {
        if (/* Fire immune */ !buffer.readBoolean()) {
            return;
        }

        AttachmentManager.getData(this, DSDataAttachments.ITEM).isFireImmune = true;
    }

    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean dragonSurvival$makeFireImmune(boolean isFireImmune) {
        return isFireImmune || AttachmentManager.getExistingData(this, DSDataAttachments.ITEM).map(data -> data.isFireImmune).orElse(false);
    }
}
