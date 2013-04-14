package org.jenkinsci.plugins.youtrack;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.youtrack.youtrackapi.Issue;
import org.jenkinsci.plugins.youtrack.youtrackapi.User;
import org.jenkinsci.plugins.youtrack.youtrackapi.YouTrackServer;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.List;

/**
 * Updates build bundle.
 */
public class YouTrackBuildUpdater extends Recorder {

    private String name;
    private String bundleName;

    @DataBoundConstructor
    public YouTrackBuildUpdater(String name, String bundleName) {
        this.name = name;
        this.bundleName = bundleName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }



    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        YouTrackSite youTrackSite = YouTrackSite.get(build.getProject());
        if (youTrackSite == null || !youTrackSite.isPluginEnabled()) {
            return true;
        }

        YouTrackServer youTrackServer = new YouTrackServer(youTrackSite.getUrl());
        User user = youTrackServer.login(youTrackSite.getUsername(), youTrackSite.getPassword());


        String buildName;
        if(getName() == null || getName().equals("")) {
            buildName = String.valueOf(build.getNumber());
        } else {
            buildName = String.valueOf(build.getNumber()) + " (" + name + ")";

        }
        youTrackServer.addBuildToBundle(user, getBundleName(), buildName);

        YouTrackSaveFixedIssues action = build.getAction(YouTrackSaveFixedIssues.class);
        if(action != null) {
            List<String> issueIds = action.getIssueIds();
            if(build.getResult().isBetterOrEqualTo(Result.SUCCESS)) {

                for (String issueId : issueIds) {
                Issue issue = new Issue(issueId);

                    youTrackServer.applyCommand(user, issue, "Fixed in build " + buildName, null, null);
                }
            }

        }

        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }



        @Override
        public String getDisplayName() {
            return "YouTrack Build Updater";
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(YouTrackBuildUpdater.class, formData);
        }
    }
}
