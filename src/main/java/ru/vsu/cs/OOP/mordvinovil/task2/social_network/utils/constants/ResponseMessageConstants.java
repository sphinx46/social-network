package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants;

public final class ResponseMessageConstants {
    // DEFAULT CONSTANTS
    public static final String SERVER_ERROR = "Внутренняя ошибка сервера";
    public static final String ACCESS_DENIED = "Доступ запрещен";
    public static final String NOT_FOUND = "Ресурс не найден";

    // Message CONSTANTS
    public static final String FAILURE_CREATE_SELF_MESSAGE = "Ошибка! Нельзя отправить сообщение самому себе.";
    public static final String FAILURE_MESSAGE_CONTENT_CANNOT_BE_EMPTY = "Ошибка! Содержание сообщения не может быть пустым";
    public static final String FAILURE_MESSAGE_CONTENT_TOO_LONG = "Ошибка! Содержание сообщения слишком длинное.";
    public static final String FAILURE_MESSAGE_NOT_FOUND = "Ошибка! Сообщение не найдено";

    // POST CONSTANTS
    public static final String FAILURE_POST_NOT_FOUND = "Ошибка! Пост не найден";
    public static final String FAILURE_POST_CONTENT_CANNOT_BE_EMPTY = "Ошибка! Содержание поста не может быть пустым";
    public static final String FAILURE_POST_CONTENT_TOO_LONG = "Ошибка! Содержание поста слишком длинное.";

    // LIKE CONSTANTS
    public static final String FAILURE_LIKE_ALREADY_EXISTS_ON_POST = "Ошибка! Лайк уже поставлен на данный пост.";
    public static final String FAILURE_LIKE_NOT_FOUND = "Ошибка! Лайк не найден.";

    // COMMENT CONSTANTS
    public static final String FAILURE_COMMENT_EMPTY_CONTENT = "Ошибка! Содержание комментария не может быть пустым.";
    public static final String FAILURE_COMMENT_CONTENT_TOO_LONG = "Ошибка! Содержание комментария слишком длинное.";
    public static final String FAILURE_COMMENT_NOT_FOUND = "Ошибка! Комментарий не найден.";

    // USER CONSTANTS
    public static final String FAILURE_USER_NOT_FOUND = "Ошибка! Пользователь не найден";
    public static final String FAILURE_USERNAME_NOT_FOUND = "Ошибка! Пользователь c таким ником не найден";
    public static final String FAILURE_USER_WITH_EMAIL_ALREADY_EXISTS = "Ошибка! Пользователь с таким email уже существует";
    public static final String FAILURE_USER_WITH_NAME_ALREADY_EXISTS = "Ошибка! Пользователь с таким именем уже существует";

    // NOTIFICATION CONSTANTS
    public static final String FAILURE_NOTIFICATION_NOT_FOUND = "Ошибка! Оповещение не найдено.";

    // PROFILE CONSTANTS
    public static final String FAILURE_PROFILE_BIO_TOO_LONG = "Ошибка! Биография слишком длинная.";
    public static final String FAILURE_PROFILE_CITY_TOO_LONG = "Ошибка! Название города слишком длинное";
    public static final String FAILURE_PROFILE_NOT_FOUND = "Ошибка! Профиль не найден";
    public static final String FAILURE_CREATE_PROFILE = "Ошибка! Профиль уже существует.";

    // RELATIONSHIP CONSTANTS
    public static final String FAILURE_RELATIONSHIP_PENDING_REQUESTS_NOT_FOUND = "Ошибка! Входящих запросов в друзья не найдено.";
    public static final String FAILURE_RELATIONSHIP_TO_SELF_OPERATION = "Ошибка! Нельзя отправить запрос самому себе";
    public static final String FAILURE_RELATIONSHIP_ALREADY_EXISTS = "Ошибка! Дружба уже существует";
    public static final String FAILURE_RELATIONSHIP_NOT_FOUND = "Ошибка! Отношения не найдены";
    public static final String FAILURE_RELATIONSHIP_CANNOT_CHANGE_QUERY = "Ошибка! Вы не можете изменить этот запрос";

    // FILE STORAGE CONSTANTS
    public static final String FAILURE_FILE_SAVE = "Ошибка при сохранении файла";
    public static final String FAILURE_INCORRECT_PATH_TO_FILE = "Ошибка! Некорректный путь к файлу.";
    public static final String FAILURE_FILE_EMPTY = "Ошибка! Файл не может быть пустым";
    public static final String FAILURE_FILE_OVERSIZE = "Ошибка! Размер файла не должен превышать 5MB";
    public static final String FAILURE_FILE_MUST_BE_IMAGE = "Ошибка! Файл должен быть изображением";
    public static final String FAULURE_FILE_UNSUPPORTED_FORMAT = "Ошибка! Поддерживаются только JPG, JPEG, PNG, GIF, BMP файлы";


    private ResponseMessageConstants() {}
}