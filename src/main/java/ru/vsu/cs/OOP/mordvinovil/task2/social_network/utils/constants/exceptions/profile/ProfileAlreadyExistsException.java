package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.exceptions.profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ProfileAlreadyExistsException extends RuntimeException {

    public ProfileAlreadyExistsException (String exception) {
        super(exception);
    }
}