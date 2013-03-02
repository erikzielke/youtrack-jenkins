package org.jenkinsci.plugins.youtrack.youtrackapi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class User {
    private String username;
    private String password;
    private transient List<String> cookies;

    public String getUsername() {
        return username;
    }

    public User() {
        cookies = new ArrayList<String>();
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
