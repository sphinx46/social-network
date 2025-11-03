package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.factor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.ProfileAgeCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;

@Slf4j
@Component
public final class DemographicFactor extends AbstractConnectionStrengthFactor {
    private final EntityUtils entityUtils;
    private final ProfileAgeCalculator ageCalculator;

    public DemographicFactor(EntityUtils entityUtils, ProfileAgeCalculator ageCalculator) {
        super(0.13);
        this.entityUtils = entityUtils;
        this.ageCalculator = ageCalculator;
    }

    @Override
    public String getFactorName() {
        return "similarAge";
    }

    @Override
    public double calculateStrength(Long userId, Long targetUserId) {
        try {
            Profile user1 = entityUtils.getProfileByUserId(userId);
            Profile user2 = entityUtils.getProfileByUserId(targetUserId);

            if (user1.getDateOfBirth() == null || user2.getDateOfBirth() == null) {
                return 0.0;
            }

            Integer ageUser1 = ageCalculator.calculateAge(user1.getDateOfBirth());
            Integer ageUser2 = ageCalculator.calculateAge(user2.getDateOfBirth());

            return Math.abs(ageUser1 - ageUser2) <= 5 ? weight : 0.0;
        } catch (Exception e) {
            log.error("Error calculating demographic factor", e);
            return 0.0;
        }
    }
}