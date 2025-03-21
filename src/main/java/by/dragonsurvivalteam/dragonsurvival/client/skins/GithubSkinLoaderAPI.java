package by.dragonsurvivalteam.dragonsurvival.client.skins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.util.json.GsonFactory;
import com.google.gson.Gson;
import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

public class GithubSkinLoaderAPI extends NetSkinLoader {
    public static final String SKINS_LIST_LINK = "https://api.github.com/repos/DragonSurvivalTeam/DragonSurvival/git/trees/30268eb7e0b3ae65e803b0c71437b6a1f85901ff?ref=master";
    private static final String SKINS_PING = "https://raw.githubusercontent.com/DragonSurvivalTeam/DragonSurvival/master/README.md";

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

    @Override
    public Collection<SkinObject> querySkinList() {
        ArrayList<SkinObject> result = new ArrayList<>();

        try {
            Gson gson = GsonFactory.getDefault();
            URL url = new URL(SKINS_LIST_LINK);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(internetGetStream(url, 2 * 1000)))) {
                SkinListApiResponse skinListResponse = gson.fromJson(reader, SkinListApiResponse.class);
                for (SkinResponseItem skinResponse : skinListResponse.tree) {
                    SkinObject skinObject = new SkinObject();
                    skinObject.name = skinResponse.path;
                    skinObject.id = skinResponse.sha;
                    skinObject.user_extra = skinResponse;
                    result.add(skinObject);
                }
                return result;
            } catch (IOException exception) {
                DragonSurvival.LOGGER.warn("Reader could not be closed", exception);
            }
        } catch (IOException exception) {
            DragonSurvival.LOGGER.log(Level.WARN, "Failed to get skin information in Github: [{}]", exception.getMessage());
        }

        return null;
    }

    @Override
    public InputStream querySkinImage(SkinObject skin) throws IOException {
        Gson gson = GsonFactory.getDefault();
        SkinResponseItem skinExtra = (SkinResponseItem) skin.user_extra;
        try (InputStream skinMetadataStream = internetGetStream(new URL(skinExtra.url), 15 * 1000)) {
            SkinFileMetaInfo skinMetaInfo = gson.fromJson(new BufferedReader(new InputStreamReader(skinMetadataStream)), SkinFileMetaInfo.class);
            String base64Content = skinMetaInfo.content.replace("\n", "");
            return new ByteArrayInputStream(Base64.getDecoder().decode(base64Content));
        }
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
