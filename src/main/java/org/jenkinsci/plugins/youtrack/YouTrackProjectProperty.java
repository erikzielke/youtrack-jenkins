package org.jenkinsci.plugins.youtrack;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.CopyOnWriteList;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class YouTrackProjectProperty extends JobProperty<AbstractProject<?, ?>> {

    private String siteName;
    private boolean pluginEnabled;
    private boolean commentsEnabled;
    private boolean commandsEnabled;
    private boolean runAsEnabled;
    private boolean annotationsEnabled;

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();


    @DataBoundConstructor
    public YouTrackProjectProperty(String siteName, boolean pluginEnabled, boolean commentsEnabled, boolean commandsEnabled, boolean runAsEnabled, boolean annotationsEnabled) {
        this.siteName = siteName;
        this.pluginEnabled = pluginEnabled;
        this.commentsEnabled = commentsEnabled;
        this.commandsEnabled = commandsEnabled;
        this.runAsEnabled = runAsEnabled;
        this.annotationsEnabled = annotationsEnabled;
    }

    @Override
    public JobPropertyDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    public void setPluginEnabled(boolean pluginEnabled) {
        this.pluginEnabled = pluginEnabled;
    }

    public boolean isCommentsEnabled() {
        return commentsEnabled;
    }

    public void setCommentsEnabled(boolean commentsEnabled) {
        this.commentsEnabled = commentsEnabled;
    }

    public boolean isCommandsEnabled() {
        return commandsEnabled;
    }

    public void setCommandsEnabled(boolean commandsEnabled) {
        this.commandsEnabled = commandsEnabled;
    }

    public boolean isRunAsEnabled() {
        return runAsEnabled;
    }

    public void setRunAsEnabled(boolean runAsEnabled) {
        this.runAsEnabled = runAsEnabled;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public boolean isAnnotationsEnabled() {
        return annotationsEnabled;
    }

    public void setAnnotationsEnabled(boolean annotationsEnabled) {
        this.annotationsEnabled = annotationsEnabled;
    }

    public static final class DescriptorImpl extends JobPropertyDescriptor {
        private final CopyOnWriteList<YouTrackSite> sites = new CopyOnWriteList<YouTrackSite>();

        public DescriptorImpl() {
            super(YouTrackProjectProperty.class);
            load();

        }

        public void setSites(YouTrackSite site) {
            sites.add(site);
        }

        public YouTrackSite[] getSites() {
            return sites.toArray(new YouTrackSite[0]);
        }

        @Override
        public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            YouTrackProjectProperty ypp = req.bindParameters(YouTrackProjectProperty.class, "youtrack.");
            if (ypp.siteName == null) {
                ypp = null;
            }
            return ypp;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) {
            sites.replaceBy(req.bindParametersToList(YouTrackSite.class,
                    "youtrack."));
            save();
            return true;
        }


        @Override
        public String getDisplayName() {
            return "YouTrack Plugin";
        }


    }

    public YouTrackSite getSite() {
        YouTrackSite result = null;
        YouTrackSite[] sites = DESCRIPTOR.getSites();
        if (siteName == null && sites.length > 0) {
            result = sites[0];
        }

        for (YouTrackSite site : sites) {
            if (site.getUrl().equals(siteName)) {
                result = site;
                break;
            }
        }
        if (result != null) {
            result.setPluginEnabled(pluginEnabled);
            result.setCommentEnabled(commentsEnabled);
            result.setCommandsEnabled(commandsEnabled);
            result.setAnnotationsEnabled(annotationsEnabled);
            result.setRunAsEnabled(runAsEnabled);
        }
        return result;
    }
}
