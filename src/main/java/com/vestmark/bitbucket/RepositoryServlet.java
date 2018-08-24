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


public class RepositoryServlet extends HttpServlet {
    private final RepositoryService repositoryService;
    private final SoyTemplateRenderer soyTemplateRenderer;

    @Autowired
    public RepositoryServlet(@ComponentImport SoyTemplateRenderer soyTemplateRenderer, 
                             @ComponentImport RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
        this.soyTemplateRenderer = soyTemplateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Get repoSlug from path
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

        boolean isSettings = false;
        if (components.length == 4 && "settings".equalsIgnoreCase(components[3])) {
            isSettings = true;
        }

        String template = "plugin.repositorySettings";
        //String template = isSettings ? "plugin.example.repositorySettings" : "plugin.example.repository";
        System.out.println("template = " + template);
        render(resp, template, ImmutableMap.<String, Object>of("repository", repository));
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
