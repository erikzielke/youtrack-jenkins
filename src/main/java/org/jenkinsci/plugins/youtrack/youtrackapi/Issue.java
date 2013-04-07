package org.jenkinsci.plugins.youtrack.youtrackapi;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
public class Issue {
    private String id;
    private String state;

    public Issue(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public static class IssueHandler extends DefaultHandler  {
        private String currentField;
        private StringBuilder stringBuilder = new StringBuilder();
        private Issue issue;

        public Issue getIssue() {
            return issue;
        }

        public void setIssue(Issue issue) {
            this.issue = issue;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            stringBuilder.setLength(0);
            if(qName.equals("issue")) {
                this.issue = new Issue(attributes.getValue("id"));
            }
            if(qName.equals("field")) {
                currentField = attributes.getValue("name");
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            stringBuilder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if(qName.equals("value")) {
                if(currentField.equals("State")) {
                    issue.state = stringBuilder.toString();
                }
            }
        }
    }
}
