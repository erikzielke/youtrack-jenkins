package org.jenkinsci.plugins.youtrack;

import hudson.Util;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import org.jenkinsci.plugins.youtrack.youtrackapi.Project;

import java.util.Arrays;
import java.util.List;

/**
 * An invisible action which only purpose is to save the short names for use in {@link YouTrackChangeLogAnnotator}
 */
public class YouTrackSaveProjectShortNamesAction extends InvisibleAction {
    private String shortNames;

    public YouTrackSaveProjectShortNamesAction(List<Project> projects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Project project : projects) {
            stringBuilder.append(project.getShortName()).append(",");
        }
        String s = stringBuilder.toString();
        if(s.endsWith(",")) {
            s = s.substring(0, s.length() -1);
        }
        shortNames = s;
    }

    public List<String> getShortNames() {
        return Arrays.asList(Util.tokenize(shortNames, ","));
    }
}
