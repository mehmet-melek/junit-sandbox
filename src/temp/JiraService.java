package com.ykb.digital.pos;

import com.ykb.digital.pos.model.QmetryProject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class JiraService {

    private Properties props = new Properties();
    private String openApiKey;
    private String automationApiKey;


    public void loadConfigs() {
        try {
            props.load(QmetryValidation.class.getResourceAsStream("/config.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateApiKey(String purpose, String projectId) throws IOException, InterruptedException {

        if (purpose.equals("OPEN-API")) {
            projectId = props.getProperty("qmetry.base.projectId");
        }

        JSONObject json = new JSONObject();
        json.put("locale", "en_US");
        json.put("timezone", "PST");
        json.put("apikeypurpose", purpose);
        json.put("projectid", projectId);

        // Create HttpClient and HttpRequest
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getProperty("jira.qmetry.url") + "/api/v1/openapikeys"))
                .header("Authorization", props.getProperty("jira.auth"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        // Send request and get response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check response code and extract key from response body
        if (response.statusCode() == 200) {
            JSONObject responseBody = new JSONObject(response.body());
            if (purpose.equals("AUTOMATION")) {
                automationApiKey = responseBody.getString("key");
            } else {
                openApiKey = responseBody.getString("key");
            }
        } else {
            throw new RuntimeException("Failed to generate API key: " + response.body());
        }
    }

    public QmetryProject searchAndGetQmetryProject(String projectName) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getProperty("jira.qmetry.url")))
                .header("Authorization", props.getProperty("jira.auth"))
                .header("Content-Type", "application/json")
                .header("apiKey", openApiKey)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject responseBody = new JSONObject(response.body());
            JSONArray data = responseBody.getJSONArray("data");
            QmetryProject qmetryProject = new QmetryProject();
            for (int i = 0; i < data.length(); i++) {
                JSONObject projectJson = data.getJSONObject(i);
                if (projectJson.getString("name").equals(projectName)) {
                    qmetryProject.setId(projectJson.getLong("id"));
                    qmetryProject.setName(projectJson.getString("name"));
                    qmetryProject.setKey(projectJson.getString("key"));
                    break;
                }
            }
            return qmetryProject;
        } else {
            throw new RuntimeException("Failed to get Qmetry projects: " + projectName + " not found");
        }
    }

    public void generateFolder(Long qmetryProjectId, String qmetryMainFolderName, String qmetrySubFolderName) throws IOException, InterruptedException {
        Long parentId = findOrCreateFolder(qmetryProjectId, qmetryMainFolderName, -1L);
        findOrCreateFolder(qmetryProjectId, qmetrySubFolderName, parentId);
    }

    public Long findOrCreateFolder(Long qmetryProjectId, String folderName, Long parentId) throws IOException, InterruptedException {
        // Search folder
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getProperty("jira.qmetry.url") + qmetryProjectId + "/testcase-folders/search/" + folderName))
                .header("Authorization", props.getProperty("jira.auth"))
                .header("Content-Type", "application/json")
                .header("apiKey", openApiKey)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JSONArray responseBody = new JSONArray(response.body());
            if (responseBody.length() > 0) {
                JSONObject firstFolder = responseBody.getJSONObject(0);
                return firstFolder.getLong("id");
            } else {
                return createFolder(qmetryProjectId, folderName, parentId);
            }
        } else {
            throw new RuntimeException("Failed to search folders: " + response.body());
        }
    }

    private Long createFolder(Long qmetryProjectId, String folderName, Long parentId) throws IOException, InterruptedException {
        JSONObject json = new JSONObject();
        json.put("folderName", folderName);
        json.put("parentId", parentId);
        json.put("description", "description");


        // Create HttpClient and HttpRequest
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(props.getProperty("jira.qmetry.url") + qmetryProjectId + "/api/v1/testcase-folders"))
                .header("Authorization", props.getProperty("jira.auth"))
                .header("Content-Type", "application/json")
                .header("apiKey", openApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        // Send request and get response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check response code and extract key from response body
        if (response.statusCode() == 201) {
            JSONObject responseBody = new JSONObject(response.body());
            return responseBody.getLong("id");
        } else {
            throw new RuntimeException("Failed to create folder: " + folderName + response.body());
        }

    }


    public void createQmetryPropertiesFile(String qmetryMainFolder, String qmetrySubFolder) {
        try {
            // Load the existing qmetry.properties file from resources
            Properties qmetryProps = new Properties();
            qmetryProps.load(getClass().getResourceAsStream("/qmetry.properties"));

            // Update properties with new values
            qmetryProps.setProperty("automation.qmetry.apikey", automationApiKey);
            qmetryProps.setProperty("automation.qmetry.testcase.customFields", qmetryMainFolder);
            qmetryProps.setProperty("automation.qmetry.testcycle.customFields", qmetryMainFolder);
            qmetryProps.setProperty("automation.qmetry.testcase.folderPath", qmetryMainFolder);
            qmetryProps.setProperty("automation.qmetry.testcycle.folderPath", qmetryMainFolder);

            // Write the updated properties to a new file in the project's root directory
            try (FileOutputStream out = new FileOutputStream("qmetry.properties")) {
                qmetryProps.store(out, null);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create qmetry.properties file", e);
        }
    }
}
