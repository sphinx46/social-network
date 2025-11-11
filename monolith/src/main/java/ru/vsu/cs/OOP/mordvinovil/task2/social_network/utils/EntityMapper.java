package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EntityMapper {
    private final ModelMapper modelMapper;

    public <S, T> T map(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return modelMapper.map(source, targetClass);
    }

    public <S, T> T mapWithName(S source, Class<T> targetClass, String name) {
        if (source == null) {
            return null;
        }
        return modelMapper.map(source, targetClass, name);
    }

    public <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        if (source == null) {
            return List.of();
        }
        return source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }

    public <S, T> List<T> mapListWithName(List<S> source, Class<T> targetClass, String name) {
        if (source == null) {
            return List.of();
        }
        return source.stream()
                .map(element -> modelMapper.map(element, targetClass, name))
                .collect(Collectors.toList());
    }
}