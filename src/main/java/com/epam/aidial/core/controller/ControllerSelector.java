package com.epam.aidial.core.controller;

import com.epam.aidial.core.Proxy;
import com.epam.aidial.core.ProxyContext;
import io.vertx.core.http.HttpMethod;
import lombok.experimental.UtilityClass;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ControllerSelector {

    private static final Pattern PATTERN_POST_DEPLOYMENT = Pattern.compile("/+openai/deployments/([-.@a-zA-Z0-9]+)/(completions|chat/completions|embeddings)");
    private static final Pattern PATTERN_DEPLOYMENT = Pattern.compile("/+openai/deployments/([-.@a-zA-Z0-9]+)");
    private static final Pattern PATTERN_DEPLOYMENTS = Pattern.compile("/+openai/deployments");

    private static final Pattern PATTERN_MODEL = Pattern.compile("/+openai/models/([-.@a-zA-Z0-9]+)");
    private static final Pattern PATTERN_MODELS = Pattern.compile("/+openai/models");

    private static final Pattern PATTERN_ADDON = Pattern.compile("/+openai/addons/([-.@a-zA-Z0-9]+)");
    private static final Pattern PATTERN_ADDONS = Pattern.compile("/+openai/addons");

    private static final Pattern PATTERN_ASSISTANT = Pattern.compile("/+openai/assistants/([-.@a-zA-Z0-9]+)");
    private static final Pattern PATTERN_ASSISTANTS = Pattern.compile("/+openai/assistants");

    private static final Pattern PATTERN_APPLICATION = Pattern.compile("/+openai/applications/([-.@a-zA-Z0-9]+)");
    private static final Pattern PATTERN_APPLICATIONS = Pattern.compile("/+openai/applications");

    private static final Pattern PATTERN_RATE_RESPONSE = Pattern.compile("/+v1/([-.@a-zA-Z0-9]+)/rate");

    public Controller select(Proxy proxy, ProxyContext context) {
        String path = URLDecoder.decode(context.getRequest().path(), StandardCharsets.UTF_8);
        HttpMethod method = context.getRequest().method();
        Controller controller = null;

        if (method == HttpMethod.GET) {
            controller = selectGet(context, path);
        } else if (method == HttpMethod.POST) {
            controller = selectPost(proxy, context, path);
        }

        return (controller == null) ? new RouteController(proxy, context) : controller;
    }

    private static Controller selectGet(ProxyContext context, String path) {
        Matcher match;

        match = match(PATTERN_DEPLOYMENT, path);
        if (match != null) {
            DeploymentController controller = new DeploymentController(context);
            String deploymentId = match.group(1);
            return () -> controller.getDeployment(deploymentId);
        }

        match = match(PATTERN_DEPLOYMENTS, path);
        if (match != null) {
            DeploymentController controller = new DeploymentController(context);
            return controller::getDeployments;
        }

        match = match(PATTERN_MODEL, path);
        if (match != null) {
            ModelController controller = new ModelController(context);
            String modelId = match.group(1);
            return () -> controller.getModel(modelId);
        }

        match = match(PATTERN_MODELS, path);
        if (match != null) {
            ModelController controller = new ModelController(context);
            return controller::getModels;
        }

        match = match(PATTERN_ADDON, path);
        if (match != null) {
            AddonController controller = new AddonController(context);
            String addonId = match.group(1);
            return () -> controller.getAddon(addonId);
        }

        match = match(PATTERN_ADDONS, path);
        if (match != null) {
            AddonController controller = new AddonController(context);
            return controller::getAddons;
        }

        match = match(PATTERN_ASSISTANT, path);
        if (match != null) {
            AssistantController controller = new AssistantController(context);
            String assistantId = match.group(1);
            return () -> controller.getAssistant(assistantId);
        }

        match = match(PATTERN_ASSISTANTS, path);
        if (match != null) {
            AssistantController controller = new AssistantController(context);
            return controller::getAssistants;
        }

        match = match(PATTERN_APPLICATION, path);
        if (match != null) {
            ApplicationController controller = new ApplicationController(context);
            String application = match.group(1);
            return () -> controller.getApplication(application);
        }

        match = match(PATTERN_APPLICATIONS, path);
        if (match != null) {
            ApplicationController controller = new ApplicationController(context);
            return controller::getApplications;
        }

        return null;
    }

    private static Controller selectPost(Proxy proxy, ProxyContext context, String path) {
        Matcher match = match(PATTERN_POST_DEPLOYMENT, path);
        if (match != null) {
            String deploymentId = match.group(1);
            String deploymentApi = match.group(2);
            DeploymentPostController controller = new DeploymentPostController(proxy, context);
            return () -> controller.handle(deploymentId, deploymentApi);
        }

        match = match(PATTERN_RATE_RESPONSE, path);
        if (match != null) {
            String deploymentId = match.group(1);
            RateResponseController controller = new RateResponseController(proxy, context);
            return () -> controller.handle(deploymentId);
        }

        return null;
    }

    private Matcher match(Pattern pattern, String path) {
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? matcher : null;
    }
}