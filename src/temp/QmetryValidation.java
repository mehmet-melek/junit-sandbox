package com.ykb.digital.pos;

import com.ykb.digital.pos.model.QmetryProject;

import java.io.IOException;

public class QmetryValidation {

    JiraService jiraService = new JiraService();

    private String qmetryProjectName;
    private String qmetryMainFolder;
    private String qmetrySubFolder;


    public void setProjectNameAndRequiredFolders() {
        String[] groupIdParts = System.getProperty("project.groupId").split("\\.");
        if (groupIdParts.length != 4) {
            throw new IllegalArgumentException("Project group id is not valid");
        }
        qmetryProjectName = groupIdParts[2].toUpperCase() + "-TM";
        qmetryMainFolder = groupIdParts[3];
        qmetrySubFolder = System.getProperty("project.artifactId");
    }

    public void validate() throws IOException, InterruptedException {

        setProjectNameAndRequiredFolders();
        jiraService.loadConfigs();
        jiraService.generateApiKey("OPEN-API", "1");
        QmetryProject qmetryProject = jiraService.searchAndGetQmetryProject(qmetryProjectName);
        jiraService.generateFolder(qmetryProject.getId(), qmetryMainFolder, qmetrySubFolder);
        jiraService.generateApiKey("AUTOMATION", String.valueOf(qmetryProject.getId()));
        jiraService.createQmetryPropertiesFile(qmetryMainFolder, qmetrySubFolder);

    }


}









