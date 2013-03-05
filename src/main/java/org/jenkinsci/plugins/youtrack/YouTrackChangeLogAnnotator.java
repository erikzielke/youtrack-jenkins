package org.jenkinsci.plugins.youtrack;

import hudson.Extension;
import hudson.MarkupText;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.ChangeLogAnnotator;
import hudson.scm.ChangeLogSet;
import org.apache.log4j.Logger;
import org.jenkinsci.plugins.youtrack.youtrackapi.Issue;
import org.jenkinsci.plugins.youtrack.youtrackapi.Project;
import org.jenkinsci.plugins.youtrack.youtrackapi.User;
import org.jenkinsci.plugins.youtrack.youtrackapi.YouTrackServer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
public class YouTrackChangeLogAnnotator extends ChangeLogAnnotator {
    private static final Logger LOGGER = Logger.getLogger(YouTrackChangeLogAnnotator.class.getName());

    @Override
    public void annotate(AbstractBuild<?, ?> abstractBuild, ChangeLogSet.Entry entry, MarkupText markupText) {
        AbstractProject<?, ?> project = abstractBuild.getProject();
        YouTrackSite youTrackSite = YouTrackSite.get(project);
        YouTrackSaveProjectShortNamesAction action = abstractBuild.getProject().getLastBuild().getAction(YouTrackSaveProjectShortNamesAction.class);
        List<String> shortNames = action.getShortNames();

        if (youTrackSite != null && youTrackSite.isPluginEnabled() && youTrackSite.isAnnotationsEnabled()) {
            LOGGER.info("Annotating change");

            String msg = entry.getMsg();
            for (String shortName : shortNames) {
                Pattern projectPattern = Pattern.compile("(" + shortName + "-" + "(\\d+)" + ")");
                Matcher matcher = projectPattern.matcher(msg);
                while (matcher.find()) {
                    if (matcher.groupCount() >= 1) {
                        String issueId = shortName + "-" + matcher.group(2);
                        String commitId = "_" + entry.getMsg().hashCode();
                        String s = "<script>\n";
                        String js =  "var tooltip = new YAHOO.widget.Tooltip(\"tt1\", {\n    context: \"" +commitId+ "\"\n});\ntooltip.contextTriggerEvent.subscribe(\n    \n    \n    function (type, args) {\n        var issueId = \"" +issueId+ "\";\n        var serverUrl = \""+youTrackSite.getUrl()+"\";\n        var username = \""+ youTrackSite.getUsername()+"\";\n        var password = \"" + youTrackSite.getPassword() + "\";\n        var context = args[0];\n        var cfg = this.cfg;\n        cfg.setProperty(\"text\", \"Loading data...\");\n        \n\n        var request = Q.ajax({\n            url: serverUrl + \"/rest/issue/\" + issueId,\n            dataType: \"json\"\n        });\n        \n        request.done(\n            function(data) {\n                alert(data);}\n        );\n        \n        request.fail(function(jqXHR, textStatus) {\n        \n            var request2 = Q.ajax({\n                url: serverUrl + \"/rest/user/login\",\n                type: \"POST\",\n                data: {\"login\": username, \"password\": password}\n                \n            });\n            \n            request2.done(function(data) {\n                var request3 = Q.ajax({\n                    url: serverUrl + \"/rest/issue/\" + issueId,\n                    dataType: \"json\",\n                    xhrFields: {\n                        withCredentials: true\n                    }\n                });\n                \n                request3.done(\n                    function(data) {\n                        \n                        var id = data.id;\n                        \n                        var summaryField = null;  \n                        var descriptionField = null;\n                        var resolvedField = null;\n                        \n                        Q.each(data.field, function(index, object) {\n                            if(object.name == \"summary\") {\n                                summaryField = object;\n                            }\n                            if(object.name == \"description\") {\n                                descriptionField = object\n                            }\n                            \n                            if(object.name == \"resolved\") {\n                                resolvedField = object;\n                            }\n                        });\n\n                        var text;\n                        var desc = \"\";\n                        if(descriptionField) {\n                            desc = descriptionField.value\n                        }\n                        \n                        if (resolvedField == null) {\n                            text = \"<h2>\" + id + \": \" + summaryField.value + \"</h2><p>\" + desc + \"</p>\";\n                        } else {\n                            text = \"<h2><del>\" + id + \": \" + summaryField.value + \"</del></h2><p>\" + desc + \"</p>\";\n                        }\n                        cfg.setProperty(\"text\", text)\n                        \n                    }\n                );\n                \n                request3.fail(function(jqXHR, textStatus) {\n                    alert(textStatus);\n                })\n            });\n            \n        });\n        \n        \n\n    }\n);\n";

                        s += js + "\n</script>";
                        markupText.addMarkup(matcher.start(1), matcher.end(1), s + "<a title=\"test\" id=\"" + commitId + "\" href=\"" + youTrackSite.getUrl() + "/issue/" + issueId + "\">", "</a>");
                    }
                }
            }
        }
    }


}
