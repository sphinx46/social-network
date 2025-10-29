package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.CacheMode;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.MessageService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.CachingMessageServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.MessageServiceImpl;

@Component
@RequiredArgsConstructor
public class MessageServiceFactory {
    private final MessageServiceImpl notCachingService;
    private final CachingMessageServiceImpl cachingService;

    public MessageService getService(CacheMode mode) {
        return switch (mode) {
            case CACHE -> cachingService;
            case NONE_CACHE -> notCachingService;
        };
    }
}
