package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class FileEmptyException extends RuntimeException {
    public FileEmptyException(String message) {
        super(message);
    }
}
