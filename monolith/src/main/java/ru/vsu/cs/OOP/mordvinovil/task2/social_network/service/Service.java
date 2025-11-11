package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

public interface Service<T, U, R> {
    R create(T request, U currentUser);
}