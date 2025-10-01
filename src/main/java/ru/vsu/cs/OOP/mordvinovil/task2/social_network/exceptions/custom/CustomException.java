package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CustomException extends RuntimeException{

    public CustomException(String exception) {
        super(exception);
    }
}