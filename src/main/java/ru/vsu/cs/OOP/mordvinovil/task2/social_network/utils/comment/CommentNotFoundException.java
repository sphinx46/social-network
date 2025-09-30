package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.comment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(String exception) {
        super(exception);
    }
}
