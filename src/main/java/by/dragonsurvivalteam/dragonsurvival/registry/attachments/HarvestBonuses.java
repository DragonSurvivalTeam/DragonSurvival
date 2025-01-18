package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.HarvestBonus;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber // Only relevant for Players since the harvest events are only fired for them
public class HarvestBonuses extends Storage<HarvestBonus.Instance> {
    public float getBaseSpeed(final BlockState state) {
        if (storage == null) {
            return HarvestBonus.BASE_SPEED;
        }
        
        float baseSpeed = HarvestBonus.BASE_SPEED;

        for (HarvestBonus.Instance instance : storage.values()){
            float speed = instance.getBaseSpeed(state);

            if (speed > baseSpeed) {
                baseSpeed = speed;
            }
        }

        return baseSpeed;
    }
    
    public int getHarvestBonus(final BlockState state) {
        if (storage == null) {
            return HarvestBonus.NO_BONUS_VALUE;
        }

        int bonus = HarvestBonus.NO_BONUS_VALUE;

        for (HarvestBonus.Instance instance : storage.values()) {
            bonus += instance.getHarvestBonus(state);
        }

        return bonus;
    }

    public float getSpeedMultiplier(final BlockState state) {
        if (storage == null) {
            return 1;
        }

        float multiplier = 1;

        for (HarvestBonus.Instance instance : storage.values()) {
            multiplier += instance.getSpeedMultiplier(state);
        }

        return multiplier;
    }

    /**
     * Determines if the player can harvest the provided block state
     * @param tool @param tool The harvest level of this item will be used as a base (see {@link ToolUtils#toolToHarvestLevel(ItemStack)})
     */
    public static boolean canHarvest(final Player player, final BlockState state, final ItemStack tool) {
        int bonus = player.getExistingData(DSDataAttachments.HARVEST_BONUSES).map(data -> data.getHarvestBonus(state)).orElse(HarvestBonus.NO_BONUS_VALUE);

        if (ToolUtils.isCorrectTool(tool, state)) {
            return bonus + ToolUtils.toolToHarvestLevel(tool) >= ToolUtils.getRequiredHarvestLevel(state);
        }

        return bonus >= ToolUtils.getRequiredHarvestLevel(state);
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            player.getExistingData(DSDataAttachments.HARVEST_BONUSES).ifPresent(storage -> {
                storage.tick(event.getEntity());

                if (storage.isEmpty()) {
                    player.removeData(DSDataAttachments.HARVEST_BONUSES);
                }
            });
        }
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final HarvestBonus.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected HarvestBonus.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return HarvestBonus.Instance.load(provider, tag);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.HARVEST_BONUSES.get();
    }
}
