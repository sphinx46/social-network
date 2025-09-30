package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.post;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException (String exception) {
        super(exception);
    }
}
