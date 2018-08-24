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

@Component("EmailGroupOnAutoMergeFailure")
public class EmailGroupOnAutoMergeFailure {
    private final MailService mailService;
    private final ApplicationPropertiesService applicationPropertiesService;

    @Autowired
    public EmailGroupOnAutoMergeFailure(@ComponentImport MailService mailService,
                                        @ComponentImport ApplicationPropertiesService applicationPropertiesService) {
        this.mailService =  mailService;
        this.applicationPropertiesService = applicationPropertiesService;
    }
    @EventListener
    public void onPullRequestCreated(PullRequestOpenedEvent event) {
        PullRequest p = event.getPullRequest();
        if (p.getTitle().equals("Automatic merge failure") || Boolean.TRUE) {
            System.out.println("baseURL " + applicationPropertiesService.getBaseUrl());
            String culprit = p.getAuthor().getUser().getDisplayName();
            Repository repo = p.getFromRef().getRepository();
            String repoId = repo.getName();
            String projectKey = repo.getProject().getKey();
            String id = Long.toString(p.getId());
            String link = applicationPropertiesService.getBaseUrl() + "/projects/" + projectKey + "/repos/" + repoId + "/pull-requests/" + id;
            MailMessage.Builder mailMessageBuilder = new MailMessage.Builder()
                            .to("bbianchi@vestmark.com")
                            .from("bitbucket@vestmark.com")
                            .text("<html><body><p>This is the body</p></body></html>")
                            .subject("Message from Auto Merge Detector")
                            .header("Content-type", "text/html; charset=UTF-8");

            if (mailService.isHostConfigured()) {
                System.out.println("In Mail Message block!!!");
                mailService.submit(mailMessageBuilder.build());
            } else {
                System.out.println("Mail Server not configured");
            }
        }
        System.out.println("PR opened!!!!!!!!!!!!!!!!");
        System.out.println("title = " + p.getTitle());
    }
}