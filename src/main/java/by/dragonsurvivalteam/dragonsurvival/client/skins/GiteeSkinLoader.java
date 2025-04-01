package by.dragonsurvivalteam.dragonsurvival.client.skins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.util.json.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

public class GiteeSkinLoader extends NetSkinLoader{
    private static final String SKINS_GET_DIRECTORY_HASH = "https://gitee.com/api/v5/repos/srinater/DragonSurvival/contents/src/test?ref=master";
    private static final String SKINS_LIST_LINK = "https://gitee.com/api/v5/repos/srinater/DragonSurvival/git/trees/";
    private static final String SKINS_DOWNLOAD_LINK = "https://gitee.com/api/v5/repos/srinater/DragonSurvival/git/blobs/";
    private static final String SKINS_PING = "https://gitee.com/srinater/DragonSurvival/";
    private final Lazy<String> skinDirectoryHash = Lazy.of(this::querySkinDirectoryHash);

    private static class NetDirectoryInfo {
        String type;
        String name;
        String sha;
    }

    private static class NetSkinInfo{
        String path;
        String sha;
        String url;
    }
    private static class SkinListApiResponse{
        NetSkinInfo[] tree;
    }

    protected String querySkinDirectoryHash()
    {
        Gson gson = GsonFactory.getDefault();
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(internetGetStream(new URL(SKINS_GET_DIRECTORY_HASH), 2 * 1000)));
            Type netDirectoryInfoListType = new TypeToken<List<NetDirectoryInfo>>(){}.getType();
            List<NetDirectoryInfo> directoryInfoList = gson.fromJson(reader, netDirectoryInfoListType);
            for (NetDirectoryInfo directoryInfo : directoryInfoList)
            {
                if (directoryInfo.name.equals("resources"))
                {
                    return directoryInfo.sha;
                }
            }
        }catch (IOException exception)
        {
            return null;
        }
        return null;
    }

    @Override
    public Collection<SkinObject> querySkinList() {
        ArrayList<SkinObject> result = new ArrayList<>();
        Gson gson = GsonFactory.getDefault();
        String hash = skinDirectoryHash.get();
        if (hash.isEmpty())
            return null;
        try {
            URL url = new URL(SKINS_LIST_LINK + hash);
            BufferedReader reader = new BufferedReader(new InputStreamReader(internetGetStream(url, 2 * 1000)));
            SkinListApiResponse skinListResponse = gson.fromJson(reader, SkinListApiResponse.class);
            for (NetSkinInfo skinInfo: skinListResponse.tree)
            {
                SkinObject skinObject = new SkinObject();
                skinObject.user_extra = skinInfo;
                skinObject.name = skinInfo.path;
                skinObject.id = skinInfo.sha;
                result.add(skinObject);
            }
            reader.close();
        }catch (IOException exception) {
            DragonSurvival.LOGGER.log(Level.WARN, "Failed to get skin information in Gitcode: [{}]", exception.getMessage());
            return null;
        }
        return result;
    }

    @Override
    public InputStream querySkinImage(SkinObject skin) throws IOException {
        NetSkinInfo netSkinInfo = (NetSkinInfo)skin.user_extra;
        URL url = new URL(SKINS_DOWNLOAD_LINK + netSkinInfo.sha);
        BufferedReader reader = new BufferedReader(new InputStreamReader(internetGetStream(url, 2 * 1000)));
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        String imageContent = jsonObject.get("content").getAsString();
        return new ByteArrayInputStream(Base64.getDecoder().decode(imageContent));
    }

    @Override
    public boolean ping() {
        try (InputStream ignore = internetGetStream(new URL(SKINS_PING), 3 * 1000)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
