package ru.vsu.cs.social_network.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomAuthorizationRequestResolver implements ServerOAuth2AuthorizationRequestResolver {

    private final DefaultServerOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(DefaultServerOAuth2AuthorizationRequestResolver defaultResolver) {
        this.defaultResolver = defaultResolver;
    }

    /**
     * Разрешает OAuth2 authorization request, добавляя параметры prompt и kc_idp_hint из query параметров.
     *
     * @param exchange обмен данными веб-запроса
     * @return Mono с OAuth2 authorization request
     */
    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
        log.debug("ШЛЮЗ_ОАУТ2_РЕЗОЛВЕР_НАЧАЛО: разрешение OAuth2 authorization request");
        return defaultResolver.resolve(exchange)
                .map(request -> {
                    String prompt = exchange.getRequest().getQueryParams().getFirst("prompt");
                    String kcIdpHint = exchange.getRequest().getQueryParams().getFirst("kc_idp_hint");
                    
                    if (prompt != null || kcIdpHint != null) {
                        log.debug("ШЛЮЗ_ОАУТ2_РЕЗОЛВЕР_ПАРАМЕТРЫ: добавление параметров prompt={}, kc_idp_hint={}", prompt, kcIdpHint);
                        Map<String, Object> additionalParameters = new HashMap<>(request.getAdditionalParameters());
                        
                        if (prompt != null) {
                            additionalParameters.put("prompt", prompt);
                        }
                        if (kcIdpHint != null) {
                            additionalParameters.put("kc_idp_hint", kcIdpHint);
                        }
                        
                        OAuth2AuthorizationRequest modifiedRequest = OAuth2AuthorizationRequest.from(request)
                                .additionalParameters(additionalParameters)
                                .build();
                        log.debug("ШЛЮЗ_ОАУТ2_РЕЗОЛВЕР_УСПЕХ: OAuth2 authorization request модифицирован");
                        return modifiedRequest;
                    }
                    
                    return request;
                });
    }

    /**
     * Разрешает OAuth2 authorization request для конкретного клиента, добавляя параметры prompt и kc_idp_hint.
     *
     * @param exchange обмен данными веб-запроса
     * @param clientRegistrationId идентификатор регистрации клиента
     * @return Mono с OAuth2 authorization request
     */
    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
        log.debug("ШЛЮЗ_ОАУТ2_РЕЗОЛВЕР_НАЧАЛО: разрешение OAuth2 authorization request для clientRegistrationId: {}", clientRegistrationId);
        return defaultResolver.resolve(exchange, clientRegistrationId)
                .map(request -> {
                    String prompt = exchange.getRequest().getQueryParams().getFirst("prompt");
                    String kcIdpHint = exchange.getRequest().getQueryParams().getFirst("kc_idp_hint");
                    
                    if (prompt != null || kcIdpHint != null) {
                        log.debug("ШЛЮЗ_ОАУТ2_РЕЗОЛВЕР_ПАРАМЕТРЫ: добавление параметров prompt={}, kc_idp_hint={}", prompt, kcIdpHint);
                        Map<String, Object> additionalParameters = new HashMap<>(request.getAdditionalParameters());
                        
                        if (prompt != null) {
                            additionalParameters.put("prompt", prompt);
                        }
                        if (kcIdpHint != null) {
                            additionalParameters.put("kc_idp_hint", kcIdpHint);
                        }
                        
                        OAuth2AuthorizationRequest modifiedRequest = OAuth2AuthorizationRequest.from(request)
                                .additionalParameters(additionalParameters)
                                .build();
                        log.debug("ШЛЮЗ_ОАУТ2_РЕЗОЛВЕР_УСПЕХ: OAuth2 authorization request модифицирован");
                        return modifiedRequest;
                    }
                    
                    return request;
                });
    }
}
