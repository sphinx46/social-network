package ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums;

public enum FriendshipStatus {
    PENDING,    // Запрос отправлен, ожидает ответа
    ACCEPTED,   // Запрос принят, пользователи друзья
    DECLINED,   // Запрос отклонен
    BLOCKED, // Один пользователь заблокировал другого
}