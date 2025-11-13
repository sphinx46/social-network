package ru.cs.vsu.social_network.user_profile_service.mapping;

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

    public <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        if (source == null) {
            return List.of();
        }
        return source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }
}