package org.jenkinsci.plugins.youtrack;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.listeners.SCMListener;
import hudson.scm.ChangeLogSet;
import org.jenkinsci.plugins.youtrack.youtrackapi.Issue;
import org.jenkinsci.plugins.youtrack.youtrackapi.Project;
import org.jenkinsci.plugins.youtrack.youtrackapi.User;
import org.jenkinsci.plugins.youtrack.youtrackapi.YouTrackServer;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTrackSCMListener extends SCMListener {

    @Override
    public void onChangeLogParsed(AbstractBuild<?, ?> build, BuildListener listener, ChangeLogSet<?> changelog) throws Exception {

        YouTrackSite youTrackSite = YouTrackSite.get(build.getProject());
        if (!youTrackSite.isPluginEnabled()) {
            return;
        }

        Iterator<? extends ChangeLogSet.Entry> changeLogIterator = changelog.iterator();

        YouTrackServer youTrackServer = new YouTrackServer(youTrackSite.getUrl());
        User user = youTrackServer.login(youTrackSite.getUsername(), youTrackSite.getPassword());
        List<Project> projects = youTrackServer.getProjects(user);


        while (changeLogIterator.hasNext()) {
            ChangeLogSet.Entry next = changeLogIterator.next();
            String msg = next.getMsg();

            if (youTrackSite.isCommentEnabled()) {
                for (Project project1 : projects) {
                    String shortName = project1.getShortName();
                    Pattern projectPattern = Pattern.compile("(" + shortName + "-" + "(\\d+)" + ")");
                    Matcher matcher = projectPattern.matcher(msg);
                    while (matcher.find()) {
                        if (matcher.groupCount() >= 1) {
                            String issueId = shortName + "-" + matcher.group(2);
                            youTrackServer.comment(user, new Issue(issueId), "Related build: " + build.getAbsoluteUrl());
                        }
                    }
                }
            }

            if (youTrackSite.isCommandsEnabled()) {
                String[] lines = msg.split("\n");

                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.contains("#")) {

                        StringBuilder stringBuilder = new StringBuilder();
                        for (Project project : projects) {
                            stringBuilder.append("#").append(project.getShortName()).append("|");
                        }
                        stringBuilder.deleteCharAt(stringBuilder.length()-1);

                        String patternString = "\\(((" + stringBuilder.toString() + " -\\d+), )*(" + stringBuilder.toString() + " -\\d+)\\)";
                        Pattern pattern = Pattern.compile(patternString);


                        String comment = line.substring(0, line.indexOf("#"));
                        String issueStart = line.substring(line.indexOf("#")+1);

                        Project p = null;
                        for (Project project : projects) {
                            if (issueStart.startsWith(project.getShortName() + "-")) {
                                p = project;
                            }
                        }
                        if(p != null) {
                            Pattern projectPattern = Pattern.compile("(" + p.getShortName() + "-" + "(\\d+)" + ") (.*)");
                            Matcher matcher = projectPattern.matcher(issueStart);
                            while (matcher.find()) {
                                if (matcher.groupCount() >= 1) {
                                    String issueId = p.getShortName() + "-" + matcher.group(2);
                                    if (!youTrackSite.isRunAsEnabled()) {
                                        youTrackServer.applyCommand(user, new Issue(issueId), matcher.group(3), comment, null);
                                    } else {
                                        User runAs = new User();
                                        runAs.setUsername(next.getAuthor().getId());
                                        youTrackServer.applyCommand(user, new Issue(issueId), matcher.group(3), comment, runAs);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        super.onChangeLogParsed(build, listener, changelog);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof YouTrackSCMListener;
    }
}
