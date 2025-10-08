package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants;

public final class ResponseMessageConstants {

    // POST CONSTANTS
    public static final String SUCCESSFUL_CREATE_POST = "Пост успешно создан";
    public static final String SUCCESSFUL_DELETE_POST = "Пост успешно удален";
    public static final String SUCCESSFUL_UPLOAD_PICTURE = "Фотография успешно загружена";
    public static final String SUCCESSFUL_DELETE_PICTURE = "Фотография успешно удалена";
    public static final String SUCCESSFUL_UPDATE_PROFILE = "Профиль успешно обновлен";

    // DEFAULT CONSTANTS
    public static final String SERVER_ERROR = "Внутренняя ошибка сервера";
    public static final String ACCESS_DENIED = "Доступ запрещен";
    public static final String NOT_FOUND = "Ресурс не найден";

    // PROFILE CONSTANTS
    public static final String FAILURE_CREATE_PROFILE = "Профиль уже существует.";

    // Message CONSTANTS
    public static final String FAILURE_CREATE_SELF_MESSAGE = "Нельзя отправить сообщение самому себе.";

    private ResponseMessageConstants() {}
}