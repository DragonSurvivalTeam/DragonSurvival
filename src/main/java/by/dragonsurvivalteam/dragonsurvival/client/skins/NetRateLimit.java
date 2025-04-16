package by.dragonsurvivalteam.dragonsurvival.client.skins;
/**
 *  Network request limit information.(If -1 appears, it means this information is unavailable.)
 *
 * @param remaining Remaining count.
 * @param limit Total request limit.
 * @param resetTime Next reset timestamp for the count. (second)
 */
public record NetRateLimit(int remaining, int limit, int resetTime) { }
