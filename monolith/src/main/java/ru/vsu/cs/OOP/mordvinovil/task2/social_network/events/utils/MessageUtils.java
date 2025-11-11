package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.utils;

import org.springframework.stereotype.Component;

@Component
public class MessageUtils {

    public String truncateMessage(String message, int maxLength) {
        if (message == null) return "";
        return message.length() > maxLength ? message.substring(0, maxLength - 3) + "..." : message;
    }
}