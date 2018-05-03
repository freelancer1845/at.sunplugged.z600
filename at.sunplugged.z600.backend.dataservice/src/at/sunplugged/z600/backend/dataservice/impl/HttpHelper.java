package at.sunplugged.z600.backend.dataservice.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.osgi.service.log.LogService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.sunplugged.z600.backend.dataservice.impl.model.TargetMaterial;
import at.sunplugged.z600.backend.dataservice.impl.model.Z600Setting;
import at.sunplugged.z600.common.settings.api.NetworkComIds;

public class HttpHelper {

    private static final String API_READY = "/api/ready";

    private static final String API_SETTINGS_GET_ALL = "/settings/api/getall";

    private static final String API_SETTINGS_SAVE_ALL = "/settings/api/saveall";

    private static final String API_TARGET_MATERIALS_ALL = "/targets/api";

    private static final String API_TARGET_MATERIALS_ADD_WORK = "targets/api/addWork";

    public static boolean checkIfHttpServerIsRunning(HttpClient client) {
        String server_url = getServerUrl();

        HttpGet get = new HttpGet(server_url + API_READY);

        HttpResponse response;
        try {
            response = client.execute(get);

            assertResponseCodeOK(response);

            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String value = rd.readLine();
            return Boolean.valueOf(value);

        } catch (IOException e) {
            // Expected behavior if http server is not reachable
            return false;
        }
    }

    public static List<Z600Setting> getAllSettings(HttpClient client) throws ClientProtocolException, IOException {

        HttpGet get = new HttpGet(getServerUrl() + API_SETTINGS_GET_ALL);
        HttpResponse response;
        response = client.execute(get);
        assertResponseCodeOK(response);
        ObjectMapper mapper = new ObjectMapper();
        Z600Setting[] settingsArray = mapper.readValue(response.getEntity().getContent(), Z600Setting[].class);
        List<Z600Setting> settingsList = new ArrayList<>(Arrays.asList(settingsArray));
        return settingsList;
    }

    public static void saveSettings(HttpClient client, List<Z600Setting> settingsToSave) throws IOException {
        HttpPost put = new HttpPost(getServerUrl() + API_SETTINGS_SAVE_ALL);

        ObjectMapper mapper = new ObjectMapper();

        try {
            String strValue = mapper.writeValueAsString(settingsToSave);

            try {
                put.setEntity(new StringEntity(strValue, ContentType.APPLICATION_JSON));

                assertResponseCodeOK(client.execute(put));

            } catch (UnsupportedEncodingException e) {
                DataServiceImpl.getLogService().log(LogService.LOG_ERROR, "Ecnoding Error.", e);
            }

        } catch (JsonProcessingException e) {
            DataServiceImpl.getLogService().log(LogService.LOG_ERROR, "Json Parse Error", e);
        }
    }

    public static String[] getTargetMaterials(HttpClient client) throws IOException {
        HttpGet get = new HttpGet(getServerUrl() + API_TARGET_MATERIALS_ALL);
        HttpResponse response = client.execute(get);
        assertResponseCodeOK(response);

        ObjectMapper mapper = new ObjectMapper();
        TargetMaterial[] materials = mapper.readValue(response.getEntity().getContent(), TargetMaterial[].class);
        return (String[]) Arrays.stream(materials).map(mat -> mat.getName()).toArray(String[]::new);
    }

    public static void addWorkToTarget(HttpClient client, String targetName, Double workToAdd) throws IOException {
        HttpPost post = new HttpPost(getServerUrl() + API_TARGET_MATERIALS_ADD_WORK);

        post.setEntity(new StringEntity(String.format("{\"name\":\"%s\",\"work\":%f}", targetName, workToAdd),
                ContentType.APPLICATION_JSON));

        HttpResponse response = client.execute(post);
        assertResponseCodeOK(response);

    }

    private static String getServerUrl() {
        return DataServiceImpl.getSettingsServce().getProperty(NetworkComIds.HTTP_BASE_SERVER_URL);
    }

    private static void assertResponseCodeOK(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

            LogService log = DataServiceImpl.getLogService();

            if (log != null) {
                log.log(LogService.LOG_WARNING,
                        "Http Resonpse code not ok. Code: " + response.getStatusLine().getStatusCode());
            } else {
                System.err.println("Http Resonpse code not ok. Code: " + response.getStatusLine().getStatusCode());
            }
        }
    }
}
