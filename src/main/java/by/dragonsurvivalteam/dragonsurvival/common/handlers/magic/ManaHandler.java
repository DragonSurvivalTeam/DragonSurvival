package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMana;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.util.ExperienceUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class ManaHandler {
    /**
     * Ratio for converting experience to mana and vice versa <br> <br>
     * Level  5:   55 experience points <br>
     * Level 10:  160 experience points <br>
     * Level 20:  550 experience points <br>
     * Level 30: 1395 experience points <br>
     */
    private static final float EXPERIENCE_TO_MANA = 10;
    private static final float LEVELS_TO_MANA = 4;

    public static final int MAX_MANA_FROM_ABILITY = 10;
    private static final int MAX_MANA_FROM_LEVELS = 9;

    @SubscribeEvent
    public static void playerTick(final PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        MagicData magic = MagicData.getData(player);

        if (magic.getCurrentlyCasting() != null) {
            return;
        }

        if (magic.getCurrentMana() < getMaxMana(player)) {
            replenishMana(player, (float) player.getAttributeValue(DSAttributes.MANA_REGENERATION));
        }
    }

    public static boolean hasEnoughMana(final Player player, float manaCost) {
        if (manaCost == 0 || player.hasEffect(DSEffects.SOURCE_OF_MAGIC) || player.isCreative()) {
            return true;
        }

        float currentMana = getCurrentMana(player);

        if (ServerConfig.consumeExperienceAsMana) {
            currentMana += getManaFromExperience(player);
        }

        return currentMana - manaCost >= 0;
    }

    public static float getMaxMana(final Player player) {
        float mana = (float) player.getAttributeValue(DSAttributes.MANA);
        mana += getBonusManaFromExperience(player);

        return Math.max(0, mana);
    }

    public static void replenishMana(final Player player, float mana) {
        if (player.level().isClientSide()) {
            return;
        }

        MagicData data = MagicData.getData(player);

        if (getCurrentMana(player) == getMaxMana(player)) {
            return;
        }

        data.setCurrentMana(data.getCurrentMana() + mana);

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncMana(player.getId(), data.getCurrentMana()));
        }
    }

    public static void consumeMana(final Player player, float manaCost) {
        if (manaCost == 0 || player == null || player.isCreative() || player.hasEffect(DSEffects.SOURCE_OF_MAGIC)) {
            return;
        }

        float pureMana = getCurrentMana(player);

        if (ServerConfig.consumeExperienceAsMana && player.level().isClientSide()) {
            // Check if experience would be consumed as part of the mana cost
            if (pureMana < manaCost && getCurrentMana(player) + getManaFromExperience(player) >= manaCost) {
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.01F, 0.01F);
            }
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MagicData magic = MagicData.getData(serverPlayer);

        if (ServerConfig.consumeExperienceAsMana) {
            if (pureMana < manaCost) {
                float missingMana = pureMana - manaCost;
                player.giveExperiencePoints(convertMana(missingMana));
                magic.setCurrentMana(0);
            } else {
                magic.setCurrentMana(pureMana - manaCost);
            }
        } else {
            magic.setCurrentMana(pureMana - manaCost);
        }

        PacketDistributor.sendToPlayer(serverPlayer, new SyncMana(player.getId(), magic.getCurrentMana()));
    }

    public static float getCurrentMana(Player player) {
        return Math.min(MagicData.getData(player).getCurrentMana(), getMaxMana(player));
    }

    public static float getBonusManaFromExperience(final Player player) {
        return Math.min(MAX_MANA_FROM_LEVELS, convertLevels(player.experienceLevel));
    }

    public static float getManaFromExperience(final Player player) {
        return convertExperience(ExperienceUtils.getTotalExperience(player));
    }

    /** Convert experience points to mana based on the {@link ManaHandler#LEVELS_TO_MANA} ratio */
    private static float convertLevels(int levels) {
        return levels / LEVELS_TO_MANA;
    }

    /** Convert experience points to mana based on the {@link ManaHandler#EXPERIENCE_TO_MANA} ratio */
    private static float convertExperience(int experience) {
        return experience / EXPERIENCE_TO_MANA;
    }

    /** Convert mana to experience points based on the {@link ManaHandler#EXPERIENCE_TO_MANA} ratio */
    private static int convertMana(float mana) {
        float converted = mana * EXPERIENCE_TO_MANA;

        if (converted > 0) {
            return Mth.ceil(converted);
        } else if (converted < 0) {
            return Mth.floor(converted);
        }

        return 0;
    }
}