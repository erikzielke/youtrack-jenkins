package org.jenkinsci.plugins.youtrack;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.listeners.SCMListener;
import hudson.scm.ChangeLogSet;
import hudson.tasks.Mailer;
import org.jenkinsci.plugins.youtrack.youtrackapi.Issue;
import org.jenkinsci.plugins.youtrack.youtrackapi.Project;
import org.jenkinsci.plugins.youtrack.youtrackapi.User;
import org.jenkinsci.plugins.youtrack.youtrackapi.YouTrackServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouTrackSCMListener extends SCMListener {

    @Override
    public void onChangeLogParsed(AbstractBuild<?, ?> build, BuildListener listener, ChangeLogSet<?> changelog) throws Exception {

        YouTrackSite youTrackSite = YouTrackSite.get(build.getProject());
        if (youTrackSite == null || !youTrackSite.isPluginEnabled()) {
            return;
        }

        Iterator<? extends ChangeLogSet.Entry> changeLogIterator = changelog.iterator();

        YouTrackServer youTrackServer = new YouTrackServer(youTrackSite.getUrl());
        User user = youTrackServer.login(youTrackSite.getUsername(), youTrackSite.getPassword());

        List<Project> projects = youTrackServer.getProjects(user);
        build.addAction(new YouTrackSaveProjectShortNamesAction(projects));

        List<Issue> fixedIssues = new ArrayList<Issue>();

        while (changeLogIterator.hasNext()) {
            ChangeLogSet.Entry next = changeLogIterator.next();
            String msg = next.getMsg();

            addCommentIfEnabled(build, youTrackSite, youTrackServer, user, projects, msg);

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

                        String comment = null;
                        String issueStart = line.substring(line.indexOf("#")+1);

                        if(i + 1 < lines.length) {
                            String l = lines[i + 1];
                            if(!l.contains("#")) {
                                comment = l;
                            }
                        }

                        Project p = null;
                        for (Project project : projects) {
                            if (issueStart.startsWith(project.getShortName() + "-")) {
                                p = project;
                            }
                        }

                        findIssueId(youTrackSite, youTrackServer, user, fixedIssues, next, comment, issueStart, p);
                    }
                }
            }
        }

        build.addAction(new YouTrackSaveFixedIssues(fixedIssues));


        super.onChangeLogParsed(build, listener, changelog);
    }

    private void findIssueId(YouTrackSite youTrackSite, YouTrackServer youTrackServer, User user, List<Issue> fixedIssues, ChangeLogSet.Entry next, String comment, String issueStart, Project p) {
        if(p != null) {
            Pattern projectPattern = Pattern.compile("(" + p.getShortName() + "-" + "(\\d+)" + ") (.*)");

            Matcher matcher = projectPattern.matcher(issueStart);
            while (matcher.find()) {
                if (matcher.groupCount() >= 1) {
                    String issueId = p.getShortName() + "-" + matcher.group(2);
                    if (!youTrackSite.isRunAsEnabled()) {
                        youTrackServer.applyCommand(user, new Issue(issueId), matcher.group(3), comment, null);
                    } else {
                        String address = next.getAuthor().getProperty(Mailer.UserProperty.class).getAddress();
                        User userByEmail = youTrackServer.getUserByEmail(user, address);

                        //Get the issue state, then apply command, and get the issue state again.
                        //to know whether the command has been marked as fixed, instead of trying to
                        //interpret the command. This means however that there is a possibility for
                        //the user to change state between the before and the after call, so the after
                        //state can be affected by something else than the command.
                        Issue before = youTrackServer.getIssue(user, issueId);
                        youTrackServer.applyCommand(user, new Issue(issueId), matcher.group(3), comment, userByEmail);
                        Issue after = youTrackServer.getIssue(user, issueId);

                        if(!before.getState().equals("Fixed") && after.getState().equals("Fixed")) {
                            fixedIssues.add(after);
                        }

                    }
                }
            }

        }
    }

    private void addCommentIfEnabled(AbstractBuild<?, ?> build, YouTrackSite youTrackSite, YouTrackServer youTrackServer, User user, List<Project> projects, String msg) {
        if (youTrackSite.isCommentEnabled()) {
            for (Project project1 : projects) {
                String shortName = project1.getShortName();
                Pattern projectPattern = Pattern.compile("(" + shortName + "-" + "(\\d+)" + ")");
                Matcher matcher = projectPattern.matcher(msg);
                while (matcher.find()) {
                    if (matcher.groupCount() >= 1) {
                        String issueId = shortName + "-" + matcher.group(2);
                        //noinspection deprecation
                        youTrackServer.comment(user, new Issue(issueId), "Related build: " + build.getAbsoluteUrl());
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof YouTrackSCMListener;
    }
}
