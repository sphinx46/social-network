package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services;

public interface Validator<T, U> {
    void validate(T request, U currentUser);
}