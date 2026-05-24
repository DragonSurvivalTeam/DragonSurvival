package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

public final class FlatCubeBakeState {
    private static final ThreadLocal<Boolean> DRAGON_SURVIVAL_MODEL_BAKE = ThreadLocal.withInitial(() -> false);

    public static void setDragonSurvivalModelBake(final boolean dragonSurvivalModelBake) {
        DRAGON_SURVIVAL_MODEL_BAKE.set(dragonSurvivalModelBake);
    }

    public static void clearDragonSurvivalModelBake() {
        DRAGON_SURVIVAL_MODEL_BAKE.remove();
    }

    public static boolean isDragonSurvivalModelBake() {
        return DRAGON_SURVIVAL_MODEL_BAKE.get();
    }
}
