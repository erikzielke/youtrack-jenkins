package org.jenkinsci.plugins.youtrack;

import hudson.Util;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import org.jenkinsci.plugins.youtrack.youtrackapi.Project;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Erik
 * Date: 05-03-13
 * Time: 19:11
 * To change this template use File | Settings | File Templates.
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
