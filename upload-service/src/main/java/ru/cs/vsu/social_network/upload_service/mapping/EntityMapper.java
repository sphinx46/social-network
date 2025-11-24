package ru.cs.vsu.social_network.upload_service.mapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EntityMapper {
    private final ModelMapper modelMapper;

    /**
     * Преобразует объект одного типа в объект другого типа.
     *
     * @param source исходный объект
     * @param targetClass класс целевого объекта
     * @param <S> тип исходного объекта
     * @param <T> тип целевого объекта
     * @return преобразованный объект или null, если source равен null
     */
    public <S, T> T map(final S source, final Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        T result = modelMapper.map(source, targetClass);
        return result;
    }

    /**
     * Преобразует список объектов одного типа в список объектов другого типа.
     *
     * @param source исходный список
     * @param targetClass класс целевых объектов
     * @param <S> тип исходных объектов
     * @param <T> тип целевых объектов
     * @return преобразованный список или пустой список, если source равен null
     */
    public <S, T> List<T> mapList(
            final List<S> source, final Class<T> targetClass) {
        if (source == null) {
            return List.of();
        }
        List<T> result = source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
        return result;
    }
}
