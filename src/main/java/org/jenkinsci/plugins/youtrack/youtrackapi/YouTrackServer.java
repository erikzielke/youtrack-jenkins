package org.jenkinsci.plugins.youtrack.youtrackapi;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains methods for communication with a YouTrack server using the REST API for version 4 of YouTrack.
 */
public class YouTrackServer {
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(YouTrackServer.class.getName());
    /**
     * The url of the YouTrack server.
     */
    private final String serverUrl;

    /**
     * Constructs a server.
     *
     * @param serverUrl the url of the server.
     */
    public YouTrackServer(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Gets all projects that the given user can see. The user shall be one obtained from {@link #login(String, String)}, i.e.
     * it should contains the cookie strings for the users login session.
     *
     * @param user the user to get projects for.
     * @return the list of projects the user can see.
     */
    public List<Project> getProjects(User user) {
        List<Project> projects = new ArrayList<Project>();
        try {
            URL url = new URL(serverUrl + "/rest/project/all");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


            for (String cookie : user.getCookies()) {

                urlConnection.setRequestProperty("Cookie", cookie);
            }

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            try {
                SAXParser saxParser = saxParserFactory.newSAXParser();
                Project.ProjectListHandler dh = new Project.ProjectListHandler();
                saxParser.parse(urlConnection.getInputStream(), dh);
                return dh.getProjects();
            } catch (ParserConfigurationException e) {
                LOGGER.log(Level.WARNING, "Could not get YouTrack Projects", e);
            } catch (SAXException e) {
                LOGGER.log(Level.WARNING, "Could not get YouTrack Projects", e);
            }
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Could not get YouTrack Projects", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not get YouTrack Projects", e);
        }
        return projects;

    }

    /**
     * Adds a comment to the issue with currently logged in user.
     *
     * @param user    the currently logged in user.
     * @param issue   the issue to comment on.
     * @param comment the comment text.
     * @return if comment was added.
     */
    public boolean comment(User user, Issue issue, String comment) {
        try {
            URL url = new URL(serverUrl + "/rest/issue/" + issue.getId() + "/execute");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            for (String cookie : user.getCookies()) {
                urlConnection.setRequestProperty("Cookie", cookie);
            }

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
            outputStreamWriter.write("comment=" + comment);
            outputStreamWriter.flush();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            }


        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not comment", e);
        }
        return false;
    }

    /**
     * Apply a command to an issue.
     *
     * @param user    the user used to apply the command, shall be one with cookies set.
     * @param issue   the issue to apply the command to.
     * @param command the command to apply.
     * @param comment comment with the command, null is allowed.
     * @param runAs   user to apply the command as, null is allowed.
     */
    public boolean applyCommand(User user, Issue issue, String command, String comment, User runAs) {
        try {


            URL url = new URL(serverUrl + "/rest/issue/" + issue.getId() + "/execute");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            for (String cookie : user.getCookies()) {
                urlConnection.setRequestProperty("Cookie", cookie);
            }

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());


            String str = "command=" + URLEncoder.encode(command, "UTF-8");
            if (comment != null) {
                str += "&comment=" + URLEncoder.encode(comment, "UTF-8");
            }
            if (runAs != null) {
                str += "&runAs=" + runAs.getUsername();
            }
            outputStreamWriter.write(str);
            outputStreamWriter.flush();

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            } else {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String l;
                StringBuilder stringBuilder = new StringBuilder();
                while ((l = bufferedReader.readLine()) != null) {
                    stringBuilder.append(l).append("\n");
                }
                LOGGER.log(Level.WARNING, "Could not apply command. Server response: " + stringBuilder.toString());
            }


        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not apply command", e);
        }
        return false;
    }

    /**
     * Get a YouTrack user from the e-mail address.
     *
     * @param user  the user to get.
     * @param email the email to get.
     * @return the user, null if none found.
     */
    public User getUserByEmail(User user, String email) {
        try {
            URL url = new URL(serverUrl + "/rest/admin/user?q=" + email);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            for (String cookie : user.getCookies()) {
                urlConnection.setRequestProperty("Cookie", cookie);
            }

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                SAXParser saxParser = saxParserFactory.newSAXParser();
                User.UserRefHandler dh = new User.UserRefHandler();
                saxParser.parse(urlConnection.getInputStream(), dh);
                return dh.getUser();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not get user", e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.WARNING, "Could not get user", e);
        } catch (SAXException e) {
            LOGGER.log(Level.WARNING, "Could not get user", e);
        }
        return null;
    }

    /**
     * Logs in a user. The result is the user object with cookies set, which should
     * be used on all subsequent requests.
     *
     * @param username the username of the user.
     * @param password the password of the user.
     * @return user, null if fails to login
     */
    public User login(String username, String password) {
        try {
            URL url = new URL(serverUrl + "/rest/user/login");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
            outputStreamWriter.write("login=" + username + "&password=" + password);
            outputStreamWriter.flush();

            Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
            List<String> strings = headerFields.get("Set-Cookie");

            User user = new User();
            for (String string : strings) {
                user.getCookies().add(string);
            }

            return user;
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Could not login", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not login", e);
        }
        return null;
    }

    /**
     * Adds a build with the name to the bundle with the given name.
     * @param user the logged in user.
     * @param bundleName the name of the bundle to add a build to.
     * @param buildName the name of the build to add.
     */
    public boolean addBuildToBundle(User user, String bundleName, String buildName) {
        try {
            String encode = URLEncoder.encode(bundleName, "ISO-8859-1").replace("+", "%20");
            String encode1 = URLEncoder.encode(buildName, "ISO-8859-1").replace("+", "%20");
            URL url = new URL(serverUrl + "/rest/admin/customfield/buildBundle/" + encode + "/" + encode1);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("PUT");
            for (String cookie : user.getCookies()) {
                urlConnection.setRequestProperty("Cookie", cookie);
            }
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
            outputStreamWriter.flush();


            int responseCode = urlConnection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_CREATED) {
                return true;
            }

        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Could not add to bundle", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not add to bundle", e);
        }
        return false;
    }

    /**
     * Gets an issue by issue id.
     *
     * Currently the only value retrieved is the State field.
     *
     * @param user the user session.
     * @param issueId the id of the issue.
     * @return the issue if any.
     */
    public Issue getIssue(User user, String issueId) {
        try {
            URL url = new URL(serverUrl + "/rest/issue/" + issueId);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            for (String cookie : user.getCookies()) {
                urlConnection.setRequestProperty("Cookie", cookie);
            }


            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    Issue.IssueHandler issueHandler = new Issue.IssueHandler();
                    saxParser.parse(urlConnection.getInputStream(), issueHandler);
                    return issueHandler.getIssue();
                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.WARNING, "Could not get issue", e);
                } catch (SAXException e) {
                    LOGGER.log(Level.WARNING, "Could not get issue", e);
                }
            }

        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Could not get issue", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not get issue", e);
        }
        return null;
    }


}
