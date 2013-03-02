package org.jenkinsci.plugins.youtrack;

import hudson.model.AbstractProject;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 */
public class YouTrackSite {
    private String url;
    private String username;
    private String password;

    private transient boolean pluginEnabled;
    private transient boolean runAsEnabled;
    private transient boolean commandsEnabled;
    private transient boolean commentEnabled;

    @DataBoundConstructor
    public YouTrackSite(String username, String password, String url) {
        this.username = username;
        this.password = password;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    public void setPluginEnabled(boolean pluginEnabled) {
        this.pluginEnabled = pluginEnabled;
    }

    public boolean isRunAsEnabled() {
        return runAsEnabled;
    }

    public void setRunAsEnabled(boolean runAsEnabled) {
        this.runAsEnabled = runAsEnabled;
    }

    public boolean isCommandsEnabled() {
        return commandsEnabled;
    }

    public void setCommandsEnabled(boolean commandsEnabled) {
        this.commandsEnabled = commandsEnabled;
    }

    public static YouTrackSite get(AbstractProject<?, ?> project) {
        YouTrackProjectProperty ypp = project.getProperty(YouTrackProjectProperty.class);
        if(ypp != null) {
            YouTrackSite site = ypp.getSite();
            if(site != null) {
                return site;
            }
        }
        YouTrackSite[] sites = YouTrackProjectProperty.DESCRIPTOR.getSites();
        if(sites.length == 1) {
            return sites[0];
        }
        return null;
    }

    public void setCommentEnabled(boolean commentEnabled) {
        this.commentEnabled = commentEnabled;
    }

    public boolean isCommentEnabled() {
        return commentEnabled;
    }
}
