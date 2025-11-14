package ru.vsu.cs.social_network.api_gateway.contoller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private static final Logger log = LoggerFactory.getLogger(DebugController.class);
    private final RouteLocator routeLocator;

    public DebugController(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @GetMapping("/routes")
    public List<String> getRoutes() {
        log.info("🔄 Getting all routes");
        return routeLocator.getRoutes()
                .map(route -> {
                    String info = String.format("Route: %s -> %s (Predicates: %s, Filters: %s)",
                            route.getId(),
                            route.getUri(),
                            route.getPredicate(),
                            route.getFilters());
                    log.info("📋 {}", info);
                    return info;
                })
                .collect(Collectors.toList())
                .block();
    }
}