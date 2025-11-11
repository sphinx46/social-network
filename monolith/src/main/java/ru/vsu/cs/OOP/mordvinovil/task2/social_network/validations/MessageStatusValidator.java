package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;

public class MessageStatusValidator {
    public static boolean isStatusAllowed(MessageStatus currentStatus, MessageStatus... allowedStatuses) {
        for (MessageStatus allowed : allowedStatuses) {
            if (currentStatus == allowed) {
                return true;
            }
        }
        return false;
    }
}



