package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;

@Slf4j
@Component
public final class GeographicFactor extends AbstractConnectionStrengthFactor {
    private final EntityUtils entityUtils;

    public GeographicFactor(EntityUtils entityUtils) {
        super(0.15);
        this.entityUtils = entityUtils;
    }

    @Override
    public String getFactorName() {
        return "commonCity";
    }

    @Override
    public double calculateStrength(Long userId, Long targetUserId) {
        try {
            Profile user1 = entityUtils.getProfileByUserId(userId);
            Profile user2 = entityUtils.getProfileByUserId(targetUserId);
            return user1.getCity().equals(user2.getCity()) ? weight : 0.0;
        } catch (Exception e) {
            log.error("Error calculating geographic factor", e);
            return 0.0;
        }
    }
}