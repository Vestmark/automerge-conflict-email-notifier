package com.vestmark.bitbucket.plugin;

import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.google.common.collect.ImmutableMap;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class RepositoryServlet extends HttpServlet {
    private final RepositoryService repositoryService;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public RepositoryServlet(@ComponentImport SoyTemplateRenderer soyTemplateRenderer,
                             @ComponentImport PluginSettingsFactory pluginSettingsFactory,
                             @ComponentImport RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("IN REPO SERVLET!!!!!!!");
        String pathInfo = req.getPathInfo();
        String[] components = pathInfo.split("/");
        if (components.length < 3) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Repository repository = repositoryService.getBySlug(components[1], components[2]);
        if (repository == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String email = "No value set";
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        if (pluginSettings.get("com.vestmark.bitbucket.email-group-on-auto-merge-failure." + repository.getName() + ".email") != null) {
            email = pluginSettings.get("com.vestmark.bitbucket.email-group-on-auto-merge-failure." + repository.getName() + ".email").toString();
        }
        String template = "plugin.repositorySettings";
        //render(resp, template, ImmutableMap.<String, Object>of("repository", repository));
        render(resp, template, ImmutableMap.<String, Object>builder().put("repository", repository).put("email", email).build());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("IN doPost!!!");
        String pathInfo = req.getPathInfo();
        String[] components = pathInfo.split("/");
        if (components.length < 3) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Repository repository = repositoryService.getBySlug(components[1], components[2]);
        if (repository == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String email =  req.getParameter("email");
        System.out.println("Email = " + email);
        if (email != "") {
          PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
          pluginSettings.put("com.vestmark.bitbucket.email-group-on-auto-merge-failure." + repository.getName() + ".email", email);
        }
        String template = "plugin.repositorySettings";
        //render(resp, template, ImmutableMap.<String, Object>of("repository", repository));
        render(resp, template, ImmutableMap.<String, Object>builder().put("repository", repository).put("email", email).build());
    }

    protected void render(HttpServletResponse resp, String templateName, Map<String, Object> data) throws IOException, ServletException {
        resp.setContentType("text/html;charset=UTF-8");
        try {
            soyTemplateRenderer.render(resp.getWriter(),
                    "com.vestmark.bitbucket.email-group-on-auto-merge-failure:soy",
                    templateName,
                    data);
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(e);
        }
    }
}
