package ru.cs.vsu.social_network.user_profile_service.utils.calculating;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

/**
 * Компонент для расчета возраста на основе даты рождения.
 * Использует Java Time API для точного вычисления количества полных лет.
 * Возвращает null при отсутствии даты рождения.
 */
@Component
public class ProfileAgeCalculator {

    /**
     * Вычисляет возраст в полных годах на текущую дату.
     * Возвращает null если дата рождения не указана.
     */
    public Integer calculateAge(final LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(LocalDate.from(dateOfBirth), LocalDate.now()).getYears();
    }
}