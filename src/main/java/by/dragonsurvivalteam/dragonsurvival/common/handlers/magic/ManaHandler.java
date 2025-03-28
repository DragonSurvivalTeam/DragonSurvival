package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ManaHandling;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
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

        if (magic.isCasting()) {
            return;
        }

        if (magic.getCurrentMana() < getMaxMana(player)) {
            replenishMana(player, (float) player.getAttributeValue(DSAttributes.MANA_REGENERATION));
        }
    }

    public static boolean hasEnoughMana(final Player player, float manaCost) {
        if (player.hasEffect(DSEffects.SOURCE_OF_MAGIC) || player.hasInfiniteMaterials()) {
            return true;
        }

        float currentMana = getCurrentMana(player) + getManaFromExperience(player);

        // If we query by mana cost and the player has no mana we should consider them as not having enough
        // No mana should be a state of not being able to cast magic
        return currentMana - manaCost > 0;
    }

    public static float getMaxMana(final Player player) {
        float mana = (float) player.getAttributeValue(DSAttributes.MANA);
        mana += getBonusManaFromExperience(player);
        mana -= getReservedMana(player);

        return Math.max(0, mana);
    }

    public static void replenishMana(final Player player, float mana) {
        MagicData data = MagicData.getData(player);
        data.setCurrentMana(Math.min(getMaxMana(player), data.getCurrentMana() + mana));
    }

    public static void consumeMana(final Player player, float manaCost) {
        if (manaCost == 0 || player == null || player.hasInfiniteMaterials() || player.hasEffect(DSEffects.SOURCE_OF_MAGIC)) {
            return;
        }

        float pureMana = getCurrentMana(player);
        ManaHandling manaHandling = DragonStateProvider.getData(player).species().value().manaHandling();

        if (manaHandling.manaXpConversion() > 0 && player.level().isClientSide()) {
            // Check if experience would be consumed as part of the mana cost
            if (pureMana < manaCost && getCurrentMana(player) + getManaFromExperience(player) >= manaCost) {
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.01F, 0.01F);
            }
        }

        MagicData magic = MagicData.getData(player);

        if (manaHandling.manaXpConversion() > 0) {
            if (pureMana < manaCost) {
                float missingMana = pureMana - manaCost;
                player.giveExperiencePoints(convertMana(missingMana, manaHandling.manaXpConversion()));
                magic.setCurrentMana(0);
            } else {
                magic.setCurrentMana(pureMana - manaCost);
            }
        } else {
            magic.setCurrentMana(pureMana - manaCost);
        }
    }

    public static float getReservedMana(final Player player) {
        float reservedMana = 0;
        MagicData magic = MagicData.getData(player);

        for (DragonAbilityInstance ability : magic.getAbilities().values()) {
            if (ability.isApplyingEffects()) {
                reservedMana += ability.getContinuousManaCost(ManaCost.ManaCostType.RESERVED);
            }
        }

        return reservedMana;
    }

    public static float getCurrentMana(final Player player) {
        return Math.min(MagicData.getData(player).getCurrentMana(), getMaxMana(player));
    }

    public static float getBonusManaFromExperience(final Player player) {
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