package org.example.httpserver;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class RouteHandler {
    private final Map<String, Map<String, BiConsumer<ServerRequest, ServerResponse>>> routes;
    private BiConsumer<ServerRequest, ServerResponse> defaultRoute;

    public RouteHandler() {
        routes = new HashMap<>();

        defaultRoute = (req, res) -> {
            try {
                res.sendText(404, "Not Found");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public void addRoute(String method, String path, BiConsumer<ServerRequest, ServerResponse> handler) {
        routes.computeIfAbsent(path, k -> new HashMap<>()).put(method, handler);
    }

    public BiConsumer<ServerRequest, ServerResponse> getHandler(String method, String path) {
        Map<String, BiConsumer<ServerRequest, ServerResponse>> methodHandlers = routes.get(path);
        if (methodHandlers != null) {
            return methodHandlers.get(method);
        }
        return null;
    }

    public void setDefaultRoute(BiConsumer<ServerRequest, ServerResponse> defaultRoute) {
        this.defaultRoute = defaultRoute;
    }

    public BiConsumer<ServerRequest, ServerResponse> getDefaultRoute() {
        return defaultRoute;
    }
}
