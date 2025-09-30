package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.profile;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException (String exception) {
        super(exception);
    }
}