package by.dragonsurvivalteam.dragonsurvival.util;

/**
 * Handles running an action with a set cooldown.
 * <br/>
 * {@link #tryRun()} can be invoked repeatedly, and it won't invoke
 * the action until the cooldown has passed.
 */
public class ActionWithCooldown {

    private final long cooldownMs;
    private final Runnable action;

    /**
     * The time at which the action can be run again
     */
    private long nextAllowedRunMs = 0;

    public ActionWithCooldown(long cooldownMs, Runnable action) {
        this.cooldownMs = cooldownMs;
        this.action = action;
    }

    public long getCooldownMs() {
        return cooldownMs;
    }

    public Runnable getAction() {
        return action;
    }

    /**
     * Runs the action if the cooldown has passed. Starts the cooldown again upon success.
     *
     * @return True if the action was run, false if the cooldown hasn't passed yet.
     */
    public boolean tryRun() {
        if (System.currentTimeMillis() < nextAllowedRunMs) return false;

        forceRun();
        return true;
    }

    /**
     * Runs the action regardless of the cooldown. Starts the cooldown again.
     */
    public void forceRun() {
        action.run();
        nextAllowedRunMs = System.currentTimeMillis() + cooldownMs;
    }

    /**
     * Resets the next allowed run time to 0, allowing the action to be run immediately next time.
     */
    public void resetCooldown() {
        nextAllowedRunMs = 0;
    }
}
