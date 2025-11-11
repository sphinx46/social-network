package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.EntityNotFoundException;

import java.util.Optional;

public interface EntityProvider<T, ID> {
    T getById(ID id) throws EntityNotFoundException;
    Optional<T> findById(ID id);
}