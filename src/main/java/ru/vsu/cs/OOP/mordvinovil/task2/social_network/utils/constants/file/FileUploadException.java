package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.file;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FileUploadException extends RuntimeException {

    public FileUploadException (String exception) {
        super(exception);
    }
}