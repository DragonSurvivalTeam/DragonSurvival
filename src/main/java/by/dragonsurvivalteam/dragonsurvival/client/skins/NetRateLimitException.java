package by.dragonsurvivalteam.dragonsurvival.client.skins;

import java.io.IOException;

public class NetRateLimitException extends IOException {
    private final String url;
    private final NetRateLimit rateLimit;
    public NetRateLimitException(String url, NetRateLimit rateLimit) {
        super(String.format("%s request failed due to rate limit exceeded: %d/%d", url, rateLimit.remaining(), rateLimit.limit()));
        this.url = url;
        this.rateLimit = rateLimit;
    }

    public NetRateLimitException(String url, NetRateLimit rateLimit, Throwable cause) {
        super(String.format("%s request failed due to rate limit exceeded: %d/%d", url, rateLimit.remaining(), rateLimit.limit()), cause);
        this.url = url;
        this.rateLimit = rateLimit;
    }

    public String getUrl() {
        return url;
    }

    public NetRateLimit getRateLimit() {
        return rateLimit;
    }
}
