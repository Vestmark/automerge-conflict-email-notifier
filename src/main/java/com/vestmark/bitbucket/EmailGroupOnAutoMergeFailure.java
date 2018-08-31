package com.vestmark.bitbucket;

import com.atlassian.bitbucket.pull.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.atlassian.bitbucket.event.pull.PullRequestOpenedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.bitbucket.user.*;
import com.atlassian.bitbucket.project.*;
import com.atlassian.bitbucket.mail.*;
import com.atlassian.bitbucket.server.*;
//import com.atlassian.bitbucket.mail.MailService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import com.google.common.collect.Sets;
import javax.annotation.Nonnull;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

@Component("EmailGroupOnAutoMergeFailure")
public class EmailGroupOnAutoMergeFailure {
    private final MailService mailService;
    private final ApplicationPropertiesService applicationPropertiesService;
    private final PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public EmailGroupOnAutoMergeFailure(@ComponentImport MailService mailService,
                                        @ComponentImport PluginSettingsFactory pluginSettingsFactory,
                                        @ComponentImport ApplicationPropertiesService applicationPropertiesService) {
        this.mailService =  mailService;
        this.applicationPropertiesService = applicationPropertiesService;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }
    @EventListener
    public void onPullRequestCreated(PullRequestOpenedEvent event) {
        PullRequest p = event.getPullRequest();
        Repository repo = p.getFromRef().getRepository();
        String repoId = repo.getName();
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        if (pluginSettings.get("com.vestmark.bitbucket.email-group-on-auto-merge-failure." + repoId + ".email") == null) {
          return;
        }
        String email = pluginSettings.get("com.vestmark.bitbucket.email-group-on-auto-merge-failure." + repoId + ".email").toString();
        String subjectText= "Bitbucket Auto Merge Conflict Detected";
        if ((p.getTitle().equals("Automatic merge failure") && email != null && email != "")) {
            String culprit = p.getAuthor().getUser().getDisplayName();
            String projectKey = repo.getProject().getKey();
            String id = Long.toString(p.getId());
            String link = applicationPropertiesService.getBaseUrl() + "/projects/" + projectKey + "/repos/" + repoId + "/pull-requests/" + id;
            MailMessage.Builder mailMessageBuilder = new MailMessage.Builder()
                            .to(email)
                            .from("bitbucket@vestmark.com")
                            .text("<html><body><p>Automatic merging has failed due to a conflict; a <a href=\"" + link + "\">pull request</a> has been opened on " + culprit + "'s behalf</p></body></html>")
                            .subject(subjectText)
                            .header("Content-type", "text/html; charset=UTF-8");
            if (mailService.isHostConfigured()) {
                mailService.submit(mailMessageBuilder.build());
            }
        }
    }
}