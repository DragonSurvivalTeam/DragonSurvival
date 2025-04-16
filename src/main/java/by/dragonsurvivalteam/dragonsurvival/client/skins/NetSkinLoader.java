package by.dragonsurvivalteam.dragonsurvival.client.skins;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface NetSkinLoader {
    Collection<SkinObject> querySkinList() throws IOException;

    InputStream querySkinImage(SkinObject skin) throws IOException;

    NetRateLimit getRateLimit();
    boolean ping();
}
