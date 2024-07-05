package by.dragonsurvivalteam.dragonsurvival.client.skins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.util.GsonFactory;
import com.google.gson.Gson;
import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class GitcodeSkinLoader extends NetSkinLoader {
    private static final String SKINS_LIST_LINK = "https://web-api.gitcode.com/api/v1/projects/mirrors%2FDragonSurvivalTeam%2FDragonSurvival/repository/tree?ref=master&path=src/test/resources&per_page=100&page=";
    private static final String SKINS_DOWNLOAD_LINK = "https://web-api.gitcode.com/api/v1/projects/mirrors%%2FDragonSurvivalTeam%%2FDragonSurvival/repository/blobs/%s/raw?ref=master&file_name=%s";
    private static final String SKINS_PING = "https://web-api.gitcode.com/api/v1/projects/mirrors%2FDragonSurvivalTeam%2FDragonSurvival/repository/tree?ref=master&path=src/test&per_page=100&page=1";
    private static final HashMap<String, String> GITCODE_HEADER = new HashMap<>(){{
        put("referer", "https://gitcode.com/");
    }};
    private static class SkinListApiResponse {
        int page_num;
        int page_size;
        int total;
        int page_count;
        SkinObject[] content;
    }
    @Override
    public Collection<SkinObject> querySkinList() {
        ArrayList<SkinObject> result = new ArrayList<>();
        int page = 1;
        try{
            while(true){
                Gson gson = GsonFactory.getDefault();
                URL url = new URL(SKINS_LIST_LINK + page);

                BufferedReader reader = new BufferedReader(new InputStreamReader(internetGetStream(url, GITCODE_HEADER, 2*1000)));
                SkinListApiResponse skinListResponse = gson.fromJson(reader, SkinListApiResponse.class);
                reader.close();
                if (skinListResponse.content.length == 0)
                    break;
                result.addAll(Arrays.asList(skinListResponse.content));
                ++page;
            }
            return result;
        }catch(IOException e){
            DragonSurvivalMod.LOGGER.log(Level.WARN, "Failed to get skin information in Gitcode.");
            return null;
        }
    }
    @Override
    public InputStream querySkinImage(SkinObject skin) throws IOException{
        return internetGetStream(new URL(String.format(SKINS_DOWNLOAD_LINK, skin.id, skin.name)), GITCODE_HEADER, 15 * 1000);
    }

    @Override
    public boolean ping() {
        try(InputStream ignore = internetGetStream(new URL(SKINS_PING), GITCODE_HEADER, 3 * 1000))
        {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
