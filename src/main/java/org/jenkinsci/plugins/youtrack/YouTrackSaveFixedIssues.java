package org.jenkinsci.plugins.youtrack;

import hudson.Util;
import hudson.model.InvisibleAction;
import org.jenkinsci.plugins.youtrack.youtrackapi.Issue;

import java.util.Arrays;
import java.util.List;

/**
 * This action is for saving which issues that has been marked fixed by the change log messages for the build.
 */
public class YouTrackSaveFixedIssues extends InvisibleAction {
    /**
     * Comma-separated list of issue ids.
     */
    private String issueIds;

    /**
     * Constructs the action with the given set of issue ids.
     *
     * @param issues list of issues.
     */
    public YouTrackSaveFixedIssues(List<Issue> issues) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Issue issue : issues) {
            stringBuilder.append(issue.getId()).append(",");
        }
        String s = stringBuilder.toString();
        if (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
        issueIds = s;
    }

    /**
     * Converts the string of issue ids into a list.
     *
     * @return list of issue id strings.
     */
    public List<String> getIssueIds() {
        return Arrays.asList(Util.tokenize(issueIds, ","));
    }
}
