package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providersImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.EntityNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.NotificationNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers.NotificationEntityProvider;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NotificationEntityProviderImpl implements NotificationEntityProvider {
    private final NotificationRepository notificationRepository;

    @Override
    public Notification getById(Long id) throws EntityNotFoundException {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }
}
