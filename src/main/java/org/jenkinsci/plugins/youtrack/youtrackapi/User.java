package org.jenkinsci.plugins.youtrack.youtrackapi;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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

    public static class UserRefHandler extends DefaultHandler {
        private User user;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if(user == null && qName.equals("user")) {
                user = new User();
                user.username = attributes.getValue("login");
            }
        }


        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }
}
