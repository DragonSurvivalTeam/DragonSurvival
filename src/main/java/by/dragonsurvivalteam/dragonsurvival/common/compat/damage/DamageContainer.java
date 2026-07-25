package by.dragonsurvivalteam.dragonsurvival.common.compat.damage;

import net.minecraft.world.damagesource.DamageSource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Forge 1.20 compatibility implementation of NeoForge's 1.21 damage context.
 */
public class DamageContainer {
    public enum Reduction {
        INVULNERABILITY,
        ARMOR,
        ENCHANTMENTS,
        MOB_EFFECTS,
        ABSORPTION,
        INNATE_RESISTANCE
    }

    private final EnumMap<Reduction, List<IReductionFunction>> reductionFunctions = new EnumMap<>(Reduction.class);
    private final EnumMap<Reduction, Float> reductions = new EnumMap<>(Reduction.class);
    private final float originalDamage;
    private final DamageSource source;
    private float newDamage;
    private int invulnerabilityTicksAfterAttack = 20;

    public DamageContainer(final DamageSource source, final float originalDamage) {
        this.source = source;
        this.originalDamage = originalDamage;
        this.newDamage = originalDamage;
    }

    public float getOriginalDamage() {
        return originalDamage;
    }

    public DamageSource getSource() {
        return source;
    }

    public void setNewDamage(final float damage) {
        newDamage = damage;
    }

    public float getNewDamage() {
        return newDamage;
    }

    public void addModifier(final Reduction type, final IReductionFunction reductionFunction) {
        reductionFunctions.computeIfAbsent(type, ignored -> new ArrayList<>()).add(reductionFunction);
    }

    public void setPostAttackInvulnerabilityTicks(final int ticks) {
        invulnerabilityTicksAfterAttack = ticks;
    }

    public int getPostAttackInvulnerabilityTicks() {
        return invulnerabilityTicksAfterAttack;
    }

    public float getReduction(final Reduction type) {
        return reductions.getOrDefault(type, 0.0F);
    }

    public void setReduction(final Reduction type, final float amount) {
        float modifiedReduction = amount;

        for (IReductionFunction function : reductionFunctions.getOrDefault(type, List.of())) {
            modifiedReduction = function.modify(this, modifiedReduction);
        }

        reductions.put(type, modifiedReduction);
        newDamage -= modifiedReduction;
    }
}
