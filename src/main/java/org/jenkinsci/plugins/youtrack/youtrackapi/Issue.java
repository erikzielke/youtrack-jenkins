package org.jenkinsci.plugins.youtrack.youtrackapi;

/**
 *
 */
public class Issue {
    private String id;

    public Issue(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
