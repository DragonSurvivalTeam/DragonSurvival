package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ManaHandling;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.util.ExperienceUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class ManaHandler {
    @SubscribeEvent
    public static void playerTick(final PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        MagicData magic = MagicData.getData(player);
        float maxMana = ManaHandler.getRawMaxMana(player);

        if (magic.getCurrentMana() > maxMana) {
            // There doesn't seem to be a good point to listen to attribute value changes
            // So we have to manually check and adjust, because otherwise current mana is not properly set
            // (Causing visual issues and reserved abilities to switch between enabled and disabled)
            magic.setCurrentMana(player, maxMana);
        }

        if (magic.isCasting()) {
            return;
        }

        if (magic.getAvailableMana() < getMaxMana(player)) {
            // FIXME :: Mana may still be out of sync by about ~0.03
            //          Potentially we have to send a sync packet every tick and let the server fully handle mana amount
            replenishMana(player, (float) player.getAttributeValue(DSAttributes.MANA_REGENERATION));
        }
    }

    public static boolean hasEnoughMana(final Player player, float manaCost) {
        if (manaCost == 0 || player.hasEffect(DSEffects.SOURCE_OF_MAGIC) || player.hasInfiniteMaterials()) {
            return true;
        }

        MagicData magic = MagicData.getData(player);

        // Prevent the usage of abilities if the mana cost would cause reserved abilities to be disabled
        // Due to losing the mana bonus from experience levels
        if (magic.getReservedMana() > 0 && magic.getAvailableMana() < manaCost) {
            ManaHandling manaHandling = DragonStateProvider.getData(player).species().value().manaHandling();

            if (manaHandling.manaXpConversion() > 0 && manaHandling.maxManaFromLevels() > 0) {
                int experienceCost = convertMana(magic.getAvailableMana() - manaCost, manaHandling.manaXpConversion());
                int newLevel = ExperienceUtils.getLevel(ExperienceUtils.getTotalExperience(player) + experienceCost);
                float manaBonus = (float) Math.min(manaHandling.maxManaFromLevels(), newLevel * manaHandling.manaPerLevel());

                if (player.getAttributeValue(DSAttributes.MANA) + manaBonus < magic.getReservedMana()) {
                    return false;
                }
            }
        }

        return MagicData.getData(player).getAvailableMana() + getManaFromExperience(player) - manaCost >= 0;
    }

    /** Returns the current maximum mana (after subtracting reserved mana) */
    public static float getMaxMana(final Player player) {
        return Math.max(0, getRawMaxMana(player) - MagicData.getData(player).getReservedMana());
    }

    /** Returns the current maximum mana */
    public static float getRawMaxMana(final Player player) {
        float mana = (float) player.getAttributeValue(DSAttributes.MANA);
        mana += getBonusManaFromExperience(player);
        return Math.max(0, mana);
    }

    public static void replenishMana(final Player player, float mana) {
        MagicData data = MagicData.getData(player);
        data.adjustMana(player, mana);
    }

    public static void consumeMana(final Player player, float manaCost) {
        if (manaCost == 0 || player == null || player.hasInfiniteMaterials() || player.hasEffect(DSEffects.SOURCE_OF_MAGIC)) {
            return;
        }

        MagicData magic = MagicData.getData(player);
        float pureMana = magic.getAvailableMana();
        ManaHandling manaHandling = DragonStateProvider.getData(player).species().value().manaHandling();

        if (manaHandling.manaXpConversion() > 0 && player.level().isClientSide()) {
            // Check if experience would be consumed as part of the mana cost
            if (pureMana < manaCost && pureMana + getManaFromExperience(player) >= manaCost) {
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.01F, 0.01F);
            }
        }

        if (manaHandling.manaXpConversion() > 0) {
            if (pureMana < manaCost) {
                float missingMana = pureMana - manaCost;
                player.giveExperiencePoints(convertMana(missingMana, manaHandling.manaXpConversion()));
                magic.setCurrentMana(player, 0);
            } else {
                magic.adjustMana(player, -manaCost);
            }
        } else {
            magic.adjustMana(player, -manaCost);
        }
    }

    public static float getBonusManaFromExperience(final Player player) {
        if (!DragonStateProvider.isDragon(player)) return 0;

        ManaHandling manaHandling = DragonStateProvider.getData(player).species().value().manaHandling();

        if (manaHandling.maxManaFromLevels() == 0) {
            return 0;
        }

        return (float) Math.min(manaHandling.maxManaFromLevels(), player.experienceLevel * manaHandling.manaPerLevel());
    }

    public static float getManaFromExperience(final Player player) {
        ManaHandling manaHandling = DragonStateProvider.getData(player).species().value().manaHandling();

        if (manaHandling.manaXpConversion() == 0) {
            return 0;
        }

        return (float) (ExperienceUtils.getTotalExperience(player) * manaHandling.manaXpConversion());
    }

    private static int convertMana(float mana, double manaXpConversion) {
        double converted = mana / manaXpConversion;

        if (converted > 0) {
            return Mth.ceil(converted);
        } else if (converted < 0) {
            return Mth.floor(converted);
        }

        return 0;
    }
}