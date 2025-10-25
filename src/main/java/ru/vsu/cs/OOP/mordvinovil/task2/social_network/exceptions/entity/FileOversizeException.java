package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class FileOversizeException extends RuntimeException {
    public FileOversizeException(String message) {
        super(message);
    }
}
