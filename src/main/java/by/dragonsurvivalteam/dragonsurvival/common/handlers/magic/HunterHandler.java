package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncHunterStacksRemoval;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.util.Color;

/**
 * Handles things related to the hunter ability (or the effect to be more precise) <br>
 * There is no check whether the player is a dragon to make this effect re-usable for other scenarios
 */
@EventBusSubscriber
public class HunterHandler { // FIXME :: disable shadows in EntityRenderDispatcher#render
    @Translation(key = "hunter_max_level", type = Translation.Type.CONFIGURATION, comments = "The current level compared to the maximum level determines how quickly stacks are gained or lost")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "hunter"}, key = "hunter_max_level")
    public static int MAX_LEVEL = 4;

    @Translation(key = "hunter_damage_per_level", type = Translation.Type.CONFIGURATION, comments = {
            "Determines the damage bonus (0.5 -> 50%) per level of the effect",
            "The damage bonus scales with the current stacks, max. amount being reached at max. stacks"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "hunter"}, key = "hunter_damage_per_level")
    public static double DAMAGE_PER_LEVEL = 0.75;

    @Translation(key = "hunter_fully_invisible", type = Translation.Type.CONFIGURATION, comments = "If enabled other players will be fully invisible at maximum hunter stacks")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "hunter"}, key = "hunter_full_invisibility")
    public static boolean FULLY_INVISIBLE;

    @Translation(key = "hunter_translucent_items_in_first_person", type = Translation.Type.CONFIGURATION, comments = "If enabled items held in first person will also appear translucent")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"effects", "hunter"}, key = "hunter_translucent_items_in_first_person")
    public static boolean TRANSLUCENT_ITEMS_IN_FIRST_PERSON = true;

    @Translation(key = "hunter_fix_translucency", type = Translation.Type.CONFIGURATION, comments = "This enables the shader features of fabulous mode which are needed for translucency to work correctly")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"effects", "hunter"}, key = "hunter_fix_translucency", /* Otherwise the game might crash */ requiresRestart = true)
    public static boolean FIX_TRANSLUCENCY = true;

    public static final int UNMODIFIED = -1;
    public static final int NON_TRANSPARENT = 1;

    // Lower values starts to just be invisible (vanilla uses ~0.15)
    public static final float MIN_ALPHA = 0.2f;

    // When first or third person held items are actually being rendered there is not enough context to determine this value
    public static float itemTranslucency = UNMODIFIED;

    @SubscribeEvent
    public static void modifyHunterStacks(final PlayerTickEvent.Post event) {
        MobEffectInstance hunterEffect = event.getEntity().getEffect(DSEffects.HUNTER);

        if (hunterEffect != null) {
            HunterData data = event.getEntity().getData(DSDataAttachments.HUNTER);
            int modification;

            if (/* Below feet*/ isHunterRelevant(event.getEntity().getBlockStateOn()) || /* Within block */ isHunterRelevant(event.getEntity().getInBlockState())) {
                // Gain more stacks per tick per amplifier level (min. of 1 and max. of max. ability level)
                modification = Math.min(MAX_LEVEL, 1 + hunterEffect.getAmplifier());
            } else {
                // Per amplifier level lose fewer stacks per tick (min. of 1 and max. of max. ability level)
                modification = Math.min(MAX_LEVEL - 1, hunterEffect.getAmplifier()) - MAX_LEVEL;
            }

            data.modifyHunterStacks(modification);
        }
    }

    @SubscribeEvent
    public static void clearCurrentTarget(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob mob && mob.getTarget() != null) {
            LivingEntity target = mob.getTarget();
            boolean maxHunterStacks = target.getExistingData(DSDataAttachments.HUNTER).map(HunterData::hasMaxHunterStacks).orElse(false);

            if (maxHunterStacks) {
                mob.setTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void removeHunterEffect(final LivingDamageEvent.Post event) {
        MobEffectInstance hunterEffect = event.getEntity().getEffect(DSEffects.HUNTER);

        if (hunterEffect != null && event.getNewDamage() > hunterEffect.getAmplifier()) {
            event.getEntity().removeEffect(DSEffects.HUNTER);
        }
    }

    @SubscribeEvent
    public static void modifyVisibility(final LivingEvent.LivingVisibilityEvent event) {
        event.getEntity().getExistingData(DSDataAttachments.HUNTER).ifPresent(data -> {
            if (data.hasHunterStacks()) {
                // Even if this is set to 0, the min. radius will be set to 4 (2x2) in TargetingConditions#test
                event.modifyVisibility(1 - (double) data.getHunterStacks() / getMaxStacks());
            }
        });
    }

    @SubscribeEvent
    public static void clearHunterStacks(final MobEffectEvent.Remove event) {
        if (event.getEffect().is(DSEffects.HUNTER)) {
            clearHunterStacks(event.getEntity());
        }
    }

    @SubscribeEvent // When an effect expires it does not trigger the 'MobEffectEvent.Remove' event
    public static void clearHunterStacks(final MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();

        if (instance != null && instance.getEffect().is(DSEffects.HUNTER)) {
            clearHunterStacks(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void avoidTarget(final LivingChangeTargetEvent event) {
        event.getEntity().getExistingData(DSDataAttachments.HUNTER).ifPresent(data -> {
            if (data.hasMaxHunterStacks()) {
                event.setNewAboutToBeSetTarget(null);
            }
        });
    }

    @SubscribeEvent
    public static void handleCriticalBonus(final CriticalHitEvent event) {
        MobEffectInstance hunterEffect = event.getEntity().getEffect(DSEffects.HUNTER);

        if (hunterEffect == null) {
            return;
        }

        HunterData data = event.getEntity().getData(DSDataAttachments.HUNTER);
        float multiplier = (float) (1 + hunterEffect.getAmplifier() * DAMAGE_PER_LEVEL);
        multiplier = multiplier * ((float) data.getHunterStacks() / getMaxStacks());

        event.setCriticalHit(true);
        event.setDamageMultiplier(event.getDamageMultiplier() + multiplier);
        event.getEntity().removeEffect(DSEffects.HUNTER);
    }

    public static int getMaxStacks() {
        return MAX_LEVEL * Functions.secondsToTicks(2);
    }

    /** Replaces (and returns) the alpha value of the packed color with the supplied alpha */
    public static int applyAlpha(float alpha, int packedColor) {
        return FastColor.ARGB32.color((int) (alpha * 255), FastColor.ARGB32.red(packedColor), FastColor.ARGB32.green(packedColor), FastColor.ARGB32.blue(packedColor));
    }

    /** Replaces (and returns) the alpha value of the packed color with the supplied alpha */
    public static int applyAlpha(int alpha, int packedColor) {
        return FastColor.ARGB32.color(alpha, FastColor.ARGB32.red(packedColor), FastColor.ARGB32.green(packedColor), FastColor.ARGB32.blue(packedColor));
    }

    /** Returns the packed color in the {@link FastColor.ARGB32#color(int, int, int, int)} format */
    public static int modifyAlpha(@Nullable final Entity entity, int packedColor) {
        if (entity == null) {
            return packedColor;
        }

        HunterData data = entity.getData(DSDataAttachments.HUNTER);

        if (!data.hasHunterStacks()) {
            return packedColor;
        }

        float alpha = calculateAlpha(data, entity == DragonSurvival.PROXY.getLocalPlayer());
        return applyAlpha(alpha, packedColor);
    }

    /** Returns the packed color in the {@link Color#ofARGB(int, int, int, int)} format */
    public static Color modifyAlpha(@Nullable final Entity entity, Color color) {
        if (entity == null) {
            return color;
        }

        HunterData data = entity.getData(DSDataAttachments.HUNTER);

        if (!data.hasHunterStacks()) {
            return color;
        }

        int packedColor = color.getColor();
        float alpha = calculateAlpha(data, entity == DragonSurvival.PROXY.getLocalPlayer());
        return Color.ofARGB((int) (alpha * 255), FastColor.ARGB32.red(packedColor), FastColor.ARGB32.green(packedColor), FastColor.ARGB32.blue(packedColor));
    }

    public static int calculateAlpha(final Entity entity) {
        return (int) (calculateAlphaAsFloat(entity) * 255);
    }

    public static float calculateAlphaAsFloat(final Entity entity) {
        return calculateAlpha(entity.getData(DSDataAttachments.HUNTER), entity == DragonSurvival.PROXY.getLocalPlayer());
    }

    private static float calculateAlpha(@NotNull final HunterData data, boolean isLocalPlayer) {
        if (!data.hasHunterStacks() || data.isBeingRenderedInInventory) {
            return 1;
        }

        float min = isLocalPlayer || FULLY_INVISIBLE ? MIN_ALPHA : 0;
        return Math.max(min, 1f - (float) data.getHunterStacks() / getMaxStacks());
    }

    private static boolean isHunterRelevant(final BlockState blockState) {
        return blockState.is(DSBlockTags.ENABLES_HUNTER_EFFECT);
    }

    private static void clearHunterStacks(final Entity entity) {
        entity.getData(DSDataAttachments.HUNTER).clearHunterStacks();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new SyncHunterStacksRemoval(entity.getId()));
    }
}
