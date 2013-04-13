package org.jenkinsci.plugins.youtrack.youtrackapi;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * This object represents a user.
 */
public class User {
    /**
     * The username/login of the user.
     */
    private String username;
    /**
     * The set of cookies if this user has a session.
     */
    private transient List<String> cookies;

    /**
     * Constructs a user.
     */
    public User() {
        cookies = new ArrayList<String>();
    }

    /**
     * @return the username of the user.
     */
    public String getUsername() {
        return username;
    }


    /**
     * Gets the list of cookie strings for this user.
     *
     * @return set of cookie strings.
     */
    public List<String> getCookies() {
        return cookies;
    }

    /**
     * Handler for parsing user query if will find the first user
     * in the result.
     */
    public static class UserRefHandler extends DefaultHandler {
        /**
         * The user result.
         */
        private User user;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (user == null && qName.equals("user")) {
                user = new User();
                user.username = attributes.getValue("login");
            }
        }

        /**
         * Gets the first user found. Should first be called when parsing if finished.
         *
         * @return the first user found in parsed xml.
         */
        public User getUser() {
            return user;
        }


    }
}
