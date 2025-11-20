package ru.cs.vsu.social_network.user_profile_service.utils.constants;

import org.springframework.stereotype.Component;

/**
 * Константы сообщений для всего приложения.
 * Содержит стандартизированные тексты ошибок и системных сообщений.
 * Все константы публичны и неизменяемы для обеспечения согласованности.
 */
@Component
public final class MessageConstants {

    public static final String SERVER_ERROR = "Внутренняя ошибка сервера";

    public static final String ACCESS_DENIED = "Доступ запрещен";

    public static final String NOT_FOUND = "Ресурс не найден";

    public static final String FAILURE_PROFILE_BIO_TOO_LONG = "Ошибка! Биография слишком длинная.";

    public static final String FAILURE_PROFILE_CITY_TOO_LONG = "Ошибка! Название города слишком длинное";
    public static final String FAILURE_PROFILE_NOT_FOUND = "Ошибка! Профиль не найден";

    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     * Класс предназначен только для хранения констант.
     */
    private MessageConstants() {}
}