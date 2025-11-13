package ru.cs.vsu.social_network.user_profile_service.utils.calculating;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class ProfileAgeCalculator {

    public Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(LocalDate.from(dateOfBirth), LocalDate.now()).getYears();
    }
}