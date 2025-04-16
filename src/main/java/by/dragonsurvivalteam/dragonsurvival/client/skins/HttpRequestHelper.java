package by.dragonsurvivalteam.dragonsurvival.client.skins;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestHelper {
    private String method = "GET";
    private String url;
    private HashMap<String, String> headers = new HashMap<>();
    private int timeout = 3000;
    private int responseCode;
    private InputStream responseStream;
    private Map<String, List<String>> responseHeaders;

    public HttpRequestHelper url(String url) {
        this.url = url;
        return this;
    }

    public HttpRequestHelper method(String method) {
        this.method = method.toUpperCase();
        return this;
    }

    public HttpRequestHelper header(String key, String value) {
        headers.put(key, value);
        return this;
    }
    public HttpRequestHelper headers(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequestHelper timeout(int ms) {
        this.timeout = ms;
        return this;
    }

    public void execute() throws IOException {
        URL u = URI.create(url).toURL();
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            conn.setRequestProperty(entry.getKey(), entry.getValue());
        }

        responseCode = conn.getResponseCode();
        responseHeaders = conn.getHeaderFields();

        responseStream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
    }
    public String getUrl() { return url; }
    public int getResponseCode() { return responseCode; }
    public InputStream getResponseBody() { return responseStream; }
    public Map<String, List<String>> getResponseHeaders() { return responseHeaders; }
}

