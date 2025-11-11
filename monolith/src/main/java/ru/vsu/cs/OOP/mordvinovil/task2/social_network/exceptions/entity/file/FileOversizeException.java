package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file;

public class FileOversizeException extends RuntimeException {
    public FileOversizeException(String message) {
        super(message);
    }
}
