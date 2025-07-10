package by.dragonsurvivalteam.dragonsurvival.client.skins;


import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface NetSkinLoader {
    Collection<SkinObject> querySkinList() throws IOException;

    InputStream querySkinImage(SkinObject skin) throws IOException;

    NetRateLimit getRateLimit();
    boolean ping();
}
