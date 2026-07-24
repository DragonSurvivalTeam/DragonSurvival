package by.dragonsurvivalteam.dragonsurvival.util;

/**
 * Tracks a cooldown period based on a counter. Must be ticked to progress the cooldown.
 *
 * @see TimedCooldown
 */
public class TickedCooldown {
    private final long startValue;

    private long counter;

    public TickedCooldown(long startValue) {
        this.startValue = startValue;
    }

    public long getStartValue() {
        return startValue;
    }

    public long getCounter() {
        return counter;
    }

    /**
     * Tries to set the cooldown. If the cooldown is already active, this method does nothing.
     *
     * @return True if the cooldown was set. False if it was already active.
     */
    public boolean trySet() {
        if (counter > 0) {
            return false;
        }

        forceSet();
        return true;
    }

    public boolean isOnCooldown() {
        return counter > 0;
    }

    /**
     * Progresses the cooldown by one tick.
     *
     * @return True if the cooldown has just expired.
     */
    public boolean tick() {
        if (counter == 0) {
            return false;
        }
        counter--;
        return counter == 0;
    }

    /**
     * Sets the cooldown immediately.
     */
    public void forceSet() {
        counter = startValue;
    }

    /**
     * Resets the cooldown immediately.
     */
    public void reset() {
        counter = 0;
    }
}
