package by.dragonsurvivalteam.dragonsurvival.client.skins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.util.json.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.neoforged.neoforge.common.util.Lazy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class GiteeSkinLoader implements NetSkinLoader {
    private static final String SKINS_GET_DIRECTORY_HASH = "https://gitee.com/api/v5/repos/srinater/DragonSurvival/contents/src/test?ref=master";
    private static final String SKINS_LIST_LINK = "https://gitee.com/api/v5/repos/srinater/DragonSurvival/git/trees/";
    private static final String SKINS_DOWNLOAD_LINK = "https://gitee.com/api/v5/repos/srinater/DragonSurvival/git/blobs/";
    private static final String SKINS_PING = "https://gitee.com/srinater/DragonSurvival/";
    private final Lazy<String> skinDirectoryHash = Lazy.of(this::querySkinDirectoryHash);
    private int remaining = -1;
    private int limit = -1;

    private static class NetDirectoryInfo {
        String type;
        String name;
        String sha;
    }

    private static class NetSkinInfo {
        String path;
        String sha;
        String url;
    }

    private static class SkinListApiResponse {
        NetSkinInfo[] tree;
    }

    protected void updateRateLimitFromRequest(HttpRequestHelper http)
    {
        if (http.getResponseCode() == 403){
            this.remaining = 0;
            this.limit = 60;
            return;
        }
        http.getResponseHeaders().getOrDefault("X-RateLimit-Remaining", List.of()).stream()
                .findFirst().ifPresent(
                        s -> this.remaining = Integer.parseInt(s)
                );
        http.getResponseHeaders().getOrDefault("X-RateLimit-Limit", List.of()).stream()
                .findFirst().ifPresent(
                        s -> this.limit = Integer.parseInt(s)
                );
    }

    protected String querySkinDirectoryHash() {
        Gson gson = GsonFactory.getDefault();

        try {
            HttpRequestHelper http = new HttpRequestHelper();
            http.url(SKINS_GET_DIRECTORY_HASH).timeout(5000);
            http.execute();
            updateRateLimitFromRequest(http);
            if (http.getResponseCode() != 200)
                return "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(http.getResponseBody()));
            Type netDirectoryInfoListType = new TypeToken<List<NetDirectoryInfo>>() {}.getType();
            List<NetDirectoryInfo> directoryInfoList = gson.fromJson(reader, netDirectoryInfoListType);
            for (NetDirectoryInfo directoryInfo : directoryInfoList) {
                if (directoryInfo.name.equals("resources")) {
                    return directoryInfo.sha;
                }
            }
        } catch (IOException exception) {
            return "";
        }

        return "";
    }

    @Override
    public Collection<SkinObject> querySkinList() throws IOException {
        ArrayList<SkinObject> result = new ArrayList<>();
        Gson gson = GsonFactory.getDefault();
        String hash = skinDirectoryHash.get();

        if (hash.isEmpty()) {
            DragonSurvival.LOGGER.warn("Failed to fetch remote skin repository directory hash.");
            return null;
        }
        HttpRequestHelper http = new HttpRequestHelper();
        http.url(SKINS_LIST_LINK + hash).timeout(5000);
        http.execute();
        updateRateLimitFromRequest(http);
        if (http.getResponseCode() != 200){
            throw new NetRateLimitException(http.getUrl(), this.getRateLimit());
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(http.getResponseBody()));
        SkinListApiResponse skinListResponse = gson.fromJson(reader, SkinListApiResponse.class);

        for (NetSkinInfo skinInfo : skinListResponse.tree) {
            SkinObject skinObject = new SkinObject();
            skinObject.user_extra = skinInfo;
            skinObject.name = skinInfo.path;
            skinObject.id = skinInfo.sha;
            result.add(skinObject);
        }
        reader.close();
        return result;
    }

    @Override
    public InputStream querySkinImage(SkinObject skin) throws IOException {
        NetSkinInfo netSkinInfo = (NetSkinInfo) skin.user_extra;
        HttpRequestHelper http = new HttpRequestHelper();
        http.url(SKINS_DOWNLOAD_LINK + netSkinInfo.sha).timeout(5000);
        http.execute();
        updateRateLimitFromRequest(http);
        if (http.getResponseCode() != 200){
            throw new NetRateLimitException(http.getUrl(), this.getRateLimit());
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(http.getResponseBody()));
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        String imageContent = jsonObject.get("content").getAsString();
        return new ByteArrayInputStream(Base64.getDecoder().decode(imageContent));
    }

    @Override
    public NetRateLimit getRateLimit() {
        return new NetRateLimit(remaining,limit,-1);
    }

    @Override
    public boolean ping() {
        HttpRequestHelper http = new HttpRequestHelper();
        http.url(SKINS_PING).timeout(3000);
        try {
            http.execute();
        } catch (IOException e) {
            return false;
        }
        updateRateLimitFromRequest(http);
        return http.getResponseCode() == 200;
    }
}
