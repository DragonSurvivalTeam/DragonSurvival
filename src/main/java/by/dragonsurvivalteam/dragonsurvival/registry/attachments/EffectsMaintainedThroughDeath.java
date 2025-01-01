package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class EffectsMaintainedThroughDeath implements INBTSerializable<CompoundTag> {
    private final List<MobEffectInstance> effectsToReapplyOnDeath = new ArrayList<>();

    public static EffectsMaintainedThroughDeath getData(Player player) {
        return player.getData(DSDataAttachments.EFFECTS_MAINTAINED_THROUGH_DEATH);
    }

    public void addEffect(MobEffectInstance effect) {
        effectsToReapplyOnDeath.add(effect);
    }

    public List<MobEffectInstance> getEffectsToReapplyOnDeath() {
        return effectsToReapplyOnDeath;
    }

    @SubscribeEvent
    public static void reapplyEffectsOnRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        EffectsMaintainedThroughDeath effectsMaintainedThroughDeath = getData(player);

        for(MobEffectInstance effect : effectsMaintainedThroughDeath.getEffectsToReapplyOnDeath()) {
            player.addEffect(effect);
        }

        effectsMaintainedThroughDeath.getEffectsToReapplyOnDeath().clear();
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        for(int i = 0; i < effectsToReapplyOnDeath.size(); i++) {
            tag.put("effect" + i, effectsToReapplyOnDeath.get(i).save());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag nbt) {
        effectsToReapplyOnDeath.clear();
        for(int i = 0; i < nbt.size(); i++) {
            effectsToReapplyOnDeath.add(MobEffectInstance.load(nbt.getCompound("effect" + i)));
        }
    }
}
