package org.jenkinsci.plugins.youtrack;

import hudson.Plugin;

/**
 * Listens to SCM changes.
 */
public class YouTrackPlugin extends Plugin {
    private transient YouTrackSCMListener scmListener;

    @Override
    public void start() throws Exception {
        super.start();

        scmListener = new YouTrackSCMListener();
        scmListener.register();
    }

    @Override
    public void stop() throws Exception {
        scmListener.unregister();
        super.stop();
    }
}
