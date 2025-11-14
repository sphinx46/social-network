package ru.cs.vsu.social_network.user_profile_service.mapping;

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
    public <S, T> T map(S source, Class<T> targetClass) {
        if (source == null) {
            log.debug("ПРОФИЛЬ_МАППИНГ_ПУСТОЙ: исходный объект null, возвращаем null");
            return null;
        }
        T result = modelMapper.map(source, targetClass);
        log.debug("ПРОФИЛЬ_МАППИНГ_УСПЕХ: объект {} успешно преобразован в {}", source.getClass().getSimpleName(), targetClass.getSimpleName());
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
    public <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        if (source == null) {
            log.debug("ПРОФИЛЬ_МАППИНГ_СПИСОК_ПУСТОЙ: исходный список null, возвращаем пустой список");
            return List.of();
        }
        log.debug("ПРОФИЛЬ_МАППИНГ_СПИСОК_НАЧАЛО: преобразование списка из {} элементов", source.size());
        List<T> result = source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
        log.debug("ПРОФИЛЬ_МАППИНГ_СПИСОК_УСПЕХ: список успешно преобразован, элементов: {}", result.size());
        return result;
    }
}
