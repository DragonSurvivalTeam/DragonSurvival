package by.dragonsurvivalteam.dragonsurvival.util;

/**
 * Handles running an action with a set cooldown.
 * <br/>
 * {@link #tryRun()} can be invoked repeatedly, and it won't invoke
 * the action until the cooldown has passed.
 */
public class ActionWithTimedCooldown {

    private final TimedCooldown timedCooldown;

    private final Runnable action;

    public ActionWithTimedCooldown(long cooldownMs, Runnable action) {
        this.timedCooldown = new TimedCooldown(cooldownMs);
        this.action = action;
    }

    public long getCooldownMs() {
        return timedCooldown.getStartValue();
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
        if (timedCooldown.isOnCooldown()) {
            return false;
        }

        forceRun();
        return true;
    }

    /**
     * Runs the action regardless of the cooldown. Starts the cooldown again.
     */
    public void forceRun() {
        action.run();
        timedCooldown.forceSet();
    }

    /**
     * Resets the next allowed run time to 0, allowing the action to be run immediately next time.
     */
    public void resetCooldown() {
        timedCooldown.reset();
    }
}
