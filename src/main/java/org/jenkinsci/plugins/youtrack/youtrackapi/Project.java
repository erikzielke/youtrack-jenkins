package org.jenkinsci.plugins.youtrack.youtrackapi;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a project.
 */
public class Project {
    /**
     * The short name of the project.
     */
    private String shortName;

    /**
     * @return the short name of the project.
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the short name of the project.
     *
     * @param shortName the short name.
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Handler for the project list.
     */
    static class ProjectListHandler extends DefaultHandler {
        /**
         * List for holding the results.
         */
        private List<Project> projects;

        /**
         * Returns the projects found in the xml, should first be called when parsing is over.
         *
         * @return the projects found
         */
        public List<Project> getProjects() {
            return projects;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            this.projects = new ArrayList<Project>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (qName.equals("project")) {
                Project project = new Project();
                project.setShortName(attributes.getValue("shortName"));
                projects.add(project);
            }
        }
    }
}
