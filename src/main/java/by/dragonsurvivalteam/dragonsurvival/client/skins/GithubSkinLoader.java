package by.dragonsurvivalteam.dragonsurvival.client.skins;

import by.dragonsurvivalteam.dragonsurvival.util.json.GsonFactory;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

public class GithubSkinLoader implements NetSkinLoader {
    private static final String SKIN_LIST_API = "https://api.github.com/repos/DragonSurvivalTeam/DragonSurvival/git/trees/master?recursive=1";
    private static final String SKIN = "https://raw.githubusercontent.com/DragonSurvivalTeam/DragonSurvival/master/src/test/resources/";
    private static final String SKINS_PING = "https://raw.githubusercontent.com/DragonSurvivalTeam/DragonSurvival/master/README.md";

    private static final String SKIN_PATH_IN_REPO = "src/test/resources/";
    private int remaining = -1;
    private int limit = -1;

    private int resetTime = -1;

    private static class SkinFileMetaInfo {
        String content;
        String encoding;
    }

    private static class SkinResponseItem {
        String path;
        String sha;
        String url;
    }

    private static class SkinListApiResponse {
        SkinResponseItem[] tree;
    }


    protected void updateRateLimitFromRequest(HttpRequestHelper http)
    {
        http.getResponseHeaders().getOrDefault("X-RateLimit-Remaining", List.of()).stream()
                .findFirst().ifPresent(
                        s -> this.remaining = Integer.parseInt(s)
                );
        http.getResponseHeaders().getOrDefault("X-RateLimit-Limit", List.of()).stream()
                .findFirst().ifPresent(
                        s -> this.limit = Integer.parseInt(s)
                );
        http.getResponseHeaders().getOrDefault("X-RateLimit-Reset", List.of()).stream()
                .findFirst().ifPresent(
                        s -> this.resetTime = Integer.parseInt(s)
                );
    }

    @Override
    public Collection<SkinObject> querySkinList() throws IOException {
        ArrayList<SkinObject> result = new ArrayList<>();
        Gson gson = GsonFactory.getDefault();
        HttpRequestHelper http = new HttpRequestHelper();
        http.url(SKIN_LIST_API).timeout(5000);
        http.execute();
        updateRateLimitFromRequest(http);
        if (http.getResponseCode() != 200) {
            throw new NetRateLimitException(http.getUrl(), this.getRateLimit());
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getResponseBody()))) {
            SkinListApiResponse skinListResponse = gson.fromJson(reader, SkinListApiResponse.class);

            for (SkinResponseItem skinResponse : skinListResponse.tree) {
                if (skinResponse.path.startsWith(SKIN_PATH_IN_REPO)) {
                    SkinObject skinObject = new SkinObject();
                    skinObject.name = skinResponse.path.substring(SKIN_PATH_IN_REPO.length());
                    skinObject.id = skinResponse.sha;
                    skinObject.user_extra = skinResponse;
                    result.add(skinObject);
                }
            }
            return result;
        }
    }

    @Override
    public InputStream querySkinImage(SkinObject skin) throws IOException {
        Gson gson = GsonFactory.getDefault();
        SkinResponseItem skinExtra = (SkinResponseItem) skin.user_extra;

        HttpRequestHelper http = new HttpRequestHelper();
        http.url(skinExtra.url).timeout(15000);
        http.execute();
        updateRateLimitFromRequest(http);
        if (http.getResponseCode() != 200) {
            throw new NetRateLimitException(http.getUrl(), this.getRateLimit());
        }

        try (InputStream skinMetadataStream = http.getResponseBody()) {
            SkinFileMetaInfo skinMetaInfo = gson.fromJson(new BufferedReader(new InputStreamReader(skinMetadataStream)), SkinFileMetaInfo.class);
            String base64Content = skinMetaInfo.content.replace("\n", "");
            return new ByteArrayInputStream(Base64.getDecoder().decode(base64Content));
        }
    }

    @Override
    public NetRateLimit getRateLimit() {
        return new NetRateLimit(remaining,limit,resetTime);
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
        return http.getResponseCode() == 200;
    }
}
