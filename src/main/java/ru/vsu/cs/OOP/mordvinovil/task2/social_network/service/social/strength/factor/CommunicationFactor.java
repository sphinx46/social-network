package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;

@Slf4j
@Component
public final class CommunicationFactor extends AbstractConnectionStrengthFactor {
    private final MessageRepository messageRepository;

    public CommunicationFactor(MessageRepository messageRepository) {
        super(0.4);
        this.messageRepository = messageRepository;
    }

    @Override
    public String getFactorName() {
        return "commonMessages";
    }

    @Override
    public double calculateStrength(Long userId, Long targetUserId) {
        try {
            boolean hasConversation = messageRepository.existsConversationBetweenUsers(userId, targetUserId);
            return hasConversation ? weight : 0.0;
        } catch (Exception e) {
            log.error("Error calculating communication factor", e);
            return 0.0;
        }
    }
}