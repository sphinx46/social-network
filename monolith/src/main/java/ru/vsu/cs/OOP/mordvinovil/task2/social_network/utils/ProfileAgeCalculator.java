package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils;

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