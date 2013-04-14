package org.jenkinsci.plugins.youtrack;

import com.google.gson.Gson;
import hudson.model.AbstractProject;
import hudson.model.Action;
import org.jenkinsci.plugins.youtrack.youtrackapi.Issue;
import org.jenkinsci.plugins.youtrack.youtrackapi.User;
import org.jenkinsci.plugins.youtrack.youtrackapi.YouTrackServer;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * This is the action to get issue data from an YouTrack issue.
 */
public class YouTrackIssueAction implements Action {

    public final AbstractProject project;

    public YouTrackIssueAction(AbstractProject project) {
        this.project = project;
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "youtrack";
    }

    public HttpResponse doIssue() {

        return new HttpResponse() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {

                String id = req.getParameter("id");
                YouTrackSite youTrackSite = YouTrackSite.get(project);
                if (youTrackSite == null) {
                    rsp.getWriter().write("YouTrack integration not set up for this project");
                    return;
                }

                YouTrackServer youTrackServer = new YouTrackServer(youTrackSite.getUrl());
                User user = youTrackServer.login(youTrackSite.getUsername(), youTrackSite.getPassword());
                Issue issue = youTrackServer.getIssue(user, id);

                Gson gson = new Gson();
                String json = gson.toJson(issue);
                rsp.getWriter().write(json);
            }
        };
    }
}
